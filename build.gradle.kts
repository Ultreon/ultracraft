import com.soywiz.korge.gradle.*
import org.joda.time.*

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }

    dependencies {
        classpath("joda-time:joda-time:2.12.2")
    }
}

plugins {
    kotlin("multiplatform") version "1.7.21"
    id("com.soywiz.korge") version "3.4.0"
}

val group: String by project
val productName: String by project
val productId = "$group.$productName"
val gameName: String by project
val gameVersion: String by project

println("Project ID: $productId ($productName)")
println("Game Info: $gameName @ $gameVersion")

project.group = group

task("setupProductJson", Copy::class) {
    from(file("$projectDir/product.json"))
    into("src/commonMain/resources/assets/$productName")
    expand(
        "product_id" to productId,
        "product_name" to productName,
        "game_name" to gameName,
        "game_version" to korge.version,
        "build_date" to DateTime.now(DateTimeZone.UTC).millis
    )
}

tasks.all() {
    if (this is KorgeProcessedResourcesTask) {
        dependsOn("setupProductJson")
    }
}

korge {
	id = productId

    version = "0.0.1"

	targetJvm()
	targetJs()
	targetDesktop()
	targetIos()
	targetAndroidIndirect()

    serialization()
    serializationJson()

    support3d()
    supportExperimental3d()
    supportVibration()
}

