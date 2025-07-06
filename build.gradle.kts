plugins {
    kotlin("jvm") version "2.0.20"
    id("com.gradleup.shadow") version "8.3.0"
    id("io.papermc.paperweight.userdev") version "1.7.2"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("xyz.jpenilla.resource-factory-paper-convention") version "1.2.0"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(kotlin("stdlib"))
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
}

tasks {
    shadowJar {
        archiveBaseName.set(name)
        archiveVersion.set(version as String)
        archiveClassifier.set("")

        relocate("kotlin", "$group.shade.kotlin")

        mergeServiceFiles()
        minimize()
    }
    runServer {
        minecraftVersion("1.21.1")
    }
}

paperPluginYaml {
    main = "$group.Main"
    apiVersion = "1.21.1"
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
