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
  ignore("responses") {
    val surveyId = UUID.fromString("ACC36FA7-4B09-11E9-AF77-0A3056FD536A")
    val report = reportService.report(surveyId, "en_GB")

    val t = Await.result(report, 15.seconds)
    val result = t(UUID.fromString("4578706c-6f72-6951-3132-333000000000"))
    List[Int](2, 1, 2, 1, 2, 4, 9, 7, 11, 4, 5) should contain theSameElementsAs (result.responses)
  }

  //testOnly *ReportSpec -- -t average
  ignore("average") {
    val surveyId = UUID.fromString("ACC36FA7-4B09-11E9-AF77-0A3056FD536A")
    val report = reportService.report(surveyId, "en_GB")

    val t = Await.result(report, 15.seconds)
    val result = t(UUID.fromString("4578706c-6f72-6951-3132-333000000000"))
    Some(6.520833333333334) should equal (result.average)
  }

  ignore("explore_survey") {
//    val surveyId = UUID.fromString("ACC36FA7-4B09-11E9-AF77-0A3056FD536A")
//
//    val f = reportRepo.explore_surveydataopt(surveyId, "en_GB")
//    val result = Await.result(f, 15.seconds)
//    println(result)
  }

  test("explore_question") {
    val surveyId = UUID.fromString("ACC36FA7-4B09-11E9-AF77-0A3056FD536A")

    val f_question = reportRepo.explore_question(surveyId, "en_GB")
    val result_question = Await.result(f_question, 15.seconds)
    //println(result)


    println("START ---------------------------------------------------")
    val relevantQuestions = result_question.map(r =>r.questionId.toString.toUpperCase())

    // questionId, (setY.hasNumericCodes, List((reportingValue, setYid)))
    val summary_statistics = result_question.map(r => (r.questionId.toUpperCase,r.setY.map(s => (s.hasNumericCodes, if (s.hasNumericCodes) s.options.map(o => (o.reportingValue, o.id.toUpperCase)) else List()))))
    println(summary_statistics.length, summary_statistics)

//    result_question.foreach(r => {
//      r.setY match {
//        case Some(setY) => {
//          println(r.questionId, setY.id, setY.hasNumericCodes)
//          if (setY.hasNumericCodes) {
//            setY.options.foreach(o => o.reportingValue match {
//              case Some(reportingValue) => println("\t", reportingValue, o.id)
//              case None => println("\t", "NO REPORTING VALUE - SHOULD NOT HAPPEN")
//            })
//          }
//          else {
//            println("\t", "NO NUMERIC CODES")
//          }
//        }
//        case None => println("NO SETY")
//      }
//    })
    println("END ---------------------------------------------------")

    println("START -------------------------------------------------")
    //    println(relevantQuestions)
    val f_survey = reportRepo.explore_surveydataopt(surveyId, relevantQuestions, "en_GB")
    val result_surveydataopt = Await.result(f_survey, 15.seconds)
//    println(result_surveydataopt.length, result_surveydataopt)
    // (questionId, setYid, count(*))
    val filtered_relevant = result_surveydataopt.filter(p => relevantQuestions.contains(p._1.toUpperCase))
    println(filtered_relevant.length,filtered_relevant)
    println("END ---------------------------------------------------")

  }
}