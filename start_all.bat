@echo off
chcp 65001 >nul
setlocal EnableExtensions

rem Purpose: start the local backend and Vue3 admin application.
rem Author: DAMU
rem Created: 2026-07-23
rem Function: validate dependencies, start both services, and verify HTTP endpoints.

set "ROOT=%~dp0"
set "REQUIRED_JDK_MAJOR=25"
set "JDK_FOUND="
call :findJdk25
if not defined JDK_FOUND (
    echo [ERROR] JDK %REQUIRED_JDK_MAJOR% was not found.
    echo [HINT] Set JAVA_HOME to a JDK %REQUIRED_JDK_MAJOR% installation, or install it in a standard location.
    exit /b 1
)
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "SERVER_JAR=%ROOT%yudao-server\target\yudao-server.jar"
set "COMMON_JAR=%ROOT%yudao-framework\yudao-common\target\yudao-common-2026.06-jdk25-SNAPSHOT.jar"
set "FRONTEND_DIR=%ROOT%yudao-ui\yudao-ui-admin-vue3"
set "REDIS_SERVER=%ROOT%runtime\redis\redis-server.exe"
set "REDIS_CLI=%ROOT%runtime\redis\redis-cli.exe"
set "REDIS_CONFIG=%ROOT%runtime\redis\redis-6380.conf"
set "REDIS_PORT=6380"

echo.
echo ===== RuoYi-Vue-Pro Full Local Startup =====

echo [INFO] Using JDK %REQUIRED_JDK_MAJOR%: %JAVA_HOME%

if not exist "%REDIS_SERVER%" (
    where redis-server.exe >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] Redis server was not found. Install Redis or place it in runtime\redis.
        exit /b 1
    )
    for /f "delims=" %%I in ('where redis-server.exe') do set "REDIS_SERVER=%%I"
)

if not exist "%REDIS_CLI%" (
    where redis-cli.exe >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] Redis CLI was not found. Install Redis or place it in runtime\redis.
        exit /b 1
    )
    for /f "delims=" %%I in ('where redis-cli.exe') do set "REDIS_CLI=%%I"
)

if not exist "%REDIS_CONFIG%" (
    set "REDIS_START_ARGS=--port %REDIS_PORT%"
) else (
    set REDIS_START_ARGS="%REDIS_CONFIG%"
)

if "%TX_DB_PASSWORD%"=="" (
    echo [INPUT] Enter the local MySQL root password for ruoyi-vue-pro.
    set /p "TX_DB_PASSWORD=Password: "
)

if "%TX_DB_PASSWORD%"=="" (
    echo [ERROR] Database password cannot be empty.
    exit /b 1
)

rem Pass the local database password to Spring Boot only for this launcher process.
rem The value is not written to any project configuration file.
set "SPRING_DATASOURCE_DYNAMIC_DATASOURCE_MASTER_PASSWORD=%TX_DB_PASSWORD%"

powershell -NoProfile -Command "$jar = Get-Item '%SERVER_JAR%' -ErrorAction SilentlyContinue; if (-not $jar -or -not (& jar tf '%SERVER_JAR%' | Select-String '^BOOT-INF/' -Quiet)) { exit 0 }; $root = '%ROOT%'; $rootPom = Get-Item (Join-Path $root 'pom.xml'); $backendRoots = Get-ChildItem -LiteralPath $root -Directory | Where-Object { $_.Name -like 'yudao-*' -and $_.Name -ne 'yudao-ui' }; $newerSource = $backendRoots | ForEach-Object { Get-ChildItem -LiteralPath $_.FullName -Recurse -File -Include '*.java','pom.xml','*.yml','*.yaml','*.properties' | Where-Object { $_.FullName -notmatch '\\target\\' } } | Where-Object { $_.LastWriteTime -gt $jar.LastWriteTime } | Select-Object -First 1; if ($rootPom.LastWriteTime -gt $jar.LastWriteTime -or $newerSource) { exit 0 }; exit 1"
if not errorlevel 1 (
    echo [INFO] Backend executable Jar is missing or older than backend sources; building all backend modules...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "$env:JAVA_HOME = '%JAVA_HOME%'; $env:Path = \"$env:JAVA_HOME\bin;$env:Path\"; Set-Location '%ROOT%'; & mvn '-Dmaven.compiler.source=25' '-Dmaven.compiler.target=25' '-Dmaven.compiler.release=25' '-pl' 'yudao-server' '-am' 'package' '-Dmaven.test.skip=true'; exit $LASTEXITCODE"
    if errorlevel 1 (
        echo [ERROR] Backend rebuild failed. Review the Maven output above.
        pause
        exit /b 1
    )
)

if not exist "%FRONTEND_DIR%\node_modules" (
    where pnpm.cmd >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] pnpm was not found. Install Node.js and pnpm, then run this script again.
        exit /b 1
    )
    echo [INFO] Installing frontend dependencies for the first run...
    pushd "%FRONTEND_DIR%"
    call pnpm.cmd install --frozen-lockfile
    if errorlevel 1 (
        popd
        echo [ERROR] Frontend dependency installation failed.
        exit /b 1
    )
    popd
)

mysql -uroot -p"%TX_DB_PASSWORD%" -D ruoyi-vue-pro -N -e "SELECT 1;" >nul 2>&1
if errorlevel 1 (
    echo [ERROR] MySQL is unavailable or root cannot access ruoyi-vue-pro.
    exit /b 1
)

powershell -NoProfile -Command "if ((& '%REDIS_CLI%' -p %REDIS_PORT% ping) -eq 'PONG') { exit 0 } else { exit 1 }"
if errorlevel 1 (
    echo [INFO] Starting local Redis on port %REDIS_PORT%...
    if exist "%REDIS_CONFIG%" (
        powershell -NoProfile -Command "Start-Process -FilePath '%REDIS_SERVER%' -ArgumentList @('%REDIS_CONFIG%') -WorkingDirectory '%ROOT%runtime\redis' -WindowStyle Hidden"
    ) else (
        powershell -NoProfile -Command "Start-Process -FilePath '%REDIS_SERVER%' -ArgumentList @('--port', '%REDIS_PORT%') -WorkingDirectory '%ROOT%' -WindowStyle Hidden"
    )
    powershell -NoProfile -Command "$deadline = (Get-Date).AddSeconds(10); do { if ((& '%REDIS_CLI%' -p %REDIS_PORT% ping) -eq 'PONG') { exit 0 }; Start-Sleep -Milliseconds 500 } while ((Get-Date) -lt $deadline); exit 1"
    if errorlevel 1 (
        echo [ERROR] Redis failed to start. Review the Redis output and port %REDIS_PORT%.
        exit /b 1
    )
)

for /f "tokens=5" %%P in ('netstat -ano ^| findstr /r /c:":48080 .*LISTENING"') do set "BACKEND_PID=%%P"
if defined BACKEND_PID (
    echo [INFO] Backend port 48080 is used by process %BACKEND_PID%; skipping startup.
) else (
    rem Start Java directly in its own console. This avoids PowerShell closing the JVM
    rem when the launcher window exits after the health checks complete.
    start "RuoYi-Vue-Pro Backend" /D "%ROOT%" "%JAVA_HOME%\bin\java.exe" -jar "%SERVER_JAR%" "--spring.profiles.active=all-local"
)

echo [CHECK] Waiting up to 240 seconds for backend health...
rem Full-module startup initializes Flowable, AI and IoT components and can exceed two minutes.
powershell -NoProfile -Command "$deadline = (Get-Date).AddSeconds(240); do { try { if ((Invoke-WebRequest 'http://127.0.0.1:48080/actuator/health' -UseBasicParsing -TimeoutSec 2).StatusCode -eq 200) { exit 0 } } catch { }; Start-Sleep -Seconds 2 } while ((Get-Date) -lt $deadline); exit 1"
if errorlevel 1 (
    echo [ERROR] Backend health check timed out. Review the RuoYi-Vue-Pro Backend window.
    exit /b 1
)

rem Start the frontend only after backend APIs are ready. Otherwise the admin page can
rem request mall routes during startup and show a transient "module disabled" message.
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /r /c:":80 .*LISTENING"') do set "FRONTEND_PID=%%P"
if defined FRONTEND_PID (
    echo [INFO] Frontend port 80 is used by process %FRONTEND_PID%; skipping startup.
) else (
    echo [INFO] Backend is ready; starting frontend...
    start "RuoYi-Vue-Pro Frontend" /D "%FRONTEND_DIR%" powershell.exe -NoExit -NoProfile -ExecutionPolicy Bypass -Command "pnpm.cmd dev"
)

echo [CHECK] Waiting up to 60 seconds for frontend home page...
powershell -NoProfile -Command "$deadline = (Get-Date).AddSeconds(60); do { try { if ((Invoke-WebRequest 'http://127.0.0.1:80' -UseBasicParsing -TimeoutSec 2).StatusCode -eq 200) { exit 0 } } catch { }; Start-Sleep -Seconds 2 } while ((Get-Date) -lt $deadline); exit 1"
if errorlevel 1 (
    echo [ERROR] Frontend did not respond. Review the RuoYi-Vue-Pro Frontend window.
    exit /b 1
)

echo.
echo [SUCCESS] Backend: http://127.0.0.1:48080/swagger-ui
echo [SUCCESS] Frontend: http://127.0.0.1:80
endlocal
exit /b 0

:findJdk25
rem Prefer an already configured JAVA_HOME, then Java on PATH.
if defined JAVA_HOME call :useJdk25 "%JAVA_HOME%"
if defined JDK_FOUND exit /b 0

for /f "delims=" %%J in ('where java.exe 2^>nul') do (
    for %%D in ("%%~dpJ..") do call :useJdk25 "%%~fD"
    if defined JDK_FOUND exit /b 0
)

rem Check the standard install folders used by Oracle, Adoptium, Microsoft,
rem BellSoft, and Amazon Corretto distributions.
for %%D in (
    "%ProgramFiles%\Java\jdk-%REQUIRED_JDK_MAJOR%*"
    "%ProgramFiles%\Eclipse Adoptium\jdk-%REQUIRED_JDK_MAJOR%*"
    "%ProgramFiles%\Microsoft\jdk-%REQUIRED_JDK_MAJOR%*"
    "%ProgramFiles%\BellSoft\LibericaJDK-%REQUIRED_JDK_MAJOR%*"
    "%ProgramFiles%\Amazon Corretto\jdk%REQUIRED_JDK_MAJOR%*"
    "%LocalAppData%\Programs\Eclipse Adoptium\jdk-%REQUIRED_JDK_MAJOR%*"
) do (
    call :useJdk25 "%%~fD"
    if defined JDK_FOUND exit /b 0
)

rem Oracle and several installers register the JDK home here.
for /f "tokens=2,*" %%A in ('reg query "HKLM\SOFTWARE\JavaSoft\JDK\%REQUIRED_JDK_MAJOR%" /v JavaHome 2^>nul ^| findstr /i "JavaHome"') do call :useJdk25 "%%B"
if defined JDK_FOUND exit /b 0
for /f "tokens=2,*" %%A in ('reg query "HKLM\SOFTWARE\WOW6432Node\JavaSoft\JDK\%REQUIRED_JDK_MAJOR%" /v JavaHome 2^>nul ^| findstr /i "JavaHome"') do call :useJdk25 "%%B"
exit /b 0

:useJdk25
if "%~1"=="" exit /b 1
if not exist "%~1\bin\java.exe" exit /b 1
"%~1\bin\java.exe" -version 2>&1 | findstr /r /c:"%REQUIRED_JDK_MAJOR%\." >nul
if errorlevel 1 exit /b 1
set "JAVA_HOME=%~1"
set "JDK_FOUND=1"
exit /b 0
