
These are the steps needed to sign your JAR files to be used with the Java Plug-In. You need a Java 2 VM and SDK to run and sign the files, respectively. You'll have to apply the included jpi.patch to take advantage of this API. It's not part of core EIRC because it's not compatible with JDK 1.1.


First time you'll have to generate your certificates:

keytool -genkey -keyalg rsa -alias eirc
keytool -selfcert -alias eirc -validity 365

Where eirc is an arbitrary alias and 365 is the number of days the certificate will be valid.


Then, everytime you change the sources:

build.sh sign
-- or --
ant sign

Remember to set your keystore password in the build.xml file, or the process will fail.


Javier Kohen <jkohen@users.sourceforge.net>
