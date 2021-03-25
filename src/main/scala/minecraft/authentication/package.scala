package minecraft

import zio._

package object authentication {

  type AuthenticationServer = Has[AuthenticationServer.Service]

  object AuthenticationServer {
    trait Service {
      def authenticate(username: String, password: String): IO[Error, Token]
    }
  }

  case class Token()

  case class Error(message: String)

  def authenticate(username: String, password: String): ZIO[AuthenticationServer, Error, Token] =
    ZIO.accessM(_.get[AuthenticationServer.Service].authenticate(username, password))
}
