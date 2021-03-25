package minecraft.impl

import minecraft.authentication.{AuthenticationServer, Error, Token}
import minecraft.impl.authentication.api.AuthenticateIn
import sttp.client._
import sttp.client.asynchttpclient.zio.SttpClient
import sttp.model.Uri.UriContext
import sttp.tapir._
import sttp.tapir.client.sttp._
import zio._

package object authentication {

  val mojang: URLayer[SttpClient, AuthenticationServer] = ZLayer.fromService { implicit backend =>
    new AuthenticationServer.Service {

      private val baseUri = uri"https://authserver.mojang.com"

      override def authenticate(username: String, password: String): IO[Error, Token] = for {
        authenticateOut <- handleResponse(api.authenticate.toSttpRequest(baseUri).apply(prepareAuthenticateIn(username, password)).send())
        _ <- ZIO.effect(println(authenticateOut)).orDie
      } yield Token()

      private def prepareAuthenticateIn(username: String, password: String): AuthenticateIn =
        api.AuthenticateIn(
          agent = api.Agent(
            name = "Minecraft",
            version = 1,
          ),
          username,
          password,
          clientToken = None,
          requestUser = Some(false),
        )

      // TODO
      private def handleResponse[A](response: Task[Response[DecodeResult[Either[api.Error, A]]]]): IO[Error, A] =
        response.foldM(
          throwable => ZIO.fail(Error(throwable.getMessage)),
          response => response.body match {
            case DecodeResult.Value(Right(a)) => ZIO.succeed(a)
            case DecodeResult.Value(Left(error)) => ZIO.fail(Error(error.errorMessage))
            case _ => ZIO.fail(Error("Bad response"))
          }
        )
    }
  }
}
