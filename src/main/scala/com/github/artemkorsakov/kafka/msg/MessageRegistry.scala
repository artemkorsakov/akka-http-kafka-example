package com.github.artemkorsakov.kafka.msg

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }
import com.github.artemkorsakov.kafka.props.KafkaProperties.createKafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

import scala.collection.immutable

final case class Message(key: String, value: String)
final case class Messages(messages: immutable.Map[String, immutable.Seq[Message]])

object MessageRegistry {
  sealed trait Command
  final case class GetMessages(replyTo: ActorRef[Messages])                                           extends Command
  final case class CreateMessage(topic: String, message: Message, replyTo: ActorRef[ActionPerformed]) extends Command

  final case class ActionPerformed(description: String)

  private var bootstrapServer: Option[String] = None

  def apply(): Behavior[Command] = apply("")
  def apply(server: String): Behavior[Command] = {
    bootstrapServer = Some(server)
    registry(Map.empty)
  }

  private def registry(messages: Map[String, Seq[Message]]): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetMessages(replyTo) =>
        replyTo ! Messages(messages)
        Behaviors.same
      case CreateMessage(topic, message, replyTo) =>
        replyTo ! ActionPerformed(s"Message (key=${message.key}, value=${message.value}) sent to topic $topic.")
        sendToKafka(topic, message)
        val seq = messages.getOrElse(topic, Seq.empty) :+ message
        registry(messages + (topic -> seq))
    }

  private def sendToKafka(topic: String, message: Message): Unit =
    if (bootstrapServer.getOrElse("").trim.nonEmpty) {
      val producer = createKafkaProducer(bootstrapServer.get)
      val record   = new ProducerRecord(topic, message.key, message.value)
      producer.send(record)
      producer.close()
    }

}
