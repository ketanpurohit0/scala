package example.services.report.models

import slick.jdbc.GetResult
import io.jvm.uuid._

import example.utils.Util._

case class SurveyDataOpt(
  surveyId: UUID,
  surveyDataId: UUID,
  questionId: UUID,
  setY: UUID,
  setX: UUID,
)

object SurveyDataOpt{
  implicit val percent = GetResult(
    r => SurveyDataOpt(uuid(r.<<), uuid(r.<<), uuid(r.<<), uuid(r.<<), uuid(r.<<))
  )
}
