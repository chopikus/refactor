
These are the steps needed to sign your JAR files to be used with Netscape Navigator. You'll need the signtool utility available at Netscape's site.


First time you'll have to generate your certificates:

-Set Netscape Navigator's certificates db password on the browser.
signtool -G eirc


Then, everytime you change the sources:

signtool -k eirc -Z EIRC.jar build/classes


Check http://developer.iPlanet.com/docs/manuals/signedobj/signtool/index.htm for more information.


Javier Kohen <jkohen@tough.com>
