
These are the steps needed to sign your CAB files to be used with Microsoft Internet Explorer. You'll need the utilities included in Microsoft's Java SDK (2.0 or later), and no, I can't redistribute them.


First time you'll have to generate your certificates:

makecert -sk eirc -n "CN=My Company Name" EIRCCert.cer
cert2spc EIRCCert.cer EIRCCert.spc


Then, everytime you change the sources:

cabarc -r -p -P build\classes\ N eirc.cab build\classes\*.*
cabarc -r -p -P src\gfx\ N eirc-gfx.cab src\gfx\*.*
signcode -j javasign.dll -jp eircsign.ini -spc EIRCCert.spc -k eirc eirc.cab


Javier Kohen <jkohen@tough.com>
