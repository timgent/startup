package controllers

import controllers.AuthenticationController.{TimSecured}
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

object CoursesController extends Controller with MongoController with TimSecured{

  def createCourse = timAuth {username => implicit request =>
    Ok(s"Basic with auth working user is $username")
  }

  def administerCourses = timAuthWithPermission(CourseAdministration) {user => implicit request =>
    Ok(s"You are allowed to administer courses ${user.username}!")
  }

}