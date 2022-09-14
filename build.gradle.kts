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

    // JUnit3 (junit-vintage) is required for running IDEA platform tests.
    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
}

tasks {
    wrapper {
        gradleVersion = "7.5.1"
    }
    test {
        useJUnitPlatform()
    }
}

intellij {
    // Even though this is not an IDEA plugin project, this is required to
    // resolve the platform APIs used by this library.
    val platformVersion: String by project  // see gradle.properties
    version.set(platformVersion)
    type.set("IC")  // IntelliJ Community
}
