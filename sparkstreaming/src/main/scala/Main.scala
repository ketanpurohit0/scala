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

  // nc -q 20 -i 1 -lk 9999 < /mnt/c/MyWork/GIT/scala/TestMe/resource/keystrokes-for-tech-test.csv
  // above submits 1 line every 1 second from file, drip feeding events

  // nc -lk 9999 < /mnt/c/MyWork/GIT/scala/TestMe/resource/keystrokes-for-tech-test.csv
  // above submits ALL lines immediately, 'drink from a hose-pipe'
  val ssc =
    Helper.sparkStreamingContext("local[2]", "streaming", Milliseconds(3000))
  ssc.checkpoint("./checkpoint_ketan")

  val lines = ssc.socketTextStream("localhost", 9999)

  // set up transforms
  val tuples = lines
    .filter(l => !l.contains("match_id,message_id"))
    .transform(Helper.convertToTuples(_))
    .transform(Helper.parseJson(_))
    .transform(Helper.filterOutBadJsonRecords(_))
    .transform(Helper.convertToMatchIdAsKeyAndValuePairs(_))

  // set-up calcs
  tuples.updateStateByKey(Helper.keepRunningSetScore _).print()
  tuples.updateStateByKey(Helper.keepRunningFaultsCount _).print()

  ssc.start()
  ssc.awaitTermination()

}
