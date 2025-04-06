plugins {
    kotlin("jvm") version "2.1.10"
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.nelmin.logger"
version = project.property("project.version").toString()

repositories {
    mavenCentral()
}

dependencies {
    /*implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
    implementation("org.apache.commons:commons-compress:1.27.1")*/

    testImplementation(kotlin("test"))
}


tasks {
    shadowJar {
        archiveClassifier.set("")
    }

    jar {
        enabled = false
    }
}


publishing {
    publications {
        create<MavenPublication>("github") {
            groupId = "dev.nelmin"
            artifactId = project.name
            version = project.property("project.version").toString()

            from(components["java"])
            artifact(tasks["shadowJar"])
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/NelminDev/Lumina")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}