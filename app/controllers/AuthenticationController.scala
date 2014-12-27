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

object AuthenticationController extends Controller with MongoController {

  def loginPage() = Action.async { implicit request =>
    Future(Ok(views.html.index()))
  }

  def authenticate() = {Action.async { implicit request =>
        Future(Ok("WOOOOOOO"))
    }


  }
}