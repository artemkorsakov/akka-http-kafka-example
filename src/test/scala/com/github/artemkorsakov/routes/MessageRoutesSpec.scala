package com.github.artemkorsakov.routes

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.{ ActorRef, ActorSystem }
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.github.artemkorsakov.kafka.msg.{ Message, MessageRegistry }
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }

class MessageRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest {
  lazy val testKit: ActorTestKit                           = ActorTestKit()
  implicit def typedSystem: ActorSystem[Nothing]           = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem = testKit.system.classicSystem

  val messageRegistry: ActorRef[MessageRegistry.Command] = testKit.spawn(MessageRegistry())
  lazy val routes: Route                                 = new MessageRoutes(messageRegistry).messageRoutes

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import com.github.artemkorsakov.formats.JsonFormats._

  "MessageRoutes" should {
    "return no messages if no present (GET /kafka)" in {
      val request = HttpRequest(uri = "/kafka")
      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"messages":{}}""")
      }
    }

    "be able to send a message (POST /kafka/send)" in {
      val message1            = Message("my_key1", "my_value1")
      val messageEntity1      = Marshal(message1).to[MessageEntity].futureValue
      val requestPostMessage1 = Post("/kafka/send/my_topic1").withEntity(messageEntity1)
      requestPostMessage1 ~> routes ~> check {
        status should ===(StatusCodes.Created)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===(
          """{"description":"Message (key=my_key1, value=my_value1) sent to topic my_topic1."}"""
        )
      }
      var requestGetMessages = HttpRequest(uri = "/kafka")
      requestGetMessages ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"messages":{"my_topic1":[{"key":"my_key1","value":"my_value1"}]}}""")
      }

      val message2            = Message("my_key2", "my_value2")
      val messageEntity2      = Marshal(message2).to[MessageEntity].futureValue
      val requestPostMessage2 = Post("/kafka/send/my_topic1").withEntity(messageEntity2)
      requestPostMessage2 ~> routes ~> check {
        status should ===(StatusCodes.Created)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===(
          """{"description":"Message (key=my_key2, value=my_value2) sent to topic my_topic1."}"""
        )
      }
      requestGetMessages = HttpRequest(uri = "/kafka")
      requestGetMessages ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===(
          """{"messages":{"my_topic1":[{"key":"my_key1","value":"my_value1"},{"key":"my_key2","value":"my_value2"}]}}"""
        )
      }

      val message3            = Message("my_key3", "my_value3")
      val messageEntity3      = Marshal(message3).to[MessageEntity].futureValue
      val requestPostMessage3 = Post("/kafka/send/my_topic2").withEntity(messageEntity3)
      requestPostMessage3 ~> routes ~> check {
        status should ===(StatusCodes.Created)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===(
          """{"description":"Message (key=my_key3, value=my_value3) sent to topic my_topic2."}"""
        )
      }
      requestGetMessages = HttpRequest(uri = "/kafka")
      requestGetMessages ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===(
          """{"messages":{"my_topic1":[{"key":"my_key1","value":"my_value1"},{"key":"my_key2","value":"my_value2"}],"my_topic2":[{"key":"my_key3","value":"my_value3"}]}}"""
        )
      }

      val message4            = Message("my_key4", "my_value4")
      val messageEntity4      = Marshal(message4).to[MessageEntity].futureValue
      val requestPostMessage4 = Post("/kafka/send/my_topic3").withEntity(messageEntity4)
      requestPostMessage4 ~> routes ~> check {
        status should ===(StatusCodes.Created)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===(
          """{"description":"Message (key=my_key4, value=my_value4) sent to topic my_topic3."}"""
        )
      }
      requestGetMessages = HttpRequest(uri = "/kafka")
      requestGetMessages ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===(
          """{"messages":{"my_topic1":[{"key":"my_key1","value":"my_value1"},{"key":"my_key2","value":"my_value2"}],"my_topic2":[{"key":"my_key3","value":"my_value3"}],"my_topic3":[{"key":"my_key4","value":"my_value4"}]}}"""
        )
      }
    }

    "impossible to send a message if the topic is not specified (POST /kafka/send)" in {
      val message            = Message("my_key1", "my_value1")
      val messageEntity      = Marshal(message).to[MessageEntity].futureValue
      val requestPostMessage = Post("/kafka/send/").withEntity(messageEntity)
      requestPostMessage ~> routes ~> runRoute
      val request = HttpRequest(uri = "/kafka")
      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"messages":{}}""")
      }
    }
  }

}
