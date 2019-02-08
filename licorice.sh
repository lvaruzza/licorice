D=`dirname $0`

if [ "$D/project.version" -ot "$D/pom.xml" ]; then
  echo updating project.version
  mvn help:evaluate -Dexpression=project.version -q -DforceStdout > $D/project.version 2>/dev/null
fi

VERSION=`cat $D/project.version`
java -jar $D/target/licorice-$VERSION-jar-with-dependencies.jar $*

