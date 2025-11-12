plugins {
    id("java-library")
    id("jacoco-report-aggregation")
    id("maven-publish")
    id("signing")
    id("com.palantir.git-version") version "4.2.0"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

group = "io.github.twonirwana"
val gitVersion: groovy.lang.Closure<String> by extra
version = gitVersion()
description = "Dice expression parser and evaluator"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    implementation("com.google.guava:guava:33.5.0-jre")
    implementation("org.apache.commons:commons-lang3:3.19.0")

    testCompileOnly("org.projectlombok:lombok:1.18.42")
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:6.0.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:3.27.6")

}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(false)
    }
}
publishing {
    println("Version: " + gitVersion())

    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "dice-evaluator"
            from(components["java"])
            pom {
                name.set("dice-evaluator")
                description.set("Dice infix notation (aka calculator notation) expression parser and evaluator")
                url.set("https://github.com/twonirwana/DiceEvaluator")
                version = gitVersion()
                licenses {
                    license {
                        name.set("GNU Affero General Public License v3.0")
                        url.set("https://www.gnu.org/licenses/agpl-3.0.en.html")
                    }
                }
                developers {
                    developer {
                        id.set("2nirwana")
                        name.set("Janno von Stülpnagel")
                        email.set("jvs@mailbox.org")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/twonirwana/DiceEvaluator.git")
                    developerConnection.set("scm:git:ssh://github.com/twonirwana/DiceEvaluator.git")
                    url.set("https://github.com/twonirwana/DiceEvaluator")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}


nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
        }
    }
}
