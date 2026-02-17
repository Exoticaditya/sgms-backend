#!/bin/sh

set -euf

MVNW_REPOURL="${MVNW_REPOURL:-}"
MAVEN_PROJECTBASEDIR="${MAVEN_PROJECTBASEDIR:-$(pwd)}"

if [ -z "${MAVEN_WRAPPER_JAR:-}" ]; then
  MAVEN_WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
fi

if [ ! -f "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties" ]; then
  echo "Missing .mvn/wrapper/maven-wrapper.properties"
  exit 1
fi

distributionUrl=$(grep -E '^distributionUrl=' "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties" | cut -d'=' -f2-)
wrapperUrl=$(grep -E '^wrapperUrl=' "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties" | cut -d'=' -f2-)

if [ ! -f "$MAVEN_WRAPPER_JAR" ]; then
  echo "Downloading Maven Wrapper..."
  mkdir -p "$(dirname "$MAVEN_WRAPPER_JAR")"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "${wrapperUrl}" -o "$MAVEN_WRAPPER_JAR"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "${wrapperUrl}" -O "$MAVEN_WRAPPER_JAR"
  else
    echo "Neither curl nor wget is available to download Maven Wrapper."
    exit 1
  fi
fi

JAVA_EXEC="${JAVA_HOME:-}/bin/java"
if [ ! -x "$JAVA_EXEC" ]; then
  JAVA_EXEC="java"
fi

exec "$JAVA_EXEC" -classpath "$MAVEN_WRAPPER_JAR" -Dmaven.multiModuleProjectDirectory="$MAVEN_PROJECTBASEDIR" org.apache.maven.wrapper.MavenWrapperMain "$@"
