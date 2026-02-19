package com.cardra.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CardraApplication

fun main(args: Array<String>) {
  runApplication<CardraApplication>(*args)
}
