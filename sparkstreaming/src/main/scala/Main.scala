import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}
import play.api.libs.json.{JsObject, JsValue, Json}

import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream.DStream
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

object Main extends App {
  val conf = new SparkConf().setMaster("local[2]").setAppName("streaming")
  val ssc = new StreamingContext(conf, Seconds(3))
  ssc.checkpoint("./checkpoint_ketan")
  val sc = ssc.sparkContext.setLogLevel("ERROR")

  val lines = ssc.socketTextStream("localhost", 9999)

  val tuples = lines
    .filter(l => !l.contains("match_id,message_id"))
    .transform(convertToTuples(_))
    .transform(parseJson(_))
    .transform(filterOutBad(_))
    .transform(toKeyValuePair(_))

  tuples.updateStateByKey(updateFunc _).print()

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
        case Some(value) => 2
        case None        => 2
      }

      val match_id = i._2

      val eventElementType = i._3 match {
        case Some(value) => 2
        case None        => 2
      }

      val json = i._4 match {
        case Failure(exception) => 2
        case Success(value)     => 2
      }

      ((seqNo, match_id), ("a", "b", "c"))
    })
  }

  def updateFunc(
      s: Seq[(String, String, String)],
      o: Option[Int]
  ): Option[Int] = {
    Some(2)
  }
}
