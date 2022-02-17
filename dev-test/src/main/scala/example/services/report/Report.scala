package example.services.report

import io.jvm.uuid._

import scala.concurrent.{ExecutionContext, Future}
import example.services.report.models.Result
import example.utils.Util

case class Report(repo: ReportRepo2) {
  implicit val ex: ExecutionContext = ExecutionContext.global

  def report(surveyId: UUID, langCode: String): Future[Map[UUID, Result]] = {
    // That's questionId -> Result

    // Add methods to repo to fetch the data from MySQL
    Future(Map(Util.uuid("4578706c-6f72-6951-3132-333000000000") -> Result(List("A"), List(1),List(1.0), Some(1.0) )))

  }
}
