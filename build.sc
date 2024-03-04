import $ivy.`de.tototec::de.tobiasroeser.mill.osgi::0.5.0`
import $ivy.`de.tototec::de.tobiasroeser.mill.vcs.version::0.4.0`

import mill._
import mill.scalalib._
import mill.scalalib.publish._

import de.tobiasroeser.mill.osgi._
import de.tobiasroeser.mill.vcs.version.VcsVersion

object main extends RootModule with JavaModule with PublishModule with OsgiBundleModule {

  override def publishVersion = VcsVersion.vcsState().format()

  val url = "https://github.com/domino-osgi/domino-java"

  object Deps {
    val slf4j = ivy"org.slf4j:slf4j-api:1.7.36"
    val osgiCore = ivy"org.osgi:org.osgi.core:5.0.0"
    val osgiCompendium = ivy"org.osgi:org.osgi.compendium:5.0.0"
    val utilsFunctional = ivy"de.tototec:de.tototec.utils.functional:2.3.0"
    val lambdaTest = ivy"de.tototec:de.tobiasroeser.lambdatest:0.8.0"
    val junit4 = ivy"junit:junit:4.13.2"
    val felixConnect = ivy"org.apache.felix:org.apache.felix.connect:0.2.0"
    val asciiDoclet = ivy"org.asciidoctor:asciidoclet:1.5.4"
    val bndlib = "biz.aQute.bnd:biz.aQute.bndlib:3.5.0"
    // val felixConfigAdmin = "org.apache.felix" % "org.apache.felix.configadmin" % "1.8.8"
    val logbackClassic = ivy"ch.qos.logback:logback-classic:1.5.3"
    val junitInterface = ivy"com.github.sbt:junit-interface:0.13.3"
  }

  override def artifactName = "domino-java"
  override def bundleSymbolicName = "domino.java"

  override def osgiHeaders = super
    .osgiHeaders()
    .copy(
      `Import-Package` = Seq(
        """org.slf4j.*;version="[1.7,3)";resolution:=optional""",
        "de.tototec.utils.functional.*;resolution:=optional",
        "*"
      ),
      `Export-Package` = Seq(
        s"""domino.java""",
        s"""domino.java.capsule"""
      )
    )

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
    Dep(Deps.slf4j.dep.withOptional(true), cross = CrossVersion.empty(false), force = false),
    Dep(Deps.utilsFunctional.dep.withOptional(true), cross = CrossVersion.empty(false), force = false)
  )

  override def javacOptions = Seq("-source", "8", "-target", "8", "-encoding", "UTF-8", "-deprecation")

  override def javadocOptions: T[Seq[String]] = super.javadocOptions() ++ Seq("-Xdoclint:none")

  override def generatedSources = T {
    val dest = T.ctx().dest
    Seq("README.adoc", "LICENSE.txt").foreach(f => os.copy.into(millSourcePath / f, dest))
    Seq(PathRef(dest))
  }

  object test extends JavaModuleTests with TestModule.Junit4 {
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
  }

}
