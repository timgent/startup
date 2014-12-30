package controllers

import models.JsonFormats._
import models._
import play.api.libs.json._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._

import scala.concurrent.Future
import scala.util._

// Reactive Mongo imports

// Reactive Mongo plugin, including the JSON-specialized collection
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

object UsersController extends Controller with MongoController {

  def usersCollection: JSONCollection = db.collection[JSONCollection]("users")

  val newUserForm = Form(
    mapping(
      "firstName" -> nonEmptyText,
      "surname" -> nonEmptyText,
      "password" -> nonEmptyText
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

  def checkLoginDetails(email  : String, password: String): Future[Boolean] = {
    val test = findByEmail(email)
    test
    findByEmail(email).map(user => user match {
      case Some(user) => user.email == email && user.password == password
      case None => false
    })
  }

  def DELETEME = {
    Future(List(User("tim.gent", "tim.gent@gmail.com", "password")))
  }


  def findByEmail(email: String): Future[Option[User]] = {
    // let's do our query
    val cursor = usersCollection.
      // find all people with name `name`
      find(Json.obj("email" -> email)).
      // perform the query and get a cursor of JsObject
      cursor[User]

    // gather all the JsObjects in a list
    cursor.collect[List]().map{userList =>
      if (userList.size == 1) {
        Some(userList.head)
      } else None}
  }
  
}