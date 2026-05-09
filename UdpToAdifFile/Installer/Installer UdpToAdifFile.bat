@echo off
setlocal

echo Questo programma richiede che Java Virtual Machine sia installata sul PC
echo e che le variabili d'ambiente siano correttamente impostate.
echo

echo Inserisci la directory di installazione:
set /p INSTALL_DIR=

REM Verifica se la directory esiste, altrimenti la crea
if not exist "%INSTALL_DIR%" (
echo La directory non esiste. Creazione in corso...
mkdir "%INSTALL_DIR%"
)

REM Copia tutto il contenuto della cartella JRE (origine) nella JRE di destinazione
echo Copia della JRE in corso...
xcopy "*.*" "%INSTALL_DIR%" /E /Y

echo Operazione completata!
pause