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


    println("START ---------------------------------------------------")
    val relevantQuestions = result_question.map(r =>r.questionId.toString.toUpperCase())

    // questionId, (setY.hasNumericCodes, List((reportingValue, setYid)))
    val question_details = result_question.map(r => (r.questionId.toUpperCase,r.setY.map(s => (s.hasNumericCodes, if (s.hasNumericCodes) s.options.map(o => (o.reportingValue, o.id.toUpperCase)) else List()))))

    val tryAgain = result_question.map(f => (f.questionId, f.setY))
    val xx = tryAgain.flatMap(t => t._2)
    val xx2 = result_question.flatMap(f => f.setY)
    // (questionId, setYid, hasNunericCodes, Optional(reportingValue)
    val result_question_flattened = for {
      a <- result_question
      xx2 <- a.setY
      y = xx2.options.map(o => (a.questionId.toUpperCase, o.id.toUpperCase, xx2.hasNumericCodes, o.reportingValue))
    } yield y

    println("**!", result_question_flattened.length, result_question.length)
//    l.foreach(li => println(li))
    result_question_flattened.flatten.foreach(li => println(li))

    println("END ---------------------------------------------------")

    println("START -------------------------------------------------")
    val f_survey = reportRepo.explore_surveydataopt(surveyId, relevantQuestions, "en_GB")
    val result_summary_stats = Await.result(f_survey, 15.seconds)
    // (questionId, setYid, count(*))
    val filtered_summary_stats = result_summary_stats.filter(p => relevantQuestions.contains(p._1.toUpperCase))
    println("END ---------------------------------------------------")

//    val monadic_join = for {
//      details <- question_details
//      summary_stats <- filtered_summary_stats
//      if (details._1 == summary_stats._1)
//    } yield (details, summary_stats)
//
//    println("MJ1 -------------------------------------------------")
//    monadic_join.foreach(m => println(m))

    val monadic_join2 = for {
      details <- result_question_flattened.flatten.groupBy(f => f._1)
      summary_stats <- filtered_summary_stats.groupBy(f => f._1)
      if (details._1 == summary_stats._1) //&& (details._2 == summary_stats._2)
      result_responses = summary_stats._2.map(x=>x._3)
      result_results = result_responses.map(f => (100.0*f)/result_responses.sum )
    } yield (summary_stats._1, result_responses,result_results)

    println("MJ2 -------------------------------------------------")
    monadic_join2.foreach(m => println(m))


  }
}