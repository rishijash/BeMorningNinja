package models

import play.api.libs.json.Json

case class AccountsResponse (
  accounts: List[Account]
)

object AccountsResponse {
  implicit val accountsResponseFormat = Json.format[AccountsResponse]
}
