#!/usr/bin/env sh
# ----------------------------------------------------------------------------
# Maven Wrapper
# ----------------------------------------------------------------------------

MAVEN_PROJECTBASEDIR="$(cd "$(dirname "$0")" >/dev/null 2>&1 && pwd)"

if [ -n "$JAVA_HOME" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD=java
fi

exec "$JAVACMD" -cp "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar" -Dmaven.multiModuleProjectDirectory="$MAVEN_PROJECTBASEDIR" org.apache.maven.wrapper.MavenWrapperMain "$@"
