package example.services.report

import slick.jdbc.MySQLProfile.api._
import slick.sql.SqlStreamingAction

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import io.jvm.uuid._


import example.services.report.models._

case class ReportRepo2(private val db: Database) {
  implicit private val ex: ExecutionContext = ExecutionContext.global

  def test() = {
    val query =
      sql"""
      SELECT 1
    """

    runAction(query.as[Int])
  }

  def runAction[A](action: SqlStreamingAction[Vector[A], A, Effect]) = {
    val futTry = db.run(action.asTry)
    val result = futTry map {
      case Success(tr) =>
        tr
      case Failure(t) =>
        throw t
    }
    result
  }

  def explore_survey() = {
    val query =
      sql"""
      SELECT * FROM survey
    """

    runAction(query.as[Survey])
  }

  def explore_question() = {
    val query =
      sql"""
            SELECT * FROM question
            INNER JOIN surveypagequestion ON surveypagequestion.questionId = question.questionId
            INNER JOIN survey ON survey.surveyId = surveypagequestion.surveyId
            WHERE question.questionType IN ('RD','CH')
            AND survey.surveyId = 'ACC36FA7-4B09-11E9-AF77-0A3056FD536A'
            -- AND question.questionId = '4578706c-6f72-6951-3132-333000000000'
        """

    runAction(query.as[Question])
  }
}