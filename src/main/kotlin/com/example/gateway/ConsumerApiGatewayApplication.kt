package com.example.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class ConsumerApiGatewayApplication

fun main(args: Array<String>) {
	runApplication<ConsumerApiGatewayApplication>(*args)
}
