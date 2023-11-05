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

### Source Dependency

Gradle can install the library [from source][4]. However, there does not seem
to be any support for pinning to a Git tag or commit hash yet. Beware of 
installing directly from GitHub because backwards compatibility is likely to
break at some point. A safer option is to install from a local clone that is
not automatically updated.

**settings.gradle.kts**
``` kotlin
sourceControl {
    gitRepository(uri("https://github.com/mdklatt/idea-common.git")) {
        producesModule("dev.mdklatt:idea-common")
    }
}
```

**build.gradle.kts**
``` kotlin
dependencies {
    implementation("dev.mdklatt:idea-common") {
        version {
            // Pointing to a tag or commit is not supported yet.
            branch = "main"  // default
        }
    }
}
```

### JitPack

The [JitPack service][7] can be used to build the library on demand from 
GitHub. This allows a specific tag or commit ID to be requested instead of a
branch.

**build.gradle.kts**
``` kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.mdklatt:idea-common:<ref>")  // from JitPack
}
```


### Maven Package

The goal is to use GitHub Packages to publish this library as Maven package.
Unfortunately, [authentication is required][6], even for public packages. At
this time, only [installation from source](#source-dependency) is supported.

**build.gradle.kts**
``` kotlin
repositories { 
    mavenCentral()
    maven { 
        url = uri("https://maven.pkg.github.com/mdklatt/idea-common/")
    }
}

dependencies {
    implementation("dev.mdklatt:idea-common:<version>") 
}
```


[1]: https://github.com/mdklatt/idea-common/actions/workflows/test.yml/badge.svg
[2]: https://github.com/mdklatt/idea-common/actions/workflows/test.yml
[3]: https://plugins.jetbrains.com/docs/intellij/welcome.html
[4]: https://blog.gradle.org/introducing-source-dependencies
[5]: https://img.shields.io/static/v1?label=IDEA&message=2023.1%2B&color=informational
[6]: https://blog.gradle.org/introducing-source-dependencies
[7]: https://jitpack.io/docs/#building-with-jitpack
