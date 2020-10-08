/*
package com.github.artemkorsakov.app

import java.util.Date

import com.github.artemkorsakov.kafka.props.KafkaProperties.createKafkaProducer
import kafka.utils.Logging
import org.apache.kafka.clients.producer.ProducerRecord

import scala.util.{Failure, Random, Success, Using}

object KafkaProducerApp extends App with Logging {
  if (args.length < 3)
    logger.error("Must be three arguments: server, topic and count of messages")
  else {
    val topic = args(1)
    val count = args(2).toInt

    val time = System.currentTimeMillis()
    Using(createKafkaProducer(args.head)) { producer =>
      val rnd = new Random()
      (0 to count).foreach { nEvents =>
        val key    = s"192.168.2.${rnd.nextInt(255)}"
        val value  = s"${new Date().getTime}, $nEvents, www.example.com, $key"
        val record = new ProducerRecord(topic, key, value)
        producer.send(record)
        logger.info(s"message = $value")
      }
      producer.close()
    } match {
      case Success(_) =>
        logger.info(s"sent per second: ${count * 1000 / (System.currentTimeMillis() - time)}")
      case Failure(e) =>
        logger.error(e.getMessage)
    }

  }

}

 */
