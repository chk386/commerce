package com.nhn.commerce

import com.nhn.commerce.tables.pojos.Member
import com.nhn.commerce.tables.references.MEMBER
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.reactive.asFlow
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
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
                                "gAAAAABlRFhaqvBtXXgazpDbPs87makk0XKMZS3Fb96KlJyZXus0xyslpEj9qgpF-sFMPc0AVtBNV5LBiA_iI2oov0BHaH4_nW-c1ij0WbJif7EgQx2lOpJfqeZcFuCC_bBKvADEHTvUW-n4hrDNgHp2KQp-aaq6CJgjmoAqgAAcr6UIcOrCIYo"

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

private fun fetchAll(dslContext: DSLContext): Flux<String> {
    val now = LocalDateTime.now()
    val headers = Flux.just("회원번호,이름,타입,가입일")

    val body = Flux.from(
        dslContext.select(MEMBER).from(MEMBER),
    ).map {
        with(it.into(Member::class.java)) {
            "$memberNo,$name,$type,${createdAt}\n"
        }
    }
    return headers.mergeWith(body)
}

//        .index()
//        .groupBy {
//            // 1부터 100백만건 까지 1
//            // 100백1부터 1999999까지 2
//        }
//        .map {
//            val member = it.t2.into(Member::class.java)
//
//        }
//        .doOnNext {
// //            println(it)
//        }
//        .doOnComplete {
//            println(now.until(LocalDateTime.now(), ChronoUnit.SECONDS))
//        }
