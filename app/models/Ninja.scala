package models

import play.api.libs.json.Json

case class Ninja (
  dataId: String,
  ninjaId: String,
  lastAlarm: Option[String],
  lastUsername: Option[String]
)

object Ninja {
  implicit val ninjaFormat = Json.format[Ninja]
}
