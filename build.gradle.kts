plugins {
    kotlin("jvm") version "2.1.0" // Указываем версию Kotlin
    id("com.github.johnrengelman.shadow") version "8.1.1" // Плагин для создания одного JAR с зависимостями
}

group = "me.justkeel"
version = "1.0"

repositories {
    mavenCentral() // Стандартный репозиторий для зависимостей
    maven("https://repo.papermc.io/repository/maven-public/") // Репозиторий для PaperAPI
    maven("https://jitpack.io") // Репозиторий для PlaceholderAPI через JitPack
}

dependencies {
    // Зависимость для API Paper, чтобы компилировать только на сервере Paper
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")

    // Стандартная библиотека Kotlin
    implementation(kotlin("stdlib"))

    // Зависимость для PlaceholderAPI для работы с переменными
    compileOnly("com.github.PlaceholderAPI:PlaceholderAPI:2.11.6")

}

tasks {
    shadowJar {
        // Сборка JAR-файла без добавления суффикса "-all"
        archiveClassifier.set("")

        // Чтобы собрать все зависимости в один JAR
        mergeServiceFiles()

        // Дополнительные параметры могут быть добавлены в зависимости от нужд
    }
    build {
        dependsOn(shadowJar) // Убедитесь, что shadowJar собирается при сборке
    }
}

java {
    toolchain {
        // Указываем Java 21
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
