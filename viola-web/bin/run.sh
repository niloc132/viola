#!/bin/bash

#
#  run and rerun:
#
# this builds the maven project and runs classes via within specified by
# the commandline, including any switches in any order desired.
#
#
# if script is named rerun.sh it skips the build and assumes the
# classpath is also pre-baked via last time.
#
#


set -x

#relaxfactory is for hard realtime, except when it's not, then it needs these env vars
export RXF_REALTIME_UNIT=MINUTES RXF_REALTIME_CUTOFF=10

[[ -r $HOME/.m2/settings.xml ]] || {
echo "one-time writing your $HOME/.m2/settings.xml"
mkdir -p $HOME/.m2/

cat >$HOME/.m2/settings.xml <<EOF
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">
<servers>
   <server>
    <id>support-commercial-snapshot</id>
    <username>xxxxx@xxxx.xxx</username>
    <password>xxxxxxxx</password>
  </server>
 </servers>
</settings>
EOF
}

echo " * you must be using oracle jdk."
echo " * you must have no spaces in any of your classpaths."
[[ $(basename $0) == rerun* ]] && omit=true;
[[ $omit ]] || mvn install
[[ $omit ]] || mvn dependency:build-classpath -Dmdep.outputFile=target/cp.txt
[[ $omit ]] || {pushd target;ln -s web static;popd}
{
pre=$PWD
pn=$(grep '<artifactId>' pom.xml |head -n 1|cut -f2 -d ">"|cut -f1 -d"<")
vers=$(grep '<version>' pom.xml  |head -n 1|cut -f2 -d ">"|cut -f1 -d"<")
CP=$(cat target/cp.txt)
pushd target
${JAVA_HOME}/bin/java -cp $CP:WEB-INF/classes -Drxf.url=http://localhost:8000 -Drxf.couch.prefix=http://weatherwax.colinalworth.com:8003/  -Drxf.cached.threadpool="true" -jar $pn-$vers.jar
}
