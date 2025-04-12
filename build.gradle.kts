import com.vanniktech.maven.publish.SonatypeHost
import java.util.Calendar

plugins {
    kotlin("jvm") version "2.1.20"
    id("maven-publish")
    id("signing")
    id("com.vanniktech.maven.publish") version "0.31.0"
}

group = project.property("project.group").toString()
version = project.property("project.version").toString()

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    api("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")

    testImplementation(kotlin("test"))
}

signing {
    useGpgCmd()
}

mavenPublishing {
    publishToMavenCentral(host = SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(
        groupId = project.property("project.group").toString(),
        artifactId = project.property("project.artifact").toString(),
        version = project.property("project.version").toString()
    )

    pom {
        name.set(project.property("project.artifact").toString())
        description.set(project.property("publish.description").toString())
        inceptionYear.set(Calendar.getInstance().get(Calendar.YEAR).toString())
        url.set("https://github.com/NelminDev/${project.property("project.artifact")}")
        licenses {
            license {
                name.set("GPL-3.0")
                url.set("https://opensource.org/licenses/GPL-3.0")
            }
        }
        developers {
            developer {
                id.set("nelmindev")
                name.set("Nelmin")
                email.set("me@nelmin.dev")
            }
        }
        scm {
            url.set("https://github.com/NelminDev/${project.property("project.artifact")}")
            connection.set("scm:git:git://github.com/NelminDev/${project.property("project.artifact")}.git")
            developerConnection.set("scm:git:ssh://git@github.com/NelminDev/${project.property("project.artifact")}.git")
        }
    }
}

kotlin {
    jvmToolchain(21)
}