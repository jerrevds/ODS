for /r %%i in (*) do (
 dx --dex --core-library --output="D:\Universiteit\Master 1\Ontwerp van gedistribueerde software\GIT\OSGi_android\res\raw\classes.dex" "%%i"
 aapt add "%%i" "D:\Universiteit\Master 1\Ontwerp van gedistribueerde software\GIT\OSGi_android\res\raw\classes.dex"
)