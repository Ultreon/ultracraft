package com.ultreon.gameutils

import org.gradle.api.Project
import java.time.Instant

class GameUtilsExt {
    String projectName
    String projectVersion = "dev"
    String projectGroup = "com.example"
    String projectId = "example-project"
    Project coreProject
    Project desktopProject
    Project packageProject
    int javaVersion = -1
    boolean production = false
    final buildDate = Instant.now()
    File runDirectory
    String mainClass
}
