package minecraft.impl.authentication

import sttp.tapir._
import sttp.tapir.json.circe._
import io.circe.generic.auto._

object api {

  case class Agent(name: String, version: Int)

  case class AuthenticateIn(agent: Agent,
                            username: String,
                            password: String,
                            clientToken: Option[String],
                            requestUser: Option[Boolean],
                           )

  case class AuthenticateOut(accessToken: String,
                             clientToken: String,
                            )

  case class Error(error: String,
                   errorMessage: String,
                   cause: Option[String],
                  )

  val authenticate = endpoint.post
    .in("authenticate")
    .in(jsonBody[AuthenticateIn])
    .out(jsonBody[AuthenticateOut])
    .errorOut(jsonBody[Error])
}
