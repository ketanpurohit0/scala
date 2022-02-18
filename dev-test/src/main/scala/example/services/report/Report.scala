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
    val resultQuestionFlattened : Vector[(String, String, Boolean, Option[Double])] = {
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
    val monadicJoin = for {
      // group by 'questionId'
      questionDetails <- resultQuestionFlattened.groupBy( {case (questionId, _, _, _) => questionId})
      // also group by 'questionId'
      summaryStatistics <- filteredSummaryStats.groupBy({case (questionId, _, _) => questionId})
      // monadic 'join' - must have same questionId
      if (questionDetails._1 == summaryStatistics._1)
      // collect the 'responses' into a vector, by picking out the count attribute
      responses = summaryStatistics._2.map({case (questionId, setYid, count) => count})
      // calculated the 'result' - % distribution by dividing each item by overall sum
      results = responses.map(f => (100.0*f)/responses.sum )
      // collect a map of setYid to its corresponding count
      ysetCountAsMap : Map[String, Int] = summaryStatistics._2
                                          .map { case (questionId, setYid, count) => Map(setYid -> count)}.flatten.toMap
      // collect a map of setYid to its corresponding detail
      ysetWeightsAsMap: Map[String, Option[Double]] = questionDetails._2
                                          .map { case (questionId, setYid, hasNumericCode, reportingValue) => Map(setYid -> reportingValue)}.flatten.toMap
      // perform the calculation of the average
      weightedSum = ysetCountAsMap
          .zip(ysetWeightsAsMap)
          .toList
          .map({case ((_, count),(_, weight)) => (weight.getOrElse(0.0),count)})
          .map({case (weight, count) => weight * count * 1.0}).sum
      weighedAverage = weightedSum/responses.sum

    } yield (Util.uuid(summaryStatistics._1), Result(List("NOT IMPLEMENTED"), responses.toList, results.toList, Some(weighedAverage)))

    // Respond with our map
    Future(monadicJoin)

  }
}
