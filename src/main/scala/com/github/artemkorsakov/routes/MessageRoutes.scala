package com.github.artemkorsakov.routes

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ ActorRef, ActorSystem }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.github.artemkorsakov.kafka.msg.MessageRegistry._
import com.github.artemkorsakov.kafka.msg._

import scala.concurrent.Future

class MessageRoutes(messageRegistry: ActorRef[MessageRegistry.Command])(implicit val system: ActorSystem[_]) {
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import com.github.artemkorsakov.formats.JsonFormats._

  private implicit val timeout: Timeout =
    Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getMessages: Future[Messages] =
    messageRegistry.ask(GetMessages)
  def createMessage(topic: String, message: Message): Future[ActionPerformed] =
    messageRegistry.ask(CreateMessage(topic, message, _))

  val messageRoutes: Route =
    pathPrefix("kafka") {
      concat(
        pathEnd {
          concat(
            get {
              complete(getMessages)
            }
          )
        },
        pathPrefix("send") {
          path(Segment) { topic =>
            concat(
              post {
                entity(as[Message]) { message =>
                  onSuccess(createMessage(topic, message)) { performed =>
                    complete((StatusCodes.Created, performed))
                  }
                }
              }
            )
          }
        }
      )
    }
}
