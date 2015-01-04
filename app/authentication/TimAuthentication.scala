package authentication

import controllers.UsersController
import models.User
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.duration._
import scala.concurrent.{Future, Await}
import play.api.libs.concurrent.Execution.Implicits.defaultContext


trait TimAuthenticationModule[userType] {


  //Controller Authentication methods
  def timAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(getSessionUsername, whenNotLoggedInDoThis) { user =>
      Action(request => f(user)(request))
    }
  }
  
  private def timAuthWithOptionalPermission(permissionNeeded: Option[Permission] = None)(f: => userType => Request[AnyContent] => Result): EssentialAction = timAuth { username => implicit request =>
    val user = getUser(username)
    if (user.nonEmpty) {
      if (permissionNeeded.isEmpty || userHasPermission(permissionNeeded.get, user.get)) {
        f(user.get)(request)
      } else {
        whenNoPermissionDoThis(request)
      }
    } else whenNotLoggedInDoThis(request)
  }

  def timAuthWithPermission(permissionNeeded: Permission)(f: => userType => Request[AnyContent] => Result) = {
    timAuthWithOptionalPermission(Some(permissionNeeded))(f)
  }

  def timAuthWithUser(f: => userType => Request[AnyContent] => Result) = {
    timAuthWithOptionalPermission(None)(f)
  }



  //General helper methods
  def getSessionUsername(request: RequestHeader) = request.session.get(Security.username)
  
  //Methods that will need to be implemented
  def whenNotLoggedInDoThis(request: RequestHeader): Result
  def whenNoPermissionDoThis(request: RequestHeader): Result
  def getUser(username: String): Option[userType]
  def userHasPermission(permission: Permission, user: userType): Boolean
}

trait Permission {val displayName: String}
