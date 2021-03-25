package minecraft.impl

import minecraft.impl.server.protocol._
import minecraft.server._
import scodec.bits.BitVector
import scodec.{Attempt, Decoder, Encoder, Err}
import zio._
import zio.nio.channels.AsynchronousSocketChannel
import zio.nio.core.SocketAddress

package object server {

  val javaEdition: ULayer[Server] = ZLayer.succeed {
    new Server.Service {
      override def fetchStatus(host: String, port: Int): Task[Status] =
        socket(host, port)
          .use(statusProtocol(host, port).provide)
          .map { statusResponse =>
            println(statusResponse.description.spaces2)
            Status(
              playerCount = statusResponse.players.online,
              maxPlayerCount = statusResponse.players.max,
            )
          }

      private def statusProtocol(host: String, port: Int) = for {
        _ <- writePacket(Packet(
          id = 0,
          data = Handshake(
            protocolVersion = -1,
            serverAddress = host,
            serverPort = port,
            nextState = 1,
          )
        ))(packetCodec(handshakeCodec))
        _ <- writePacket(Packet(
          id = 0,
          data = StatusRequest,
        ))(packetCodec(statusRequestCodec))
        packet <- readPacket[StatusResponse]()(packetCodec(statusResponseCodec))
      } yield packet.data

      private def writePacket[A](packet: Packet[A])(implicit encoder: Encoder[Packet[A]]) = for {
        bits <- ZIO.fromAttempt(encoder.encode(packet))
          .mapError(adaptErr)
        chunk = Chunk.fromArray(bits.toByteArray)
        _ <- ZIO.accessM[AsynchronousSocketChannel](_.write(chunk))
      } yield ()

      private def readPacket[A]()(implicit decoder: Decoder[Packet[A]]) = for {
        // TODO: In theory the server can send larger packets but it should not happen during handshake and status check.
        chunk <- ZIO.accessM[AsynchronousSocketChannel](_.read(Short.MaxValue))
        // TODO: The correct implementation should check for incomplete input.
        packet <- ZIO.fromAttempt(decoder.complete.decodeValue(BitVector(chunk.toArray)))
          .mapError(adaptErr)
      } yield packet

      private def adaptErr(err: Err) = new RuntimeException(err.message)

      private def socket(host: String, port: Int) =
        AsynchronousSocketChannel().mapM { socket =>
          for {
            address <- SocketAddress.inetSocketAddress(host, port)
            _ <- socket.connect(address)
          } yield socket
        }
    }
  }

  private implicit class ZIOOps(val zio: ZIO.type) extends AnyVal {

    def fromAttempt[A](attempt: Attempt[A]): IO[Err, A] = attempt.fold(ZIO.fail(_), ZIO.succeed(_))
  }
}
