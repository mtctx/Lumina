/*
 *     Lumina: build.gradle.kts
 *     Copyright (C) 2025 mtctx
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import dev.mtctx.unipub.License

plugins {
    kotlin("jvm") version "2.1.20"
    id("org.jetbrains.dokka") version "2.1.0-Beta"
    id("dev.mtctx.unipub") version "1.0.9"
}

group = "dev.mtctx.logger"
version = "3.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    api("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
    api("com.squareup.okio:okio:3.16.0")

    testImplementation(kotlin("test"))
}

unipub {
    project {
        name = "Lumina"
        id = "lumina"
        description = "A Modular, Fast and easy to use Kotlin Logger"
        inceptionYear = "2025"
        url = "https://github.com/mtctx/Lumina"

        licenses = listOf(License.GPL_V3)

        scm {
            url = "https://github.com/mtctx/Lumina"
            connection = "scm:git:git@github.com:mtctx/Lumina.git"
            developerConnection = "scm:git:ssh://git@github.com:mtctx/Lumina.git"
        }
    }

    developers {
        developer {
            name = "mtctx"
            email = "me@mtctx.dev"
        }
    }
}

dokka {
    dokkaPublications.html {
        outputDirectory.set(layout.buildDirectory.dir("dokka"))
    }

    dokkaSourceSets.configureEach {
        jdkVersion.set(21)
        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(uri("https://github.com/mtctx/UniPub/"))
            remoteLineSuffix.set("#L")
        }
    }
}

kotlin {
    jvmToolchain(21)
}