package com.nhn.commerce

import com.nhn.commerce.tables.references.MEMBER
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.reactive.asFlow
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.data.annotation.Id
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.coRouter
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@SpringBootApplication
class CommerceApplication

fun main(args: Array<String>) {
    runApplication<CommerceApplication>(*args) {
        addInitializers(
            beans {
                bean(isPrimary = true) {
                    DSL.using(ref<ConnectionFactory>(), SQLDialect.MYSQL).dsl()
                }
                bean {
                    coRouter {
                        GET("/") {
                            val dslContext = ref<DSLContext>()
                            val members = fetchAll(dslContext)

                            val token =
                                "gAAAAABlQydnbNX1t7kajC6q3WuGa9NFqfU9HSY-jt-4Don_cL0KjrYw_M3KMWjP2LYKTWj8ba0vED2gPcoLeg0giAYotMoEztiYQeOovHDf3CcBJlrgnPx19tL_tOZ31y-jGslYzYZDQUhK4WQMRig7SuvLqKRPAs0sfvRR_mxBl88W2wQVwng"
                            val uri =
                                "https://kr1-api-object-storage.nhncloudservice.com/v1/AUTH_69db659103894b00aa9f8b28aa62fe8e/paycomall/dummy.csv"

                            val response: Mono<String> =
                                WebClient.create("https://kr1-api-object-storage.nhncloudservice.com")
                                    .put()
                                    .uri("/v1/AUTH_69db659103894b00aa9f8b28aa62fe8e/paycomall/dummy.csv")
                                    .header("X-Auth-Token", token)
                                    .body(members, String::class.java)
                                    .retrieve()
                                    .bodyToMono(String::class.java)

                            ServerResponse.ok().contentType(MediaType.TEXT_EVENT_STREAM)
                                .bodyAndAwait(response.asFlow())
                        }
                    }
                }
            },
        )
    }
}

private fun fetchAll(dslContext: DSLContext): Flux<String> = Flux.from(
    dslContext.select(MEMBER).from(MEMBER),
).doFirst {
    val runtime = Runtime.getRuntime()
    val total = runtime.totalMemory()
    val max = runtime.maxMemory()
    val free = runtime.freeMemory()

    println("total : $total max : $max free : $free")
    println(LocalDateTime.now())
}
//    .delayElements(Duration.ofMillis(100))
    .map {
        val member = it.into(Member::class.java)

        "${member.memberNo},${member.name},${member.type},${member.createdAt}\n"
    }
    .doOnNext {
        println(it)
    }
    .doOnComplete { println(LocalDateTime.now()) }

data class Member(
    @Id var memberNo: Int? = null,
    var name: String? = null,
    var type: String? = null,
    var createdAt: LocalDateTime = LocalDateTime.now(),
)
