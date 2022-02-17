package example.utils

import io.jvm.uuid._

import scala.util.{Failure, Success, Try}
import play.api.libs.json.Json

import example.services.report.models.{XOptionSet, YOptionSet}

object Util {
  val EmptyUUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
  val uuid: String => UUID = str => {
    Try {
      UUID.fromString(str)
    } match {
      case Failure(exception) => EmptyUUID
      case Success(uuid)      => uuid
    }
  }

  val setY: String => Option[YOptionSet] = setY => {
    Try {
      Json.parse(setY).as[YOptionSet]
    } match {
      case Failure(exception) => None
      case Success(o) => Some(o)
    }
  }

  val setX: String => Option[XOptionSet] = setX => {
    Try {
      Json.parse(setX).as[XOptionSet]
    } match {
      case Failure(exception) => None
      case Success(o) => Some(o)
    }
  }
}
