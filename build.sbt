lazy val root = project
  .in(file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name                := "sbt-twirl-remote-cache",
    organization        := "io.github.irevive",
    crossSbtVersions    := Seq("1.4.1"),
    scriptedBufferLog   := false,
    scriptedLaunchOpts ++= Seq("-Xmx1024M", "-Dplugin.version=" + version.value),
    homepage            := Some(url("https://github.com/iRevive/sbt-twirl-remote-cache")),
    licenses            := List("MIT" -> url("http://opensource.org/licenses/MIT")),
    developers          := List(Developer("iRevive", "Maksim Ochenashko", "", url("https://github.com/iRevive")))
  )
  .settings(addSbtPlugin("com.typesafe.play" % "sbt-twirl" % "1.5.2"))
