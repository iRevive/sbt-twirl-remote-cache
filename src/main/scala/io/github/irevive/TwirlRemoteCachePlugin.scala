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
    lazy val Twirl = config("twirl")

    lazy val twirlCacheArtifactName = settingKey[String]("The artifact name")
    lazy val twirlCachePull         = taskKey[Unit]("Push generated Twirl templates to remote cache")
    lazy val twirlCachePush         = taskKey[Unit]("Pull Twirl templates from remote cache")
    lazy val twirlCache             = taskKey[Unit]("Produce Twirl cache")
    lazy val twirlCacheRemote       = taskKey[File]("Twirl remote cache directory")
  }

  import autoImport._

  override lazy val projectConfigurations: Seq[Configuration] = Seq(
    Twirl
  )

  override lazy val projectSettings: Seq[Setting[_]] =
    Seq(
      twirlCacheArtifactName := "twirl-templates",
      pushRemoteCacheConfiguration / remoteCacheArtifacts += {
        val art              = (Twirl / artifact).value
        val packaged         = Twirl / packageCache
        val extractDirectory = crossTarget.value / "twirl" / "main"

        CustomRemoteCacheArtifact(art, packaged, extractDirectory, preserveLastModified = false)
      },
      twirlCacheRemote := {
        val remote          = pushRemoteCacheTo.value
        val module          = moduleName.value
        val scalaBinVersion = scalaBinaryVersion.value

        val base = remote match {
          case Some(cache: MavenCache) => cache.rootFile
          case _                       => baseDirectory.value
        }

        base / module / scalaBinVersion
      },
      twirlCachePush := {
        val log       = streams.value.log
        val cacheDir  = twirlCacheRemote.value
        val baseDir   = crossTarget.value / "twirl"
        val cacheId   = remoteCacheId.value
        val templates = baseDir.globRecursive("*.template.scala").pair(Path.relativeTo(baseDir))
        val output    = cacheDir / s"$cacheId.zip"

        IO.zip(templates, output, None)

        log.info(s"Published Twirl cache to $output")
      },
      twirlCachePull := {
        val log          = streams.value.log
        val baseDir      = crossTarget.value / "twirl"
        val cacheDir     = twirlCacheRemote.value
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
    inConfig(Twirl)(
      Seq(
        packageOptions := {
          val n       = name.value + "-" + twirlCacheArtifactName.value
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
        artifact             := Artifact(moduleName.value, twirlCacheArtifactName.value),
        packagedArtifact     := (artifact.value -> packageCache.value),
        artifactPath         := Defaults.artifactPathSetting(artifact).value,
        artifactName         := Artifact.artifactName
      )
    )

}
