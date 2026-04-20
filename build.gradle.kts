plugins {
    kotlin("jvm") version "2.0.0"
    `maven-publish`
}

group = "dev.lavalink.failover"
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

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "dev.lavalink.failover"
            artifactId = "lavasrc-failover-plugin"
            version = project.version.toString()
            from(components["java"])
            pom {
                name.set("LavaSrc Failover Plugin")
                description.set("Lavalink plugin that monitors audio source health and exposes REST endpoints")
                url.set("https://github.com/doyimmiuink/lavasrc-failover-plugin")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/doyimmiuink/lavasrc-failover-plugin")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

tasks.jar {
    archiveBaseName.set("lavasrc-failover-plugin")
    archiveVersion.set(version.toString())
}
