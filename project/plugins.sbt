addSbtPlugin("com.geirsson"  % "sbt-ci-release" % "1.5.3")
addSbtPlugin("org.scalameta" % "sbt-scalafmt"   % "2.4.2")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
