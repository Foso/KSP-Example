plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
}

version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}


kotlin {
    jvm {
        withJava()

    }
    js(IR) {
        browser()
        nodejs()
    }
    linuxX64() {
        binaries {
            executable()
        }
    }

    sourceSets {
        val commonMain by getting
        val linuxX64Main by getting
        val linuxX64Test by getting
        val jvmMain by getting {
           // kotlin.srcDir("build/generated/ksp/jvm/jvmMain/")
            
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", project(":test-processor"))
    add("kspJvm", project(":test-processor"))
    add("kspJvmTest", project(":test-processor"))
    add("kspJs", project(":test-processor"))
    add("kspJsTest", project(":test-processor"))
    add("kspLinuxX64", project(":test-processor"))
    add("kspLinuxX64Test", project(":test-processor"))

    // The universal "ksp" configuration has performance issue and is deprecated on multiplatform since 1.0.1
    // ksp(project(":test-processor"))
}
