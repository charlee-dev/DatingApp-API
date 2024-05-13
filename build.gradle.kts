import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.graalvm.buildtools.gradle.tasks.BuildNativeImageTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    kotlin("jvm") version libs.versions.kotlin.get()
    id("io.ktor.plugin") version libs.versions.ktor.get()
    id("com.github.johnrengelman.shadow") version libs.versions.shadow.get()
    id("org.jetbrains.kotlinx.kover") version libs.versions.kover.get()
    alias(libs.plugins.ktLint)
    alias(libs.plugins.detekt)
    id("org.graalvm.buildtools.native") version "0.9.19"
}

group = "com.ajlabs.forevely"
version = "0.0.1"

application {
    mainClass.set("$group.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.websocket)

    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.client.content)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.serialization.json)

    implementation(libs.expedia.server)
    implementation(libs.expedia.ktor.server)
    implementation(libs.expedia.federation)
    implementation(libs.graphql.scalars)
    implementation(libs.javalin.core)

    implementation(libs.koin.ktor)
    implementation(libs.server.bcrypt)
    implementation(libs.server.logback)
    implementation(libs.koin.logger)
    implementation(libs.kermit)
    implementation(libs.server.mongodb)
    implementation(libs.ktor.server.logging)
    implementation(libs.validator.libPhoneNumber)
    implementation(libs.validator.mail)

    implementation("com.google.firebase:firebase-admin:9.2.0")

    testImplementation(libs.test.kluent)
    testImplementation(libs.test.koin)
    testImplementation(libs.ktor.client.content)
    testImplementation(libs.ktor.client.websockets)
    testImplementation(libs.test.kotlin.ktor.server)
    testImplementation(libs.test.kotlin.ktor.serverHost)
    testImplementation(libs.test.testContainers.mongo)
    testImplementation(libs.test.testContainers.junit)
    testImplementation(libs.test.junit.jupiter.api)
    testRuntimeOnly(libs.test.junit.jupiter.engine)
    testRuntimeOnly(libs.test.junit.jupiter.params)
    testImplementation(libs.test.mockk)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}

configure<KtlintExtension> {
    filter {
        exclude { element -> element.file.path.contains("/build/") }
    }
    debug.set(false)
    outputToConsole.set(true)
}

detekt {
    parallel = true
    config.setFrom(files(rootProject.file("detekt.yml")))
    autoCorrect = true
}

tasks {
    withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = libs.versions.jvmTarget.get()
        compilerOptions.freeCompilerArgs.add("-Xcontext-receivers")
    }
    withType<Test> {
        useJUnitPlatform()
        setEnvironment("MONGO_URI" to "mongodb://localhost:27017/")
    }
    withType<Jar> {
        manifest {
            attributes(
                mapOf(
                    "Main-Class" to application.mainClass.get(),
                ),
            )
        }
    }
    withType<Detekt>().configureEach {
        jvmTarget = libs.versions.jvmTarget.get()
        parallel = true
        reports {
            xml.required.set(false)
            html.required.set(false)
            txt.required.set(false)
            sarif.required.set(false)
        }
        exclude { it.file.absolutePath.contains("resources/") }
        exclude { it.file.absolutePath.contains("build/") }
        include("**/*.kt")

        dependsOn("ktlintFormat")
    }
    withType<DetektCreateBaselineTask>().configureEach {
        this.jvmTarget = libs.versions.jvmTarget.get()
        exclude { it.file.absolutePath.contains("resources/") }
        exclude { it.file.absolutePath.contains("build/") }
        include("**/*.kt")
    }

    create("stage")
        .dependsOn("installShadowDist")

    withType<BuildNativeImageTask>().configureEach {
        disableToolchainDetection.set(true)
    }
}

graalvmNative {
    binaries {
        named("main") {
            fallback.set(false)
            verbose.set(true)

            buildArgs.add("--initialize-at-build-time=ch.qos.logback")
            buildArgs.add("--initialize-at-build-time=io.ktor,kotlin")
            buildArgs.add("--initialize-at-build-time=org.slf4j.LoggerFactory")

            buildArgs.add("-H:+InstallExitHandlers")
            buildArgs.add("-H:+ReportUnsupportedElementsAtRuntime")
            buildArgs.add("-H:+ReportExceptionStackTraces")

            imageName.set("graalvm-server")
        }

        named("test") {
            fallback.set(false)
            verbose.set(true)

            buildArgs.add("--initialize-at-build-time=ch.qos.logback")
            buildArgs.add("--initialize-at-build-time=io.ktor,kotlin")
            buildArgs.add("--initialize-at-build-time=org.slf4j.LoggerFactory")

            buildArgs.add("-H:+InstallExitHandlers")
            buildArgs.add("-H:+ReportUnsupportedElementsAtRuntime")
            buildArgs.add("-H:+ReportExceptionStackTraces")

            val path = "${projectDir}/src/test/resources/META-INF/native-image/"
            buildArgs.add("-H:ReflectionConfigurationFiles=${path}reflect-config.json")
            buildArgs.add("-H:ResourceConfigurationFiles=${path}resource-config.json")

            imageName.set("graalvm-test-server")
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
