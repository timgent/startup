package controllers

import authentication.{Permission, TimAuthenticationModule}
import models.JsonFormats._
import models._
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util._

// Reactive Mongo imports

// Reactive Mongo plugin, including the JSON-specialized collection
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

object AuthenticationController extends Controller with MongoController {

  def usersCollection: JSONCollection = db.collection[JSONCollection]("users")


  def loginPage(uri: String = "/") = Action.async { implicit request =>
    Future(Ok(views.html.login(uri)))
  }

  def signupPage() = Action.async { implicit request =>
    Future(Ok(views.html.signup()))
  }

  def authenticate = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => {
        Future(BadRequest(views.html.login(request.uri)))
      },
      user => {
        val (email, password, url) = (user._1, user._2, user._3)
        val isAuthenticated = {
          checkLogin(email, password)
        }
        val redirectLocation = routes.Application.index

        isAuthenticated.flatMap{auth =>
          if (auth) {Future(Redirect(url).withSession(Security.username -> user._1))}
          else {Future(Redirect(controllers.routes.AuthenticationController.loginPage(url)).flashing("login" -> "Your email address or password are incorrect, please try again"))}
        }
      }
    )
  }

  val createLoginForm = Form(
    tuple(
      "username" -> nonEmptyText,
      "email" -> nonEmptyText,
      "password" -> nonEmptyText,
      "passwordRepeat" -> nonEmptyText
    ) verifying ("Password and password repeat must match", result => result match{
      case (username, email, password, passwordRepeat) => password == passwordRepeat
    })
  )

    def createLogin() = Action.async { implicit request =>
      createLoginForm.bindFromRequest.fold(
        errors => Future(Ok("You right cocked that one up son")),
        user => {
          val newUser = User(user._1, user._2, user._3)
          usersCollection.insert(newUser)
          Future(Ok(views.html.index()))
        }
      )
    }


  val loginForm = Form(
    tuple(
      "email" -> text,
      "password" -> text,
      "url" -> text
    )
  )

  def checkLogin(email: String, password: String) = {
    UsersController.checkLoginDetails(email, password)
  }

  def logout = Action {
    Redirect(routes.AuthenticationController.loginPage(routes.Application.index().url)).withNewSession.flashing(
      "success" -> "You are now logged out."
    )
  }


  trait TimSecured extends TimAuthenticationModule[User] {
    override def whenNotLoggedInDoThis(request: RequestHeader) = {
//      implicit val flash = Flash(Map("login" -> "Please login to access all of our features"))
      Redirect(controllers.routes.AuthenticationController.loginPage(request.uri)).flashing("login" -> "Please login to access all of our features")
//      Ok(views.html.login(Some(request.uri)))
    }
    override def getUser(username: String): Option[User] = Await.result(UsersController.findByEmail(username), 5 seconds)
    override def whenNoPermissionDoThis(request: RequestHeader) = Ok("Access denied")
    override def userHasPermission(permission: Permission, user: User) = user.email == "tim.gent@gmail.com" || permission != CourseAdministration
  }


}

//Permissions
object CourseAdministration extends Permission {val displayName = "Course Administrator"}
object CourseAccess extends Permission {val displayName = "Course Access"}