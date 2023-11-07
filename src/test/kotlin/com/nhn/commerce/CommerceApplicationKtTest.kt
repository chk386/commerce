package com.nhn.commerce

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux

class CommerceApplicationKtTest {
    @Test
    fun reactor_test() {
        val fistHeader = Flux.just(0)
        val just = Flux.just(1, 2, 3, 4)

        Flux.merge(fistHeader, just)
            .log()
            .subscribe()
    }
}
