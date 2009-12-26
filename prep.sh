#!/bin/sh

mkdir tmp
cd tmp

wget http://heanet.dl.sourceforge.net/project/jvi/jvi/1.2-NB6.5_6.7/nbvi-1.2.6.zip
unzip nbvi-1.2.6.zip
cd nbvi-1.2.6
unzip com-raelity-jvi.nbm

mvn install:install-file -DgroupId=com.raelity -DartifactId=jvi -Dversion=1.2.6 -Dfile=netbeans/modules/ext/jvi-project.jar -Dpackaging=jar -DgeneratePom=true
