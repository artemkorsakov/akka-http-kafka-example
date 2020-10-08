package com.github.artemkorsakov.app

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.github.artemkorsakov.kafka.msg.MessageRegistry
import com.github.artemkorsakov.routes.MessageRoutes

import scala.util.{ Failure, Success }

object QuickstartApp {
  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    import system.executionContext

    val interface = system.settings.config.getString("my-app.interface")
    val port      = system.settings.config.getInt("my-app.port")

    val futureBinding = Http().newServerAt(interface, port).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }
  def main(args: Array[String]): Unit = {
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      val bootstrapServer      = context.system.settings.config.getString("my-app.bootstrap-server")
      val messageRegistryActor = context.spawn(MessageRegistry(bootstrapServer), "MessageRegistryActor")
      context.watch(messageRegistryActor)

      val routes = new MessageRoutes(messageRegistryActor)(context.system)
      startHttpServer(routes.messageRoutes)(context.system)

      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "HelloAkkaHttpServer")
  }
}
