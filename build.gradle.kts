import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "com.yuhtin.quotes"
version = "1.0-SNAPSHOT"

application {
    mainClass = "com.yuhtin.quotes.bot.thumbnail.startup.Startup"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("net.dv8tion:JDA:5.0.0-beta.15") {
        exclude(module = "opus-java")
        exclude(group = "org.apache.logging.log4j")
    }

    // sqlite driver
    implementation("org.xerial:sqlite-jdbc:3.43.2.1")
    implementation("com.github.HenryFabio:sql-provider:9561f20fd2")
    implementation("com.google.guava:guava:31.0.1-jre")
    implementation("com.google.code.gson:gson:2.8.8")

    implementation("redis.clients:jedis:3.6.0")

    implementation("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")

}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
    archiveFileName.set("bot.jar")
    destinationDirectory.set(file(project.rootDir.parent.toString() + "/artifacts"))

    println("Shadowing ${project.name} to ${destinationDirectory.get()}")
}
