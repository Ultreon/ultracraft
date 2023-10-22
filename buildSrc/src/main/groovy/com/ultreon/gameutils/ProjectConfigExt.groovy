package com.ultreon.gameutils

import org.gradle.api.Task

import java.util.function.Supplier

class ProjectConfigExt {
    ProjectType type = null
    Supplier<List<Task>> jarDependTasks = { [] }
}
