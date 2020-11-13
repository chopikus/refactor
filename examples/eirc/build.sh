#!/bin/sh

REALANTHOME=$ANT_HOME
ANT_HOME=.
export ANT_HOME

if test ! -f lib/ant.jar -o  ! -x bin/ant -o ! -x bin/antRun ; then
  echo Make sure you\'ve got the EIRC-lib package and that you\'re running this script from the package\'s root dir.
  exit 1
fi    

if [ "$REALANTHOME" != "" ] ; then
  ANT_INSTALL="-Dant.install $REALANTHOME"
fi

bin/ant $ANT_INSTALL $*

