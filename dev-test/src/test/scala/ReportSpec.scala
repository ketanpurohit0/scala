//package example

//import example.services.report.ReportRepo2
//import example.services.report.Report
import example.services.report.models.{Result, YOptionSet}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, FunSuite, Matchers, WordSpecLike}

import scala.concurrent.{Await, ExecutionContext, Future}
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.duration._
import akka.util.Timeout
import example.services.report.{Report, ReportRepo2}
import io.jvm.uuid._
import play.api.libs.json.Json

class ReportSpec extends FunSuite with Matchers with BeforeAndAfterAll{
  val db: Database  = Database.forConfig("mysql")
  val reportRepo    = ReportRepo2(db)
  val reportService = Report(reportRepo)

  implicit val ex: ExecutionContext = ExecutionContext.global

  override def afterAll() = {
    db.close()
  }

  //testOnly *ReportSpec -- -t responses
  test("responses") {
    val surveyId = UUID.fromString("ACC36FA7-4B09-11E9-AF77-0A3056FD536A")
    val report = reportService.report(surveyId, "en_GB")

    val t = Await.result(report, 15.seconds)
    val result = t(UUID.fromString("4578706c-6f72-6951-3132-333000000000"))
    List[Int](2, 1, 2, 1, 2, 4, 9, 7, 11, 4, 5) should contain theSameElementsAs (result.responses)
  }

  //testOnly *ReportSpec -- -t average
  test("average") {
    val surveyId = UUID.fromString("ACC36FA7-4B09-11E9-AF77-0A3056FD536A")
    val report = reportService.report(surveyId, "en_GB")

    val t = Await.result(report, 15.seconds)
    val result = t(UUID.fromString("4578706c-6f72-6951-3132-333000000000"))
    Some(6.520833333333334) should equal (result.average)
  }

  test("explore_survey") {
    val f = reportRepo.explore_survey()
    val result = Await.result(f, 15.seconds)
    println(result)
  }

  test("explore_question") {
    val f = reportRepo.explore_question()
    val result = Await.result(f, 15.seconds)
    //println(result)

    println("START ---------------------------------------------------")
    result.foreach(r => {
      r.setY match {
        case Some(setY) => {
          println(r.questionId, setY.hasNumericCodes)
          setY.options.foreach(o => o.reportingValue match {
            case Some(reportingValue) => println("\t", reportingValue)
            case None => println("\t", "NO REPORTING VALUE")
          })
        }
        case None => println("NO SETY")
      }
    })
    println("END ---------------------------------------------------")

  }
}