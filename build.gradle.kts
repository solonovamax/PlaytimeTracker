plugins {
    id("org.quiltmc.loom")
    `maven-publish`
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    
    withSourcesJar()
}

loom {
    accessWidenerPath.set(file("src/main/resources/playtime_tracker.accesswidener"))
}

// archivesBaseName = findProperty("archives_base_name")
version = findProperty("mod_version")!!
group = findProperty("maven_group")!!

repositories {
    mavenCentral()
    maven("https://maven.quiltmc.org/repository/release")
    maven("https://maven.fabricmc.net/")
    maven("https://jitpack.io/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/") {
        mavenContent { snapshotsOnly() }
    }
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        mavenContent { snapshotsOnly() }
    }
    maven("https://masa.dy.fi/maven")
}

kotlin {
    // explicitApi()
    target {
        compilations.configureEach {
            kotlinOptions {
                jvmTarget = "17"
                apiVersion = "1.7"
                languageVersion = "1.7"
            }
        }
    }
}

dependencies {
    val minecraftVersion = findProperty("minecraft_version")
    val yarnMappingsVersion = findProperty("yarn_mappings")
    val loaderVersion = findProperty("loader_version")
    val fabricVersion = findProperty("fabric_version")
    val quiltMappingsBuild = findProperty("quilt_mappings_build")
    val quiltedFabricApiVersion = findProperty("quilted_fabric_api")
    val fabricKotlinVersion = findProperty("fabric_kotlin_version")
    val aegisVersion = findProperty("aegis_version")
    // val neVSetiVersion = findProperty("ne_v_seti_version")
    val ekhoVersion = findProperty("ekho_version")
    val cloudVersion = findProperty("cloud_version")
    val adventureVersion = findProperty("adventure_version")
    val adventureFabricVersion = findProperty("adventure_fabric_version")
    val carpetVersion = findProperty("carpet_version")
    
    // To change the versions see the gradle.properties file
    
    minecraft("com.mojang:minecraft:$minecraftVersion")
    // @Suppress("UnstableApiUsage")
    // mappings(loom.layered {
    //     addLayer(quiltMappings.mappings("org.quiltmc:quilt-mappings:$minecraftVersion+build.14:v2"))
    // })
    mappings("net.fabricmc:yarn:$yarnMappingsVersion:v2")
    
    modImplementation("org.quiltmc:quilt-loader:$loaderVersion")
    
    // modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    modImplementation("org.quiltmc.quilted-fabric-api:quilted-fabric-api:$quiltedFabricApiVersion-$minecraftVersion") {
        exclude(group = "org.quiltmc.quilted-fabric-api", module = "fabric-gametest-api-v1")
    }
    
    // Fabric API
    // modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    // modImplementation("net.fabricmc.fabric-api:fabric-transitive-access-wideners-v1:1.3.0")
    
    // Kotlin
    modImplementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")
    implementation(kotlin("stdlib-jdk8"))
    
    // Aegis
    modImplementation("com.github.P03W:Aegis:$aegisVersion") { include(this) }
    
    // Ne v seti
    // modImplementation("com.github.solonovamax:ne-v-seti:$neVSetiVersion") { include(this) }
    
    // Ekho
    modImplementation("com.github.SpaceClouds42:Ekho:$ekhoVersion") { include(this) }
    
    compileOnly("org.checkerframework", "checker-qual", "3.21.1")
    
    // Fabric Carpet
    modImplementation("carpet:fabric-carpet:$carpetVersion")
    
    modImplementation("cloud.commandframework:cloud-fabric:$cloudVersion") { include(this) }
    modImplementation("cloud.commandframework:cloud-brigadier:$cloudVersion") { include(this) }
    implementation("cloud.commandframework:cloud-annotations:$cloudVersion") { include(this) }
    implementation("cloud.commandframework:cloud-kotlin-extensions:$cloudVersion") { include(this) }
    implementation("cloud.commandframework:cloud-kotlin-coroutines:$cloudVersion") { include(this) }
    implementation("cloud.commandframework:cloud-kotlin-coroutines-annotations:$cloudVersion") { include(this) }
    implementation("cloud.commandframework:cloud-services:$cloudVersion") { include(this) }
    implementation("cloud.commandframework:cloud-minecraft-extras:$cloudVersion") { include(this) }
    
    modImplementation("net.kyori:adventure-platform-fabric:$adventureFabricVersion") {
        exclude("ca.stellardrift", "colonel")
        include(this)
    }
    
    implementation("net.kyori:adventure-extra-kotlin:$adventureVersion") {
        include(this)
    }
}

java {

}

tasks {
    processResources.configure {
        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }
    
    withType<Jar>().configureEach {
        from(rootProject.file("LICENSE"))
    }
    
    // ensure that the encoding is set to UTF-8, no matter what the system default is
    // this fixes some edge cases with special characters not displaying correctly
    // see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            // add all the jars that should be included when publishing to maven
            artifact(tasks.remapJar) {
                builtBy(tasks.remapJar)
            }
            artifact(tasks["sourcesJar"]) {
                builtBy(tasks.remapSourcesJar)
            }
        }
    }
    
    // select the repositories you want to publish to
    repositories {
        // uncomment to publish to the local maven
        // mavenLocal()
    }
}

project.afterEvaluate {
    loom {
        runs {
            configureEach {
                property("fabric.development=true")
                property("mixin.hotSwap")
                // val mixinJarFile = configurations.compileClasspath.get().files {
                //     it.group == "net.fabricmc" && it.name == "sponge-mixin"
                // }.first()
                // vmArg("-javaagent:$mixinJarFile")
                
                ideConfigGenerated(true)
            }
        }
    }
}
