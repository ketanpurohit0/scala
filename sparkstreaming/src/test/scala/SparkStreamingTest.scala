import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

class SparkStreamingTest extends org.scalatest.funsuite.AnyFunSuite {

  test("spark_streaming") {

    val conf = new SparkConf().setMaster("local[2]").setAppName("streaming")
    val ssc = new StreamingContext(conf, Seconds(1))
    val sc = ssc.sparkContext.setLogLevel("ERROR")

    val lines = ssc.socketTextStream("localhost", 9999)

    val words = lines.flatMap(l => l.split(" "))

    val pairs = words.map(w => (w, 1))

    val wordCounts = pairs.reduceByKey(_ + _)

    object Helper {
      implicit class F(r: DStream[(String, Int)]) {
        def businessFilter() = r.filter(s => s._2 > 3)
      }
    }
    import Helper._
    def t(in: RDD[(String, Int)]) =
      in.filter(s => s._2 > 3)

    def m(in: RDD[(String, Int)]) = {
      in.map(s => (s._1 + "_Big", s._2))
    }

    val wcFiltered =
      wordCounts.transform(m(_)).businessFilter()

    lines.print()
    ssc.start()
    ssc.awaitTermination()
  }

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

  test("streaming") {
    // nc -q 20 -i 1 -lk 9999 < /mnt/c/MyWork/GIT/scala/TestMe/resource/keystrokes-for-tech-test.csv

    val conf = new SparkConf().setMaster("local[2]").setAppName("streaming")
    val ssc = new StreamingContext(conf, Seconds(3))
    val sc = ssc.sparkContext.setLogLevel("ERROR")

    val lines = ssc.socketTextStream("localhost", 9999)

    val tuples = lines
      .filter(l => !l.contains("match_id,message_id"))
      .transform(convertToTuples(_))
      .transform(parseJson(_))
      .transform(filterOutBad(_))

    tuples.print()

    ssc.start()
    ssc.awaitTermination()

  }
}
