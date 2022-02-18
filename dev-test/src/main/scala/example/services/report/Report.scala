package example.services.report

import io.jvm.uuid._

import scala.concurrent.{Await, ExecutionContext, Future}
import example.services.report.models.Result
import example.utils.Util
import scala.concurrent.duration._

case class Report(repo: ReportRepo2) {
  implicit val ex: ExecutionContext = ExecutionContext.global

  def report(surveyId: UUID, langCode: String): Future[Map[UUID, Result]] = {
    // That's questionId -> Result

    // Phase 1 - obtain details for relevant questions

    // read the questions from the database, relevant for the survey and within the types RD,CH
    val futureOnQuestions = repo.read_questions_for_survey(surveyId, "en_GB")
    val resultOnQuestions = Await.result(futureOnQuestions, 15.seconds)
    // obtain ids of the questions we need to consider when obtaining their summary details later
    val relevantQuestions = resultOnQuestions.map(r =>r.questionId.toString.toUpperCase())

    // convert the Result object to a list of tuples of the form
    // (questionId, setYid, hasNunericCodes, Optional(reportingValue)
    val result_question_flattened : Vector[(String, String, Boolean, Option[Double])] = {
      for {
        question <- resultOnQuestions
        setY <- question.setY
        y = setY.options.map(o => (question.questionId.toUpperCase, o.id.toUpperCase, setY.hasNumericCodes, o.reportingValue))
      } yield y
    }.flatten


    // Phase 2 - obtain summary details for relevant questions, but only for the questions of interest

    val futureSurveyData = repo.read_surveydataopt(surveyId, relevantQuestions, "en_GB")
    val resultSurveyData = Await.result(futureSurveyData, 15.seconds)
    // (questionId, setYid, count(*)) - only retain summary details for relevant questions
    val filteredSummaryStats :  Vector[(String, String, Int)] = resultSurveyData.filter(p => relevantQuestions.contains(p._1.toUpperCase))

    // Phase 3 - use both structures to obtain a composite view of the required result


    // Add methods to repo to fetch the data from MySQL
    Future(Map(Util.uuid("4578706c-6f72-6951-3132-333000000000") -> Result(List("A"), List(1),List(1.0), Some(1.0) )))

  }
}
