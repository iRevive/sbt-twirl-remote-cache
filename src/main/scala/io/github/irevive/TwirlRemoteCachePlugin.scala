package io.github.irevive

import java.io.File

import play.twirl.sbt.SbtTwirl
import play.twirl.sbt.SbtTwirl.autoImport.TwirlKeys
import sbt.Keys._
import sbt._
import sbt.internal.remotecache.CustomRemoteCacheArtifact

object TwirlRemoteCachePlugin extends AutoPlugin {

  override def requires = SbtTwirl
  override def trigger  = allRequirements

  object autoImport {
    lazy val TwirlCache = config("twirlCache")

    lazy val twirlRemoteCacheArtifactName = settingKey[String]("The artifact name")
    lazy val twirlRemoteCacheDir          = settingKey[File]("Twirl remote cache directory")
    lazy val twirlPullRemoteCache         = taskKey[Unit]("Push generated Twirl templates to remote cache")
    lazy val twirlPushRemoteCache         = taskKey[Unit]("Pull Twirl templates from remote cache")
  }

  import autoImport._

  override lazy val projectConfigurations: Seq[Configuration] = Seq(
    TwirlCache
  )

  override lazy val projectSettings: Seq[Setting[_]] =
    Seq(
      twirlRemoteCacheArtifactName := "twirl-templates",
      pushRemoteCacheConfiguration / remoteCacheArtifacts += {
        val art              = (TwirlCache / artifact).value
        val packaged         = TwirlCache / packageCache
        val extractDirectory = crossTarget.value / "twirl" / "main"

        CustomRemoteCacheArtifact(art, packaged, extractDirectory, preserveLastModified = false)
      },
      twirlRemoteCacheDir := {
        val remote          = pushRemoteCacheTo.value
        val module          = moduleName.value
        val scalaBinVersion = scalaBinaryVersion.value

        val base = remote match {
          case Some(cache: MavenCache) => cache.rootFile
          case _                       => baseDirectory.value
        }

        base / module / scalaBinVersion
      },
      twirlPushRemoteCache := {
        val _         = (Compile / TwirlKeys.compileTemplates).value
        val log       = streams.value.log
        val cacheDir  = twirlRemoteCacheDir.value
        val baseDir   = crossTarget.value / "twirl"
        val cacheId   = remoteCacheId.value
        val templates = baseDir.globRecursive("*.template.scala").pair(Path.relativeTo(baseDir))
        val output    = cacheDir / s"$cacheId.zip"

        IO.zip(templates, output, None)

        log.info(s"Published Twirl cache to $output")
      },
      twirlPullRemoteCache := {
        val log          = streams.value.log
        val baseDir      = crossTarget.value / "twirl"
        val cacheDir     = twirlRemoteCacheDir.value
        val candidateIds = remoteCacheIdCandidates.value

        candidateIds.map(id => cacheDir / s"$id.zip").find(_.exists) match {
          case Some(archive) =>
            log.info(s"Found Twirl cache $archive")
            IO.unzip(archive, baseDir, preserveLastModified = false)

          case None =>
            log.info("Twirl cache does not exist")
        }
      }
    ) ++ cacheSettings

  def cacheSettings: Seq[Def.Setting[_]] =
    inConfig(TwirlCache)(
      Seq(
        packageOptions := {
          val n       = name.value + "-" + twirlRemoteCacheArtifactName.value
          val ver     = version.value
          val org     = organization.value
          val orgName = organizationName.value
          val hp      = homepage.value

          List(
            Package.addSpecManifestAttributes(n, ver, orgName),
            Package.addImplManifestAttributes(n, ver, hp, org, orgName)
          )
        },
        mappings := {
          val templatesDir = (Compile / TwirlKeys.compileTemplates / target).value
          val templates    = templatesDir.globRecursive("*.template.scala").pair(Path.relativeTo(templatesDir))

          templates
        },
        packageConfiguration := Defaults.packageConfigurationTask.value,
        packageCache         := Defaults.packageTask.value,
        artifact             := Artifact(moduleName.value, twirlRemoteCacheArtifactName.value),
        packagedArtifact     := (artifact.value -> packageCache.value),
        artifactPath         := Defaults.artifactPathSetting(artifact).value,
        artifactName         := Artifact.artifactName
      )
    )

}
