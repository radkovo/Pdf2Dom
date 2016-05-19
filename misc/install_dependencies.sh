#! /bin/sh
set -ev

TMP=~/tmp
PARSER=$TMP/jStyleParser
CSSBOX=$TMP/CSSBox

mkdir $TMP
mkdir $CSSBOX
mkdir $PARSER

git clone https://github.com/radkovo/jStyleParser.git $PARSER
(cd $PARSER; mvn install -DskipTests=true -Dmaven.javadoc.skip=true -B -V)
git clone https://github.com/radkovo/CSSBox.git $CSSBOX
(cd $CSSBOX; mvn install -DskipTests=true -Dmaven.javadoc.skip=true -B -V)
