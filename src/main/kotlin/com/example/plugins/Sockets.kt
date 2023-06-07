package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*
import kotlinx.coroutines.*

fun Application.configureSockets() {
    install(CallLogging)
    val logger = LoggerFactory.getLogger("MyLogger")
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(100)
        timeout = Duration.ofSeconds(100)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
        webSocket("/") {
            println("Adding user!")
            val thisConnection = Connection(this)
            connections += thisConnection
            try {
                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                val receivedText = frame.readText()
                                val textWithUsername = "[${thisConnection.name}]: $receivedText"
                                logger.info(receivedText)
                                connections.forEach {
                                    it.session.send(textWithUsername)
                                }
                            }

                            else -> {
                                logger.info("Received a frame that is not text")
                            }
                        }
                    }
            }catch (e: Exception) {
                println(e.localizedMessage)
            }finally {
                connections -= thisConnection
            }
        }
    }
}
