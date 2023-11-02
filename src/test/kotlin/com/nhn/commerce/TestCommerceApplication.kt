package com.nhn.commerce

import org.springframework.boot.devtools.restart.RestartScope
import org.springframework.boot.fromApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.with
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestCommerceApplication {
    @Bean
    @ServiceConnection
    @RestartScope
    fun mysqlContainer(): MySQLContainer<*> = MySQLContainer(DockerImageName.parse("mysql:latest"))
        .withUsername("test")
        .withPassword("test")
        .withDatabaseName("test")
}

fun main(args: Array<String>) {
    fromApplication<CommerceApplication>()
        .with(TestCommerceApplication::class)
        .run(*args)
}
