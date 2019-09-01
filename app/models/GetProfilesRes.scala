package models

import play.api.libs.json.Json

case class GetProfilesRes(
  profiles: List[Profile]
)

object GetProfilesRes {
  implicit val getProfilesResFormat = Json.format[GetProfilesRes]
}
