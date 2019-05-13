import java.io.File

import mill._
import mill.api.Loose
import mill.define.Target
import mill.modules.Util
import mill.scalalib._
import mill.scalalib.publish._

import $ivy.`de.tototec::de.tobiasroeser.mill.osgi:0.0.6`
import de.tobiasroeser.mill.osgi._

import $ivy.`de.tototec::de.tobiasroeser.mill.publishM2:0.1.0`
import de.tobiasroeser.mill.publishM2._

object main
  extends MavenModule
  with PublishModule
  with OsgiBundleModule
  with PublishM2Module {

  val url = "https://github.com/domino-osgi/domino-java"

  object Deps {
    val slf4j = ivy"org.slf4j:slf4j-api:1.7.25"
    val osgiCore = ivy"org.osgi:org.osgi.core:5.0.0"
    val osgiCompendium = ivy"org.osgi:org.osgi.compendium:5.0.0"
    val utilsFunctional = ivy"de.tototec:de.tototec.utils.functional:2.0.1"
    val lambdaTest = ivy"de.tototec:de.tobiasroeser.lambdatest:0.6.2"
    val junit4 = ivy"junit:junit:4.12"
    val felixConnect = ivy"org.apache.felix:org.apache.felix.connect:0.1.0"
    val asciiDoclet = ivy"org.asciidoctor:asciidoclet:1.5.4"
    val bndlib = "biz.aQute.bnd:biz.aQute.bndlib:3.5.0"
    // val felixConfigAdmin = "org.apache.felix" % "org.apache.felix.configadmin" % "1.8.8"
    val logbackClassic = ivy"ch.qos.logback:logback-classic:1.1.3"
    val junitInterface = ivy"com.novocode:junit-interface:0.11"
  }

  override def artifactName = T {
    "domino-java"
  }

  override def bundleSymbolicName = "domino.java"

  def osgiHeaders = T {
    super.osgiHeaders().copy(
      `Import-Package` = Seq(
        "org.slf4j.*;resolution:=optional",
        "de.tototec.utils.functional.*;resolution:=optional",
        "*"
      ),
      `Export-Package` = Seq(
        s"""${bundleSymbolicName()};version="0.3.0"""",
        s"""${bundleSymbolicName()}.capsule;version="0.1.0""""
      )
    )
  }

  override def millSourcePath = super.millSourcePath / os.up

  override def publishVersion = T {
    "0.3.0-SNAPSHOT"
  }

  override def pomSettings: mill.T[mill.scalalib.publish.PomSettings] = T {
    PomSettings(
      description = "A lightweight Java library for writing elegant OSGi bundle activators",
      organization = "com.github.domino-osgi",
      url = url,
      licenses = Seq(License.`Apache-2.0`),
      versionControl = VersionControl.github("domino-osgi", "domino-java"),
      developers = Seq(
        Developer(id = "lefou", name = "Tobias Roeser", url = "https://github.com/lefou")
      )

    )
  }

  override def ivyDeps = Agg(
    Deps.osgiCore,
    Deps.osgiCompendium
  )

  override def compileIvyDeps = Agg(
    // Deps.slf4j.optional(true)
    Dep(Deps.slf4j.dep.copy(optional = true), cross = CrossVersion.empty(false), force = false),
    Dep(Deps.utilsFunctional.dep.copy(optional = true), cross = CrossVersion.empty(false), force = false)
  )

  def docletIvyDeps = T {
    Agg(
      Deps.asciiDoclet
    )
  }

  def docletClasspath = T {
    resolveDeps(docletIvyDeps)
  }

  override def generatedSources = T {
    val dest = T.ctx().dest
    Seq("README.adoc", "LICENSE.txt").foreach(f => os.copy.into(millSourcePath / f, dest))
    Seq(PathRef(dest))
  }

  override def javadocOptions = Seq(
    "-doclet", "org.asciidoctor.Asciidoclet",
    "-docletpath", s"${docletClasspath().map(_.path).mkString(File.pathSeparator)}",
    "-overview", s"${millSourcePath / "README.adoc"}",
    "--base-dir", s"${millSourcePath}",
    "--attributes-file", s"${millSourcePath / 'src / 'main / 'doc / "placeholders.adoc"}",
    "--attribute", s"name=${artifactName}",
    "--attribute", s"version=${publishVersion}",
    "--attribute", s"dominojavaversion=${publishVersion}",
    "--attribute", s"title-link=${url}[${bundleSymbolicName} ${publishVersion}]",
    "--attribute", "env-asciidoclet=true"
  )

  object test extends Tests {
    override def ivyDeps = T {
      super.ivyDeps() ++ Agg(
        Deps.lambdaTest,
        Deps.felixConnect,
        Deps.junitInterface,
        Deps.junit4,
        Deps.logbackClassic,
        Deps.utilsFunctional
      )
    }
    def testFrameworks = Seq("com.novocode.junit.JUnitFramework")
  }

}

