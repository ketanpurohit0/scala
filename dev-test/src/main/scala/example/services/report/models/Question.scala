package example.services.report.models

import slick.jdbc.GetResult
import io.jvm.uuid._

import example.utils.Util._

case class Question(
  questionId: UUID,
  questionType: String,
  questionOid: Long,
  setY: Option[YOptionSet],
  setX: Option[XOptionSet],
)

object Question{
  implicit val percent = GetResult(
    r => Question(uuid(r.<<), r.<<, r.<<, setY(r.<<), setX(r.<<))
  )
}