import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream

class SparkStreamingTest extends org.scalatest.funsuite.AnyFunSuite {

  test("spark_streaming") {
    import org.apache.spark._
    import org.apache.spark.streaming._

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

    wcFiltered.print()
    ssc.start()
    ssc.awaitTermination()
  }
}
