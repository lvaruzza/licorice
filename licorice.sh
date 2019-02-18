D=`dirname $0`
PV=$D/project.version

echo $PV

if [ "PV" -ot "$D/pom.xml" ]; then
  echo updating $PV
  pushd $D
  mvn help:evaluate -Dexpression=project.version -q -DforceStdout > $PV 2>/dev/null
  popd
fi

VERSION=`cat $PV`
java -jar $D/target/licorice-$VERSION-jar-with-dependencies.jar $*

