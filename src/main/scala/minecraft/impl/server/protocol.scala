package minecraft.impl.server

import java.nio.charset.StandardCharsets

import io.circe.Json
import io.circe.syntax._
import io.circe.parser.decode
import io.circe.generic.auto._
import scodec._

object protocol {

  import codecs.{
    vint => varInt, // TODO: There is a bug in scodec xD it should be vintL. It is fixed in 2.0.0.
    uint16 => uShort,
  }

  val string: Codec[String] = codecs.variableSizeBytes(varInt, codecs.string(StandardCharsets.UTF_8))

  case class Packet[+A](id: Int, data: A)

  implicit def packetCodec[A](implicit a: Codec[A]): Codec[Packet[A]] = codecs.variableSizeBytes(varInt, varInt :: a).as[Packet[A]]

  case class Handshake(protocolVersion: Int,
                       serverAddress: String,
                       serverPort: Int,
                       nextState: Int,
                      )

  implicit val handshakeCodec: Codec[Handshake] = (varInt :: string :: uShort :: varInt).as[Handshake]

  case object StatusRequest

  implicit val statusRequestCodec: Codec[StatusRequest.type] = codecs.provide(StatusRequest)

  case class StatusResponse(version: StatusResponse.Version,
                            players: StatusResponse.Players,
                            description: Json,
                            favicon: String,
                           )

  object StatusResponse {

    case class Version(name: String, protocol: Int)

    case class Players(max: Int, online: Int, sample: Option[Vector[Player]])

    case class Player(name: String, id: String)
  }

  implicit val statusResponseCodec: Codec[StatusResponse] = string.exmap(
    string => Attempt.fromEither(decode[StatusResponse](string).left.map(error => Err(error.getMessage()))),
    statusResponse => Attempt.successful(statusResponse.asJson.noSpaces),
  )
}
