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

//object CoursesController extends Controller with MongoController {
//
//  def coursesCollection: JSONCollection = db.collection[JSONCollection]("courses")
//
//  val newCourseForm = Form(
//    mapping(
//      "firstName" -> nonEmptyText,
//      "surname" -> nonEmptyText
//    )(Course.apply)(Course.unapply)
//  )
//
//  def createCourse= Action.async { implicit request =>
//    newCourseForm.bindFromRequest.fold(
//      errors => Future(Ok("You right cocked that one up son")),
//      course => {
//        coursesCollection.insert(course)
//        Future(Ok("You've just created a course well done you!!"))
//      }
//    )
//  }
//
//}