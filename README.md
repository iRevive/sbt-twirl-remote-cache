# sbt-twirl-remote-cache

[![Build Status](https://github.com/iRevive/sbt-twirl-remote-cache/workflows/CI/badge.svg)](https://github.com/iRevive/sbt-twirl-remote-cache/actions?query=branch%3Amaster+workflow%3ACI+)
[![Maven Version](https://maven-badges.herokuapp.com/maven-central/io.github.irevive/sbt-twirl-remote-cache/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.irevive/sbt-twirl-remote-cache)

sbt-twirl-remote-cache is an SBT plugin that actives support of the remote cache introduced in SBT 1.4.0+.    
The plugin creates an archive of the **generated** Twirl templates and pushes it to the remote cache directory/repository.    
Works out of the box with Play Framework.  

**Note:** caching of the **generated** files can lead to various problems, e.g. non-reproducible builds.  
On the other hand, such an approach is suitable for a project with multiple subprojects, where the templates do not change often.
Therefore use with caution.

## Quick Start

To use sbt-twirl-remote-cache in an existing SBT project (1.4.0+), add the following dependency to your `plugins.sbt`:
 
```sbt
addSbtPlugin("io.github.irevive" % "sbt-twirl-remote-cache" % "0.1.0")
```

## Usage Guide

The project configuration:
```sbt
lazy val root = project
  .in(file("."))
  .enablePlugins(PlayScala)
  .settings(
    // setup remote cache
    pushRemoteCacheTo := Some(MavenCache("local-cache", (ThisBuild / baseDirectory).value / "remote-cache"))
  )
```

The commands:
* `twirlPushRemoteCache` - compile Twirl templates and push an archive to the remote cache repository
* `twirlPullRemoteCache` - pull an archive from the remote cache repository and extract Twirl templates

CI usage example:
```
sbt pullRemoteCache \ 
    twirlPullRemoteCache \ 
    compile \
    test \
    pushRemoteCache \
    twirlPushRemoteCache
```
