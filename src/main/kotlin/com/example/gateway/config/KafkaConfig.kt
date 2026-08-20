package com.example.gateway.config

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@EnableKafka
@Configuration
class KafkaConfig {

    // Reads KAFKA_BOOTSTRAP_SERVERS from environment or application.yml (Defaults to localhost:9092)
    @Value("\${spring.kafka.bootstrap-servers:eventhubs-emulator:9092}")
    private lateinit var bootstrapServers: String

    private fun getJaasConfig(): String {
        val hostname = bootstrapServers.split(":")[0]
        val connectionString = "Endpoint=sb://$hostname;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;"
        return "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"\$ConnectionString\" password=\"$connectionString\";"
    }

    // Configures the low-level kafka ProducerFactory with connection details and string serializers
    @Bean
    fun producerFactory(): ProducerFactory<String, String> {
        val configProps = mapOf<String, Any>(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.name,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.name,
            CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "SASL_PLAINTEXT", // Required protocol for event hubs kafka endpoint
            SaslConfigs.SASL_MECHANISM to "PLAIN", // Required SASL mechanism
            SaslConfigs.SASL_JAAS_CONFIG to getJaasConfig() // Injects Event Hubs SASL authentication credentials
        )
        return DefaultKafkaProducerFactory(configProps)
    }

    // Registers the thread-safe KafkaTemplate<String, String> Spring bean injected into EventPublisherService
    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, String> {
        return KafkaTemplate(producerFactory())
    }

    // Configures the ConsumerFactory with deserializers and SASL_PLAINTEXT credentials
    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> {
        val configProps = mapOf<String, Any>(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to "gateway-cg",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "SASL_PLAINTEXT",
            SaslConfigs.SASL_MECHANISM to "PLAIN",
            SaslConfigs.SASL_JAAS_CONFIG to getJaasConfig()
        )
        return DefaultKafkaConsumerFactory(configProps)
    }

    // Configures ConcurrentKafkaListenerContainerFactory for @KafkaListener annotations
    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.setConsumerFactory(consumerFactory())
        return factory
    }
}