package minecraft

import zio._

package object server {

  type Server = Has[Server.Service]

  object Server {
    trait Service {

      def fetchStatus(host: String, port: Int): Task[Status]
    }
  }

  case class Status(playerCount: Int,
                    maxPlayerCount: Int,
                   )

  def fetchStatus(host: String, port: Int): RIO[Server, Status] =
    ZIO.accessM(_.get.fetchStatus(host, port))
}
