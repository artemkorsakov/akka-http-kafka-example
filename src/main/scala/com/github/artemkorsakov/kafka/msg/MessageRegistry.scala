package com.github.artemkorsakov.kafka.msg

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }

import scala.collection.immutable

final case class Message(key: String, value: String)
final case class Messages(messages: immutable.Map[String, immutable.Seq[Message]])

object MessageRegistry {
  sealed trait Command
  final case class GetMessages(replyTo: ActorRef[Messages])                                           extends Command
  final case class CreateMessage(topic: String, message: Message, replyTo: ActorRef[ActionPerformed]) extends Command

  final case class ActionPerformed(description: String)

  def apply(): Behavior[Command] = registry(Map.empty)

  private def registry(messages: Map[String, Seq[Message]]): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetMessages(replyTo) =>
        replyTo ! Messages(messages)
        Behaviors.same
      case CreateMessage(topic, message, replyTo) =>
        replyTo ! ActionPerformed(s"Message (key=${message.key}, value=${message.value}) sent to topic $topic.")
        val seq = messages.getOrElse(topic, Seq.empty) :+ message
        registry(messages + (topic -> seq))
    }

}
