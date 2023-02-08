plugins {
    kotlin("multiplatform") apply false
}

repositories {
    mavenCentral()
}

subprojects {
    repositories {
        mavenCentral()
        jcenter()
    }

    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
    }
}
