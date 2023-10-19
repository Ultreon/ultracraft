package com.ultreon.craftutils

import org.gradle.api.Project

import java.time.Instant

class CraftUtilsExt {
    String projectName
    String projectVersion = "dev"
    String projectGroup = "com.example"
    String projectId = "example-project"
    Project coreProject
    Project desktopProject
    boolean production = false
    final buildDate = Instant.now()
    File runDirectory
}
