package controllers

import models.JsonFormats._
import models._
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


  def loginPage() = Action.async { implicit request =>
    Future(Ok(views.html.login()))
  }

  def signupPage() = Action.async { implicit request =>
    Future(Ok(views.html.signup()))
  }

  def authenticate = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => Future(BadRequest(views.html.login())),
      user => {
        val (email, password) = (user._1, user._2)
        val isAuthenticated = checkLogin(email, password)
        isAuthenticated.flatMap{auth =>
          if (auth) {Future(Redirect(routes.Application.index).withSession(Security.username -> user._1))} //We use the email for Security.username
          else {Future(Ok("Piss off!! Your login attempt has failed"))}
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
          Future(Ok("You've just created a user well done you!!"))
        }
      )
    }


  val loginForm = Form(
    tuple(
      "email" -> text,
      "password" -> text
    )
  )

  def checkLogin(email: String, password: String) = {
    UsersController.checkLoginDetails(email, password)
  }

  def logout = Action {
    Redirect(routes.AuthenticationController.loginPage()).withNewSession.flashing(
      "success" -> "You are now logged out."
    )
  }


  trait Secured {

    def email(request: RequestHeader) = request.session.get(Security.username) //We use the email for Security.username

    def onUnauthorized(request: RequestHeader) = Results.Redirect(controllers.routes.AuthenticationController.loginPage())

    def git swithAuth(f: => String => Request[AnyContent] => Result) = {
      Security.Authenticated(email, onUnauthorized) { user =>
          Action(request => f(user)(request))
      }
    }

    /**
     * This method shows how you could wrap the withAuth method to also fetch your user
     * You will need to implement UserDAO.findOneByUsername
     */
    def withUser(f: User => Request[AnyContent] => Result) = withAuth { username => implicit request =>
      val futureResult = UsersController.findByEmail(username).map { user =>
        user match {
          case Some(user) => f(user)(request)
          case None       => onUnauthorized(request)
        }
      }
      Await.result(futureResult, 5 seconds)
    }
  }
}