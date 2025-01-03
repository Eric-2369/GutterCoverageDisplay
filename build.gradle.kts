plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "cx.eri.guttercoveragedisplay"
version = "1.0.2"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
        intellijDependencies()
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")

    intellijPlatform {
        create("IC", "2024.1")
        bundledPlugins("Git4Idea")
    }
}

tasks {
    publishPlugin {
        token.set(System.getenv("INTELLIJ_PUBLISH_TOKEN"))
        channels.set(listOf("default"))
        hidden.set(true)
    }
}