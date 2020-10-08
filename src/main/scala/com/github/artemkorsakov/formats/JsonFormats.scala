package com.github.artemkorsakov.formats

import com.github.artemkorsakov.kafka.msg.MessageRegistry.ActionPerformed
import com.github.artemkorsakov.kafka.msg.{ Message, Messages }
import spray.json.{ DefaultJsonProtocol, RootJsonFormat }

object JsonFormats {

  import DefaultJsonProtocol._

  implicit val messageJsonFormat: RootJsonFormat[Message]   = jsonFormat4(Message)
  implicit val messagesJsonFormat: RootJsonFormat[Messages] = jsonFormat1(Messages)

  implicit val actionPerformedJsonFormat: RootJsonFormat[ActionPerformed] = jsonFormat1(ActionPerformed)
}
