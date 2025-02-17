import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    java
    `maven-publish`
    kotlin("jvm") version "2.1.0"
    kotlin("kapt") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("com.gradleup.shadow") version "9.0.0-beta8"
    id("org.ajoberstar.grgit") version "4.1.1"
}

group = "lol.arch.symphony"
version = "1.1.0"

repositories {
    mavenCentral()
    maven("https://nexus.velocitypowered.com/repository/maven-public/")
    configureScalaRepository()
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("lib"))

    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    kapt("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")

    compileOnly("lol.arch.combinator:plugins-proxy:1.1.0")
    compileOnly("gg.scala.store:velocity:1.0.0")

    compileOnly("gg.scala.commons:velocity:4.2.3")
}

kotlin {
    jvmToolchain(jdkVersion = 21)
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
    exclude(
        "**/*.kotlin_metadata",
        "**/*.kotlin_builtins",
        "META-INF/"
    )

    archiveFileName.set(
        "symphony.jar"
    )
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
    options.fork()
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        javaParameters.set(true)
    }
}

publishing {
    repositories.configureScalaRepository()

    publications {
        register(
            name = "mavenJava",
            type = MavenPublication::class,
            configurationAction = shadow::component
        )
    }
}

tasks.getByName("build")
    .dependsOn(
        "shadowJar",
        "publishMavenJavaPublicationToScalaRepository"
    )

fun RepositoryHandler.configureScalaRepository(dev: Boolean = false)
{
    maven("${property("artifactory_contextUrl")}/gradle-${if (dev) "dev" else "release"}") {
        name = "scala"
        credentials {
            username = property("artifactory_user").toString()
            password = property("artifactory_password").toString()
        }
    }
}
