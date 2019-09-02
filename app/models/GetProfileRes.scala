package models

import play.api.libs.json.Json

case class GetProfileRes(
  profile: Profile
)

object GetProfileRes {
  implicit val getProfileResFormat = Json.format[GetProfileRes]
}
