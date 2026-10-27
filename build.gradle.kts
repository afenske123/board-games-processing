plugins {
    id("java")
    id("application")
}

group = "de.fenske.boardgames"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("de.fenske.boardgames.gui.BoardGamesSketch")
}

dependencies {
    // Processing wird als lokales core.jar eingebunden (siehe README: "Processing core.jar besorgen").
    implementation(files("libs/core.jar"))

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
