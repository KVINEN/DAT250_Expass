plugins {
	java
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
    id("org.sonarqube") version "6.3.1.5724"
    application
    jacoco
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"


java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

application {
    mainClass = "com.example.DAT250_Expass.Dat250ExpassApplication"
}

sonar {
    properties {
        property("sonar.projectKey", "KVINEN_DAT250Expass")
        property("sonar.organization", "kvinen")
        property("sonar.host.url", "https://sonarcloud.io")
        val sonarToken = System.getenv("SONAR_TOKEN")
        if(sonarToken != null) {
            property("sonar.token", sonarToken)
        }
    }
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.6.0")
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
        csv.required = false
        html.outputLocation = layout.buildDirectory.dir("reports/jacoco/test/html")
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule {
            limit {
                minimum = "0.5".toBigDecimal()
            }
        }

        rule {
            isEnabled = false
            element = "CLASS"
            includes = listOf("org.gradle.*")

            limit {
                counter = "LINE"
                value = "TOTALCOUNT"
                maximum = "0.3".toBigDecimal()
            }
        }
    }
}