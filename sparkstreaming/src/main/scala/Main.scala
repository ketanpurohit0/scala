import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}
import play.api.libs.json.{JsObject, JsValue, Json}
import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream.DStream
import play.api.libs.json._

import scala.collection.mutable
import scala.util.parsing.input.PagedSeq
import scala.util.{Failure, Success, Try}

object Main extends App {
  val conf = new SparkConf().setMaster("local[2]").setAppName("streaming")
  val ssc = new StreamingContext(conf, Milliseconds(3000))
  ssc.checkpoint("./checkpoint_ketan")
  val sc = ssc.sparkContext.setLogLevel("ERROR")

  val lines = ssc.socketTextStream("localhost", 9999)

  val tuples = lines
    .filter(l => !l.contains("match_id,message_id"))
    .transform(convertToTuples(_))
    .transform(parseJson(_))
    .transform(filterOutBad(_))
    .transform(toKeyValuePair(_))

//  tuples.print()
  tuples.updateStateByKey(keepMatchScore _).print()
  tuples.updateStateByKey(countFaults _).print()

  ssc.start()
  ssc.awaitTermination()

  // ==
  def convertToTuples(in: RDD[String]): RDD[(Long, Int, String)] = {
    in.map(s => {
      val tokens = s.split(",")
      val match_id = tokens(1)
      val message_id = tokens(2)
      val cleanJson = tokens
        .slice(3, tokens.length)
        .mkString(",")
        .replace("\"", "")
        .replace('\'', '"')

      (match_id.toLong, message_id.toInt, cleanJson)
    })
  }

  def parseJson(
      in: RDD[(Long, Int, String)]
  ): RDD[(Option[JsValue], Long, Option[JsValue], Try[JsValue])] = {
    in.map(t => {
      val (match_id, message_id, json) = (t._1, t._2, Try { Json.parse(t._3) })
      val (seqNum, eventElementType) = json match {
        case Success(value) => {
          value match {
            case JsObject(underlying) =>
              (underlying.get("seqNum"), underlying.get("eventElementType"))
            case _ => (Option.empty[JsValue], Option.empty[JsValue])
          }
        }
      }

      (seqNum, match_id, eventElementType, json)

    })
  }

  def filterOutBad(
      in: RDD[(Option[JsValue], Long, Option[JsValue], Try[JsValue])]
  ): RDD[(Option[JsValue], Long, Option[JsValue], Try[JsValue])] = {
    in.filter(i => {
      val json = i._4
      json match {
        case Failure(exception) => false // write bad record to some topic?
        case Success(value)     => true
      }
    })
  }

  def toKeyValuePair(
      in: RDD[(Option[JsValue], Long, Option[JsValue], Try[JsValue])]
  ) = {
    in.map(i => {
      val seqNo = i._1 match {
        case Some(value) => value.as[Int]
        case None        => 0
      }

      val match_id = i._2

      val eventElementType = i._3 match {
        case Some(value) => value.as[String]
        case None        => "Unknown"
      }

      val json = i._4 match {
        case Failure(exception) => Json.parse("")
        case Success(value)     => value
      }

      ((match_id), (seqNo, eventElementType, json))
    })
  }

  def countFaults(
      s: Seq[(Int, String, JsValue)],
      oldValue: Option[Int]
  ): Option[Int] = {

    val oldValue_ : Option[Int] = oldValue match {
      case Some(value) => Some(value)
      case None        => Some(0)
    }

    val newValue = s.foldLeft(oldValue_)((runningCount, tuples) => {
      val json = tuples._3
      val eventElementType = (json \ "eventElementType")
      eventElementType match {
        case JsDefined(value) =>
          value match {
            case JsString(strValue) =>
              if (strValue == "PointFault")
                for (x <- runningCount; y <- Some(1)) yield x + y
              else runningCount
            case _ => runningCount
          }
        case undefined: JsUndefined => runningCount
      }
    })

    newValue
  }

  def keepMatchScore(
      s: Seq[(Int, String, JsValue)],
      oldValue: Option[String]
  ): Option[String] = {

//    println("===O>>", oldValue)
    var overallScore = oldValue
    s.foreach(si => {
      val json = si._3
      val prevSetsScore =
        (json \ "score" \ "previousSetsScore")
//      println("A>>", prevSetsScore)
      overallScore = prevSetsScore match {
        case JsDefined(value) =>
          value match {
            case JsArray(arrayValue) => {
              val games_wonByA = arrayValue
                .filter(item => {
                  item match {
                    case JsNull             => true
                    case boolean: JsBoolean => true
                    case JsNumber(value)    => true
                    case JsString(value)    => true
                    case JsArray(value)     => true
                    case JsObject(underlying) => {
                      val gamesA = underlying.get("gamesA")
                      val gamesB = underlying.get("gamesB")
//                      println("B>>", gamesA, gamesB)
                      val r = for {
                        game_wonByA <- gamesA
                        game_wonByB <- gamesB
                      } yield game_wonByA.as[Int] > game_wonByB.as[Int]
                      val result = r match {
                        case Some(boolean_result) => boolean_result
                        case None                 => false
                      }

                      result
                    }
                  }
                })
                .length
//              println(
//                "C>>",
//                arrayValue.mkString(","),
//                arrayValue.length,
//                games_wonByA
//              )
              val newv = Some(
                Seq(games_wonByA, arrayValue.length - games_wonByA)
                  .mkString("-")
              )

//              println("N>>", newv)
              newv
            }
          }
        case undefined: JsUndefined => overallScore
      }
    })

    val newValue = overallScore match {
      case Some(value) => overallScore
      case None =>
        oldValue match {
          case None => Some("0-0")
          case _    => oldValue
        }
    }

//    println("===R>>", newValue)

    newValue

  }
}
