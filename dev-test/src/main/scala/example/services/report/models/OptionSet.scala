package example.services.report.models

import io.jvm.uuid._

import play.api.libs.json._
import play.api.libs.functional.syntax._

object Axes {
  sealed trait Axis
  case object XAxis extends Axis
  case object YAxis extends Axis
  //case object ZAxis extends Axis
  
  val axisTypes = List(XAxis, YAxis)
  
  implicit object AxisFormat extends Format[Axis] {
    
    def writes(axis: Axis) = axis match {
      case XAxis => Json.toJson("XAxis")
      case YAxis => Json.toJson("YAxis")
      //case ZAxis => Json.toJson("z")
    }
    
    def reads(js: JsValue) = js.as[String] match {
      case "XAxis" => JsSuccess(XAxis)
      case "YAxis" => JsSuccess(YAxis)
      //case "z" => JsSuccess(ZAxis)
    }
  }
}

import Axes._

case class QuestionLang private(
  legacyId: Option[Long] = None,
  text: String
  ){
  
  def copy(
    legacyId: Option[Long] = this.legacyId,
    text: String = this.text
  ): QuestionLang = QuestionLang.apply(legacyId, text)
  
}
case class QuestionOptionLang private(
  legacyId: Option[Long] = None,
  text: String
){
  
  def copy(
    legacyId: Option[Long] = this.legacyId,
    text: String = this.text
  ) = QuestionOptionLang.apply(legacyId, text)
  
}
object QuestionOptionLang {
  
  def apply(
    legacyId: Option[Long] = None,
    text: String    
  ) = new QuestionOptionLang(
      legacyId = legacyId,
      text = text
    )
  
  implicit val format = Json.using[Json.WithDefaultValues].format[QuestionOptionLang]
}

case class QuestionOption(
  id: UUID = UUID.random, 
  originalId: Option[UUID] = None,
  legacyId: Option[Long] = None,
  reportingValue: Option[Double] = None,
  langs: Map[String, QuestionOptionLang],
  priority: Int,
  isHidden: Boolean = false,
  originalQuestionOptionId: Option[Long] = None,
  optionOtherIsMandatory: Boolean = false,
  hasOptionOther: Boolean = false,
  isExclusive: Boolean = false,
  fieldType: String = "RD",
  isLocked: Boolean = false,
  isNonReporting: Boolean = false
    ) {
}
object QuestionOption {
  
  implicit object ExploriLocaleMapFormat extends Format[Map[String, QuestionOptionLang]] {
    
    def reads(json: JsValue): JsResult[Map[String, QuestionOptionLang]] = JsSuccess {
      val r = json.as[JsObject].value.map {
        case (k, v) => (k, v.as[QuestionOptionLang])
      }.toMap
      r
    }
    
    def writes(o: Map[String, QuestionOptionLang]) = {
      val seqStrings = o.map { case (k, v) =>
        k.toString -> Json.toJson(v)
      }.toSeq
      JsObject(seqStrings)
    }
      
  }
  
  implicit val format = Json.using[Json.WithDefaultValues].format[QuestionOption]
}

sealed trait OptionSet {
  
  val axis: Axis
  /*
  val hasNumericCodes: Boolean = options.map(_.reportingValue).map(_.isDefined).exists(_ == true)
  val hasExclusiveOptions: Boolean = options.map(_.isExclusive).exists(_ == true)
  */
  
}

case class XOptionSet(
  id: UUID = UUID.random,
  originalId: Option[UUID] = None,
  legacyId: Option[Long] = None,
  options: List[QuestionOption] = Nil,
  sortType: Option[String] = None,
  hasNumericCodes: Boolean = false,
  isExclusive: Boolean = false
) extends OptionSet {
  override val axis: Axis = XAxis
}

object XOptionSet {
  //implicit val format = Json.using[Json.WithDefaultValues].format[XOptionSet]
  
  /*
  val writes: Writes[XOptionSet] = (
    (JsPath \ "id").write[UUID] and
    (JsPath \ "originalId").writeNullable[UUID] and
    (JsPath \ "legacyId").writeNullable[Long] and
    (JsPath \ "options").write[List[QuestionOption]] and
    (JsPath \ "sortType").writeNullable[QuestionOptionSortType] and
    (JsPath \ "hasNumericCodes").write[Boolean] and
    (JsPath \ "isExclusive").write[Boolean]
  )(unlift(XOptionSet.unapply))
  * 
  */
  val defaultWrites = Json.using[Json.WithDefaultValues].writes[XOptionSet]
  val writesWithAxis: Writes[XOptionSet] = (defaultWrites ~ (__ \ "axis").write[Axis])((s: XOptionSet) => (s, s.axis))
  
  val reads = Json.using[Json.WithDefaultValues].reads[XOptionSet]
  
 implicit val format: Format[XOptionSet] = Format(reads, writesWithAxis)
  
}

case class YOptionSet(
  id: UUID = UUID.random,
  originalId: Option[UUID] = None,
  legacyId: Option[Long] = None,
  options: List[QuestionOption] = Nil,
  sortType: Option[String] = None,
  hasNumericCodes: Boolean = false,
  isExclusive: Boolean = false
) extends OptionSet {
  override val axis: Axis = YAxis
}

object YOptionSet {
  //implicit val format = Json.using[Json.WithDefaultValues].format[YOptionSet]
  
  val defaultWrites = Json.using[Json.WithDefaultValues].writes[YOptionSet]
  val writesWithAxis: Writes[YOptionSet] = (defaultWrites ~ (__ \ "axis").write[Axis])((s: YOptionSet) => (s, s.axis))
  
  val reads = Json.using[Json.WithDefaultValues].reads[YOptionSet]
  
  implicit val format: Format[YOptionSet] = Format(reads, writesWithAxis)
}
    
object OptionSet {
  
  implicit val reads: Reads[OptionSet] = {
    (__ \ "axis").read[String].flatMap {
      case "x" => implicitly[Reads[XOptionSet]].map(identity)
      case "y" => implicitly[Reads[YOptionSet]].map(identity)
      case other => Reads(_ => JsError(s"Unknown question type $other"))
    }
  }
  implicit val writes: Writes[OptionSet] = Writes { optionSet =>
    val (jsValue, axis) = optionSet match {
      case a: XOptionSet => (Json.toJson(a)(XOptionSet.format), "x")
      case a: YOptionSet => (Json.toJson(a)(YOptionSet.format), "y")
    }
    jsValue.transform(__.json.update((__ \ 'axis).json.put(JsString(axis)))).get
  }
  
}