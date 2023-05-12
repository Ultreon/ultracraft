package com.ultreon.craft.resources

data class Identifier(val namespace: String, val path: String) {
    override fun toString(): String {
        return "$namespace:$path"
    }

    companion object {
        private val namespaceRegex = Regex("[a-z0-9_\\-]*")
        private val pathRegex = Regex("[a-z0-9_\\-/.]*")

        private fun testNamespace(namespace: String): String {
            if (namespaceRegex.matches(namespace)) {
                return namespace
            }

            throw IllegalArgumentException("Invalid namespace: $namespace")
        }

        private fun testPath(path: String): String {
            if (namespaceRegex.matches(path)) {
                return path
            }

            throw IllegalArgumentException("Invalid path: $path")
        }

        operator fun invoke(text: String): Identifier {
            if (":" in text) {
                val split = text.split(":", limit = 2)
                return Identifier(testNamespace(split[0]), testPath(split[1]))
            }
            return Identifier(defaultNamespace, testPath(text))
        }
    }
}

fun id(namespace: String, path: String) = Identifier(namespace, path)
fun id(text: String) = Identifier(text)
fun craftId(text: String) = Identifier(defaultNamespace, text)
fun idOrNull(text: String): Identifier? = try {
    Identifier(text)
} catch (e: IllegalArgumentException) {
    null
}

const val defaultNamespace: String = "craft"
