import scala.io.Source
object Main extends App {

  val telco = Source.fromFile(raw"C:\MyWork\GIT\scala\TeralyticsTest\resources\Germany-telco-age.csv")
  val telco_lines = telco.getLines().drop(1)
  // lines1.foreach(println)

  val consensus = Source.fromFile(raw"C:\MyWork\GIT\scala\TeralyticsTest\resources\Germany-census-age.csv")
  val census_lines = consensus.getLines().drop(1)
  // lines2.foreach(println)

  val mapped_lines1 = telco_lines
    .map(l => {
      val v = l.split(",")
      v(0) -> v(1).toInt
    })
    .toMap

  val telco_sum = mapped_lines1.values.sum - mapped_lines1("u")

  val telco_ratios = mapped_lines1.map(r => r._1 -> (r._2 * 1.0 / telco_sum))

  // println(telco_ratios)

  // == census
  val mapped_census1 = census_lines
    .map(l => {
      val v = l.split(",")
      v(0) -> v(1).toInt
    })
    .toMap

  val census_sum = mapped_census1.values.sum

  val census_ratios = mapped_census1.map(r => r._1 -> (r._2 * 1.0 / census_sum))

  // println(census_ratios)

  census_ratios.foreach(cd => {
    val adjustment_ratio = cd._2 / telco_ratios(cd._1)
    println(cd._1, adjustment_ratio)
  })

}
