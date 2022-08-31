plugins {
    id("org.jetbrains.kotlin.jvm") version("1.7.10")
    id("org.jetbrains.intellij") version("1.8.0")
    id("java-library")
}

intellij {
    version.set("2022.1")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    testImplementation(kotlin("test"))
}

tasks {
    wrapper {
        gradleVersion = "7.5.1"
    }
}
