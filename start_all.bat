@echo off
chcp 65001 >nul
setlocal EnableExtensions

rem Purpose: start the local backend and Vue3 admin application.
rem Author: DAMU
rem Created: 2026-07-23
rem Function: validate dependencies, start both services, and verify HTTP endpoints.

set "ROOT=%~dp0"
set "JAVA_HOME=C:\Program Files\Java\jdk-25"
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

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERROR] JDK 25 not found: %JAVA_HOME%
    exit /b 1
)

if not exist "%REDIS_SERVER%" (
    echo [ERROR] Redis server not found: %REDIS_SERVER%
    exit /b 1
)

if not exist "%REDIS_CLI%" (
    echo [ERROR] Redis CLI not found: %REDIS_CLI%
    exit /b 1
)

if not exist "%REDIS_CONFIG%" (
    echo [ERROR] Redis config not found: %REDIS_CONFIG%
    exit /b 1
)

powershell -NoProfile -Command "$jar = Get-Item '%SERVER_JAR%' -ErrorAction SilentlyContinue; if (-not $jar -or -not (& jar tf '%SERVER_JAR%' | Select-String '^BOOT-INF/' -Quiet)) { exit 0 }; exit 1"
if not errorlevel 1 (
    echo [INFO] Backend executable Jar is missing; building all backend modules...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "$env:JAVA_HOME = '%JAVA_HOME%'; $env:Path = \"$env:JAVA_HOME\bin;$env:Path\"; Set-Location '%ROOT%'; & mvn '-Dmaven.compiler.source=25' '-Dmaven.compiler.target=25' '-Dmaven.compiler.release=25' '-pl' 'yudao-server' '-am' 'package' '-Dmaven.test.skip=true'; exit $LASTEXITCODE"
    if errorlevel 1 (
        echo [ERROR] Backend rebuild failed. Review the Maven output above.
        pause
        exit /b 1
    )
)

if not exist "%FRONTEND_DIR%\node_modules" (
    echo [ERROR] Frontend dependency directory not found: %FRONTEND_DIR%\node_modules
    echo Run in the frontend directory: pnpm install --frozen-lockfile
    exit /b 1
)

mysql -uroot -pltr041204 -D ruoyi-vue-pro -N -e "SELECT 1;" >nul 2>&1
if errorlevel 1 (
    echo [ERROR] MySQL is unavailable or root cannot access ruoyi-vue-pro.
    exit /b 1
)

powershell -NoProfile -Command "if ((& '%REDIS_CLI%' -p %REDIS_PORT% ping) -eq 'PONG') { exit 0 } else { exit 1 }"
if errorlevel 1 (
    echo [INFO] Starting local Redis on port %REDIS_PORT%...
    start "RuoYi-Vue-Pro Redis" /B "%REDIS_SERVER%" "%REDIS_CONFIG%"
    powershell -NoProfile -Command "$deadline = (Get-Date).AddSeconds(10); do { if ((& '%REDIS_CLI%' -p %REDIS_PORT% ping) -eq 'PONG') { exit 0 }; Start-Sleep -Milliseconds 500 } while ((Get-Date) -lt $deadline); exit 1"
    if errorlevel 1 (
        echo [ERROR] Redis failed to start. Review runtime\redis\redis-6380.log.
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

for /f "tokens=5" %%P in ('netstat -ano ^| findstr /r /c:":80 .*LISTENING"') do set "FRONTEND_PID=%%P"
if defined FRONTEND_PID (
    echo [INFO] Frontend port 80 is used by process %FRONTEND_PID%; skipping startup.
) else (
    start "RuoYi-Vue-Pro Frontend" /D "%FRONTEND_DIR%" powershell.exe -NoExit -NoProfile -ExecutionPolicy Bypass -Command "pnpm.cmd dev"
)

echo [CHECK] Waiting up to 240 seconds for backend health...
rem Full-module startup initializes Flowable, AI and IoT components and can exceed two minutes.
powershell -NoProfile -Command "$deadline = (Get-Date).AddSeconds(240); do { try { if ((Invoke-WebRequest 'http://127.0.0.1:48080/actuator/health' -UseBasicParsing -TimeoutSec 2).StatusCode -eq 200) { exit 0 } } catch { }; Start-Sleep -Seconds 2 } while ((Get-Date) -lt $deadline); exit 1"
if errorlevel 1 (
    echo [ERROR] Backend health check timed out. Review the RuoYi-Vue-Pro Backend window.
    exit /b 1
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
