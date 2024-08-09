plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "cx.eri.guttercoveragedisplay"
version = "1.0.0"

repositories {
    mavenCentral()
}

intellij {
    version.set("2024.1")
    plugins.set(listOf("Git4Idea"))
}

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}