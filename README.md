Project ODS
===
ODSCommons:
Bevat de implementaties en de interfaces
Bevat ene manifest.mf om als bundle te dienen en exporteert zijn impl en service packages. naar andere bundles

Server
Bevat de server implementatie en start een felix server op (runconfig via http://felix.apache.org/site/integrating-felix-with-eclipse.html)
bevat een activator voor de server side bundle
*laad dosgi in (consumeert odscommons packages)

Er is een build script voorzien dat odscommons build en zichzelf build om dan als jars met ene correcte manifest in de bundles map te steken voor automatisch opstarten.

Android:
app die een kleine gui geeft
Laad bundles in via de felixmanager met jars die zich in res/raw bevinden
==> MErk op dat je deze jars nog moet dexify'n

DOSGIclient
Voldoet aan de eisen van dosgi om via een xml data op te halen (OSGi-INF)
Heeft ene activator met tracker zoals in het voorbeeld
==> werkt nog niet, de activator lijkt niet opgeroepen te worden of de remote niet te vinden
Er is een build script aanwezig dat een jar trekt en zichzelf in res/raw van de android app plaats met de nodige manifests. Deze moet echter daarna nog gedexified worden