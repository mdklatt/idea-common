group = "dev.mdklatt"
version = "0.1.0.dev0"

plugins {
    id("org.jetbrains.kotlin.jvm") version("1.7.10")
    id("org.jetbrains.intellij") version("1.8.0")
    id("java-library")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    testImplementation(kotlin("test"))
    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
}

intellij {
    type.set("IC")
    version.set("2022.1")
}

tasks {
    wrapper {
        gradleVersion = "7.5.1"
    }

    test {
        useJUnitPlatform()
    }
}
