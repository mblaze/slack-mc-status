package minecraft.realms.status

import java.net.InetAddress

import minecraft.impl.authentication._
import minecraft.impl.server._
import minecraft.authentication._
import minecraft.server
import sttp.client.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio._

object Main extends App {

  val deps = AsyncHttpClientZioBackend.layer().orDie >>> mojang

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    server.fetchStatus("play.hivemc.com", 25565)
      .provideLayer(javaEdition)
//    authenticate("email", "pass")
//      .provideLayer(deps)
      .foldM(
        error => console.putStr(error.getMessage).as(1),
        result => console.putStr(result.toString).as(0)
      )
}
