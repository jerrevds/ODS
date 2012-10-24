@ECHO OFF
echo.
echo.NEED TO HAVE dex and aapt in classpath !!!
echo.
cd dex

set PWD=%~dp0

for /r %%i in (*) do (
 echo.
 echo.[[[ FILE %%i ]]]
 echo.
 dx --dex --core-library --output="%PWD%\classes.dex" "%%i"
 @ECHO OFF
 aapt add "%%i" "%PWD%\classes.dex"
 del "%PWD%\classes.dex"
)
