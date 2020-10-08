package com.github.artemkorsakov.kafka.props

import java.util.Properties

import org.apache.kafka.clients.producer.{ KafkaProducer, ProducerConfig }

object KafkaProperties {
  def createKafkaProducer(brokers: String): KafkaProducer[String, String] = {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    new KafkaProducer[String, String](props)
  }
}
