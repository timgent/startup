package models

import play.api.libs.json.Json
import play.api.data._
import play.api.data.Forms._

case class User(username: String, email: String, password: String)


object JsonFormats {
  // Generates Writes and Reads for User thanks to Json Macros
  implicit val userFormat = Json.format[User]
}