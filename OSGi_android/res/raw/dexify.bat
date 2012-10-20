for /r %%i in (*) do (
 dx --dex --output=C:\Users\jerrevds\workspace\ODSFinal\OSGi_android\res\raw\classes.dex %%i
 aapt add %%i C:\Users\jerrevds\workspace\ODSFinal\OSGi_android\res\raw\classes.dex
)