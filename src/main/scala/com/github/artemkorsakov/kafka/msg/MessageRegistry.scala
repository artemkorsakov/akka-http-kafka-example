package com.github.artemkorsakov.kafka.msg

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }
import com.github.artemkorsakov.kafka.props.KafkaProperties.createKafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

import scala.collection.immutable

final case class Message(server: Option[String], topic: String, key: String, value: String)
final case class Messages(messages: immutable.Seq[Message])

object MessageRegistry {
  sealed trait Command
  final case class GetMessages(replyTo: ActorRef[Messages])                            extends Command
  final case class CreateMessage(message: Message, replyTo: ActorRef[ActionPerformed]) extends Command

  final case class ActionPerformed(description: String)

  def apply(): Behavior[Command] = registry(Seq.empty)

  private def registry(messages: Seq[Message]): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetMessages(replyTo) =>
        replyTo ! Messages(messages)
        Behaviors.same
      case CreateMessage(message, replyTo) =>
        replyTo ! ActionPerformed(s"Message ($message) sent.")
        sendToKafka(message)
        registry(messages :+ message)
    }

  private def sendToKafka(message: Message): Unit =
    if (message.server.isDefined && message.server.get.trim.nonEmpty) {
      val producer = createKafkaProducer(message.server.get)
      val record   = new ProducerRecord(message.topic, message.key, message.value)
      producer.send(record)
      producer.close()
    }

}
