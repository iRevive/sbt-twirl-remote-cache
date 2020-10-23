lazy val root = project
  .in(file("."))
  .enablePlugins(SbtTwirl)
  .settings(
    remoteCacheId := "static-id", // simplifies testing
    remoteCacheIdCandidates := Seq("static-id")
  )
