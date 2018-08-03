#!/bin/sh

set -e

if [ ! -f "staging-settings.xml" ]; then

  cat > staging-settings.xml << EOF
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>your-username</username>
      <password>your-password</password>
    </server>
  </servers>
</settings>
EOF

fi

echo "Please edit staging-settings.xml with propper connection details."
read

DOMINO_VERSION="0.2.0"
URL="https://oss.sonatype.org/service/local/staging/deploy/maven2/"

mvn clean source:jar javadoc:jar install

if [ -n "$SKIP_UPLOAD" ] ; then
  echo "Skipping Upload"
else

  echo "Uploading jar"
  mvn -s ./staging-settings.xml gpg:sign-and-deploy-file -Durl="${URL}" -DrepositoryId=ossrh -DpomFile=.polyglot.pom.scala -Dfile="target/domino-java-${DOMINO_VERSION}.jar"

  echo "Uploading sources"
  mvn -s ./staging-settings.xml gpg:sign-and-deploy-file -Durl="${URL}" -DrepositoryId=ossrh -DpomFile=.polyglot.pom.scala -Dfile="target/domino-java-${DOMINO_VERSION}-sources.jar" -Dclassifier=sources

  echo "Uploading javadoc"
  mvn -s ./staging-settings.xml gpg:sign-and-deploy-file -Durl="${URL}" -DrepositoryId=ossrh -DpomFile=.polyglot.pom.scala -Dfile="target/domino-java-${DOMINO_VERSION}-javadoc.jar" -Dclassifier=javadoc

fi
