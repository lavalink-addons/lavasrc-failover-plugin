plugins {
    kotlin("jvm") version "2.0.0"
    `maven-publish`
    signing
}

group = "io.github.lavalink-addons"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://maven.lavalink.dev/releases")
    maven("https://maven.lavalink.dev/snapshots")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly(platform("org.springframework.boot:spring-boot-dependencies:3.2.3"))
    compileOnly("dev.arbjerg.lavalink:plugin-api:4.0.0")
    compileOnly("com.github.topi314.lavasrc:lavasrc-plugin:4.8.1")
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github.lavalink-addons"
            artifactId = "lavasrc-failover-plugin"
            version = project.version.toString()
            from(components["java"])
            pom {
                name.set("LavaSrc Failover Plugin")
                description.set("Lavalink plugin that monitors audio source health and exposes REST endpoints")
                url.set("https://github.com/lavalink-addons/lavasrc-failover-plugin")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("lavalink-addons")
                        name.set("lavalink-addons")
                        url.set("https://github.com/lavalink-addons")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/lavalink-addons/lavasrc-failover-plugin.git")
                    developerConnection.set("scm:git:ssh://git@github.com:lavalink-addons/lavasrc-failover-plugin.git")
                    url.set("https://github.com/lavalink-addons/lavasrc-failover-plugin")
                }
            }
        }
    }
    repositories {
        maven {
            name = "MavenCentral"
            url = uri("https://central.sonatype.com/api/v1/publisher/upload")
            credentials {
                username = System.getenv("MAVEN_CENTRAL_TOKEN_ID")
                password = System.getenv("MAVEN_CENTRAL_TOKEN_SECRET")
            }
        }
    }
}

signing {
    val gpgKey = System.getenv("GPG_PRIVATE_KEY")
    val gpgPass = System.getenv("GPG_PASSPHRASE")
    if (gpgKey != null && gpgPass != null) {
        useInMemoryPgpKeys(gpgKey, gpgPass)
        sign(publishing.publications["maven"])
    }
}

tasks.jar {
    archiveBaseName.set("lavasrc-failover-plugin")
    archiveVersion.set(version.toString())
}
