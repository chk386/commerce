import nu.studer.gradle.jooq.JooqGenerate
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jooq.meta.jaxb.Logging
import org.jooq.meta.jaxb.Property

plugins {
    id("org.springframework.boot") version "3.1.4"
    id("io.spring.dependency-management") version "1.1.3"
    id("nu.studer.jooq") version "8.2.1"

//    id("io.gitlab.arturbosch.detekt") version "1.23.1"

    kotlin("jvm") version "1.9.20"
    kotlin("plugin.spring") version "1.9.20"
}

group = "com.nhn"
version = "0.0.1-SNAPSHOT"
val jooqVersion = "3.18.6"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("io.asyncer:r2dbc-mysql")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    jooqGenerator("com.mysql:mysql-connector-j")
    jooqGenerator("org.jooq:jooq-meta-extensions:$jooqVersion")

    testImplementation("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:r2dbc")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn")
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
jooq {
    version.set(jooqVersion) // default (can be omitted)
//    edition.set(nu.studer.gradle.jooq.JooqEdition.OSS)  // default (can be omitted)

    configurations {
        create("main") {
            // name of the jOOQ configuration
            generateSchemaSourceOnCompilation.set(false)

            jooqConfiguration.apply {
                logging = Logging.WARN
                jdbc = null

                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"

                    database.apply {
                        name = "org.jooq.meta.extensions.ddl.DDLDatabase"
                        // inputSchema = "public"
                        properties.addAll(
                            listOf(
                                // Specify the location of your SQL script.
                                // You may use ant-style file matching, e.g. /path/**/to/*.sql
                                //
                                // Where:
                                // - ** matches any directory subtree
                                // - * matches any number of characters in a directory / file name
                                // - ? matches a single character in a directory / file name
                                Property().apply {
                                    key = "scripts"
                                    value = "src/main/resources/schema.sql"
                                },
                                // The sort order of the scripts within a directory, where:
                                //
                                // - semantic: sorts versions, e.g. v-3.10.0 is after v-3.9.0 (default)
                                // - alphanumeric: sorts strings, e.g. v-3.10.0 is before v-3.9.0
                                // - flyway: sorts files the same way as flyway does
                                // - none: doesn't sort directory contents after fetching them from the directory
                                Property().apply {
                                    key = "sort"
                                    value = "semantic"
                                },
                                // The default schema for unqualified objects:
                                //
                                // - public: all unqualified objects are located in the PUBLIC (upper case) schema
                                // - none: all unqualified objects are located in the default schema (default)
                                //
                                // This configuration can be overridden with the schema mapping feature
                                Property().apply {
                                    key = "unqualifiedSchema"
                                    value = "none"
                                },
                                // The default name case for unquoted objects:
                                //
                                // - as_is: unquoted object names are kept unquoted
                                // - upper: unquoted object names are turned into upper case (most databases)
                                // - lower: unquoted object names are turned into lower case (e.g. PostgreSQL)
                                Property().apply {
                                    key = "defaultNameCase"
                                    value = "upper"
                                },
                            ),
                        )
                    }

                    generate.apply {
                        isPojosAsKotlinDataClasses = true
                        isImmutablePojos = true
                    }

                    target.apply {
                        packageName = "com.nhn.commerce"
                        directory = "build/generated-src/jooq/main" // default (can be omitted)
                    }

                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}

tasks.named<JooqGenerate>("generateJooq") { allInputsDeclared.set(true) }
