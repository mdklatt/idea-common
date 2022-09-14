# idea-common

 ![IDEA version][5]
 [![Test status][1]][2]

Common utility library for [developing JetBrains IDEA plugins][3] with Kotlin.

## Packages

### *dev.mdklatt.idea.common.exec*

#### `CommandLine`

Execute an external command-line process.

#### `PosixCommandLine`

Execute an external process with a POSIX command line.

#### `WindowsCommandLine`

Execute an external process with a Windows command line.

### *dev.mdklatt.idea.common.map*

Extensions for the standard library `Map` class.

### *dev.mdklatt.idea.common.password*

#### `StoredPassword`

Manage passwords in the system credential store.

#### `ModalDialog`

A modal dialog box to use as a password prompt.


## Installation

Use Gradle to install as a [source dependency][4].

**settings.gradle.kts**
```kotlin
sourceControl {
    gitRepository(uri("https://github.com/mdklatt/idea-common.git")) {
        producesModule("dev.mdklatt:idea-common")
    }
}
```

**build.gradle.kts**
```kotlin
dependencies {
    implementation("dev.mdklatt:idea-common") {
        version {
            // Pointing to a tag or commit is not supported yet.
            branch = "main"  // default
        }
    }
}
```

[1]: https://github.com/mdklatt/idea-common/actions/workflows/test.yml/badge.svg
[2]: https://github.com/mdklatt/idea-common/actions/workflows/test.yml
[3]: https://plugins.jetbrains.com/docs/intellij/welcome.html
[4]: https://blog.gradle.org/introducing-source-dependencies
[5]: https://img.shields.io/static/v1?label=IDEA&message=2022.1%2B&color=informational