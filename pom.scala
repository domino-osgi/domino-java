import org.sonatype.maven.polyglot.scala.model._
import scala.collection.immutable._

val dominoJavaName = "domino.java"
val dominoJavaVersion = "0.1.0-SNAPSHOT"
val url = "https://github.com/domino-osgi/domino-java"

object Deps {
	val asciiDoclet = "org.asciidoctor" % "asciidoclet" % "1.5.4"
	val bndlib = "biz.aQute.bnd" % "biz.aQute.bndlib" % "3.5.0"
	val osgiCompendium = "org.osgi" % "org.osgi.compendium" % "5.0.0"
  val osgiCore = "org.osgi" % "org.osgi.core" % "5.0.0"
  val slf4j = "org.slf4j" % "slf4j-api" % "1.7.25"
  val utilsFunctional = "de.tototec" % "de.tototec.utils.functional" % "1.0.0"
  // val felixConfigAdmin = "org.apache.felix" % "org.apache.felix.configadmin" % "1.8.8"
  // val pojosr = "com.googlecode.pojosr" % "de.kalpatec.pojosr.framework.bare" % "0.2.1"
  // val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.1.3"
}

object Plugins {
  val bnd = "biz.aQute.bnd" % "bnd-maven-plugin" % "3.5.0"
  val bundle = "org.apache.felix" % "maven-bundle-plugin" % "3.3.0"
  val clean = "org.apache.maven.plugins" % "maven-clean-plugin" % "3.0.0"
  val gpg = "org.apache.maven.plugins" % "maven-gpg-plugin" % "1.6"
  val jar = "org.apache.maven.plugins" % "maven-jar-plugin" % "2.5"
  val javadoc = "org.apache.maven.plugins" % "maven-javadoc-plugin" % "2.10.3"
  val polyglotTranslate = "io.takari.polyglot" % "polyglot-translate-plugin" % "0.2.1"
  val surefire = "org.apache.maven.plugins" % "maven-surefire-plugin" % "2.17"
}

Model(
  gav = "com.github.domino-osgi" % "domino-java" % dominoJavaVersion,
  modelVersion = "4.0.0",
  packaging = "bundle",
  properties = Map(
    "maven.compiler.source" -> "1.8",
    "maven.compiler.target" -> "1.8",
    "project.build.sourceEncoding" -> "UTF-8",
    "bundle.symbolicName" -> "${project.artifactId}",
    "bundle.namespace" -> "domino.java"
  ),
  name = "Domino for Java",
  description = "A lightweight Java library for writing elegant OSGi bundle activators",
  url = url,
  scm = Scm(
    url = url,
    connection = "scm:git:" + url,
    developerConnection = "scm:git:" + url
  ),
  licenses = Seq(License(
    name = "Apache License, Version 2",
    url = "http://www.apache.org/licenses",
    distribution = "repo"
  )),
  developers = Seq(
    Developer(
      name = "Tobias Roeser",
      email = "le.petit.fou@web.de"
    )
  ),
  dependencies = Seq(
    // compile dependencies
    Deps.osgiCore,
    Deps.osgiCompendium,
    Dependency(Deps.slf4j, scope = "provided", optional = true),
    Dependency(Deps.utilsFunctional, scope = "provided", optional = true)
    // test dependencies
    // Deps.felixConfigAdmin % "test",
    // Deps.pojosr % "test"
    // Deps.logbackClassic % "test"
  ),
  build = Build(
    resources = Seq(
      Resource(
        directory = "src/main/resources"
      ),
      Resource(
        directory = ".",
        includes = Seq(
          "README.adoc",
          "FAQ.adoc",
          "UserGuide.adoc",
          "LICENSE"
        )
      )
    ),
    plugins = Seq(
      // Build OSGi Manifest and bundle, also check version compatibility (baselining)
      Plugin(
        Plugins.bundle,
        dependencies = Seq(
          Deps.bndlib
        ),
        extensions = true,
        configuration = Config(
          instructions = Config(
            _include = "osgi.bnd"
          )
        ),
        executions = Seq(Execution(phase = "verify", goals = Seq("baseline")))
      ),
       // Use Asciidoclet processor instead of standard Javadoc
      Plugin(
        Plugins.javadoc,
        configuration = Config(
          source = "${maven.compiler.source}",
          doclet = "org.asciidoctor.Asciidoclet",
          docletArtifact = Config(
            groupId = Deps.asciiDoclet.groupId.get,
            artifactId = Deps.asciiDoclet.artifactId,
            version = Deps.asciiDoclet.version.get
          ),
          overview = "README.adoc",
          additionalparam = s"""--base-dir "$${project.basedir}"
            | --attributes-file src/main/doc/placeholders.adoc
            | --attribute "name=$${project.name}"
						| --attribute "version=${dominoJavaVersion}"
						| --attribute "dominojavaversion=${dominoJavaVersion}"
						| --attribute "title-link=${url}[${dominoJavaName} ${dominoJavaVersion}]"
						| --attribute "env-asciidoclet=true"""".stripMargin
        )
      )
    )
  ),
  profiles = Seq(
    Profile(
      id = "gen-pom-xml",
      build = BuildBase(
        plugins = Seq(
          Plugin(
            Plugins.polyglotTranslate,
            executions = Seq(
              Execution(
                id = "pom-scala-to-pom-xml",
                phase = "initialize",
                goals = Seq("translate-project"),
                configuration = Config(
                  input = "pom.scala",
                  output = "pom.xml"
                )
              )
            )
          ),
          Plugin(
            Plugins.clean,
            configuration = Config(
              filesets = Config(
                fileset = Config(
                  directory = "${basedir}",
                  includes = Config(
                    include = "pom.xml"
                  )
                )
              )
            )
          )
        )
      )
    )
  )
)
