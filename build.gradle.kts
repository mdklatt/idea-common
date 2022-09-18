plugins {
    id("org.jetbrains.kotlin.jvm") version("1.7.10")
    id("org.jetbrains.intellij") version("1.9.0")
    id("java-library")
    id("maven-publish")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    testImplementation(kotlin("test"))

    // JUnit3 is required for running IDEA platform tests.
    testImplementation(platform("org.junit:junit-bom:5.9.0"))
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
}

tasks {
    wrapper {
        gradleVersion = "7.5.1"
    }
}

intellij {
    // Even though this is not an IDEA plugin project, this is required to
    // resolve the platform dependencies used by this library.
    val platformVersion: String by project  // see gradle.properties
    version.set(platformVersion)
    type.set("IC")  // IntelliJ Community
}

publishing {
    publications {
        create<MavenPublication>("main") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/mdklatt/idea-common/")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
