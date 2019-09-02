package clients

import play.api.libs.json.Json

case class Response (
  code: String,
  body: String
)

object Response {
  implicit val responseFormat = Json.format[Response]
}
