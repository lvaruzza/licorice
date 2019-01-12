if [ "project.version" -ot "pom.xml" ]; then
  echo updating project.version
  mvn help:evaluate -Dexpression=project.version -q -DforceStdout > project.version 2>/dev/null
fi

VERSION=`cat project.version`
java -jar target/licorice-$VERSION-jar-with-dependencies.jar $*

