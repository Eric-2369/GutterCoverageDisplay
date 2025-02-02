plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "cx.eri.guttercoveragedisplay"
version = "1.0.3"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
        intellijDependencies()
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.12.1")

    intellijPlatform {
        create("IC", "2024.2")
        bundledPlugins("Git4Idea")
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "cx.eri.gutter-coverage-display"
        name = "Gutter Coverage Display"
        version = project.version.toString()
        description = """
            <p>A simple IntelliJ-based plugin to display and toggle coverage data in the gutter, specifically for the repository
            <a href="https://github.com/techops-e2ecs/psa-sfdx">psa-sfdx</a>.</p>
            <p>This plugin allows users to read external coverage JSON files and display the coverage data directly in the gutter of the code editor.</p>
            <ul>
                <li>Toggle coverage data visibility</li>
                <li>Supports custom JSON coverage files</li>
                <li>Seamless integration with Git</li>
            </ul>
            <p>For more information, please visit our <a href="https://github.com/Eric-2369/GutterCoverageDisplay">project page</a>.</p>
            """.trimIndent()
        changeNotes = """
            <h2>Version 1.0.3</h2>
            <ul>
                <li>Updated Gson library.</li>
            </ul>
            <h2>Version 1.0.2</h2>
            <ul>
                <li>Updated dependencies to ensure compatibility with IntelliJ IDEA 2024.2.</li>
                <li>Increased the minimum supported version to IntelliJ IDEA 2024.2.</li>
            </ul>
            <h2>Version 1.0.1</h2>
            <ul>
                <li>Updated plugin icon.</li>
                <li>Improved plugin description.</li>
            </ul>
            <h2>Initial Release - Version 1.0.0</h2>
            <ul>
                <li>Initial release of Gutter Coverage Display plugin.</li>
                <li>Added feature to read and display coverage data from external JSON files.</li>
                <li>Implemented toggle functionality for coverage data visibility.</li>
            </ul>
            """.trimIndent()
        vendor {
            name = "Eric2369"
            email = "admin@eri.cx"
            url = "https://eri.cx"
        }
        ideaVersion {
            sinceBuild = "242"
            untilBuild = provider { null }
        }
    }
}

tasks {
    publishPlugin {
        token.set(System.getenv("INTELLIJ_PUBLISH_TOKEN"))
        channels.set(listOf("default"))
        hidden.set(true)
    }
}