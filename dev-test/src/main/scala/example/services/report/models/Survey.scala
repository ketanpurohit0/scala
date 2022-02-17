package example.services.report.models

import slick.jdbc.GetResult
import io.jvm.uuid._

import example.utils.Util._

case class Survey(
  surveyId: UUID,
  surveyType: String,
  respondentType: String,
  langCode: String,
  productId: Long,
)

object Survey{
  implicit val percent = GetResult(
    r => Survey(uuid(r.<<), r.<<, r.<<, r.<<, r.<<)
  )
}