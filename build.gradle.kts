plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.0.1"
}

group = "cx.eri.guttercoveragedisplay"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
        intellijDependencies()
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    intellijPlatform {
        create("IC", "2024.1")
        bundledPlugins("Git4Idea")
        instrumentationTools()
    }
}

tasks.test {
    useJUnitPlatform()
}