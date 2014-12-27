package controllers

import models.JsonFormats._
import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._

import scala.concurrent.Future
// Reactive Mongo imports

// Reactive Mongo plugin, including the JSON-specialized collection
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

object UsersController extends Controller with MongoController {

  def usersCollection: JSONCollection = db.collection[JSONCollection]("users")

  val newUserForm = Form(
    mapping(
      "firstName" -> nonEmptyText,
      "surname" -> nonEmptyText
    )(User.apply)(User.unapply)
  )

  def createUser = Action.async { implicit request =>
    newUserForm.bindFromRequest.fold(
      errors => Future(Ok("You right cocked that one up son")),
      user => {
        usersCollection.insert(user)
        Future(Ok("You've just created a user well done you!!"))
      }
    )
  }

}