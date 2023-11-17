lazy val root = project
  .in(file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name                := "sbt-twirl-remote-cache",
    organization        := "io.github.irevive",
    crossSbtVersions    := Seq("1.9.7"),
    scriptedBufferLog   := false,
    scriptedLaunchOpts ++= Seq("-Xmx1024M", "-Dplugin.version=" + version.value),
    homepage            := Some(url("https://github.com/iRevive/sbt-twirl-remote-cache")),
    licenses            := List("MIT" -> url("http://opensource.org/licenses/MIT")),
    developers          := List(Developer("iRevive", "Maksym Ochenashko", "", url("https://github.com/iRevive")))
  )
  .settings(addSbtPlugin("org.playframework.twirl" % "sbt-twirl" % "2.0.2"))
