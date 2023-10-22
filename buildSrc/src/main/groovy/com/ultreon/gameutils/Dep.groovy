package com.ultreon.gameutils

class Dep {
    String group
    String name
    String version
    String extension
    String classifier
    File file

    Dep(String group, String name, String version, String extension, String classifier, File file) {
        this.group = group
        this.name = name
        this.version = version
        this.extension = extension
        this.classifier = classifier
        this.file = file
    }
}
