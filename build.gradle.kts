import kotlinx.benchmark.gradle.JvmBenchmarkTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform") version "2.1.10"
    kotlin("plugin.allopen") version "2.0.20"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.13"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)

    jvm()

    macosArm64()
    macosX64()
    linuxX64()
    linuxArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { nodejs() }
    js(IR) { nodejs() }

    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.13")
            }
        }
    }
}

benchmark {
    targets {
        register("jvm") {
            this as JvmBenchmarkTarget
            jmhVersion = "1.37"
        }
        register("macosArm64")
        register("macosX64")
        register("linuxX64")
        register("linuxArm64")
        register("js")
        register("wasmJs")
    }

    configurations {
        named("main") {
            advanced("jvmForks", 2)
        }
    }
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}