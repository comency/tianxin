# IoT TDengine Local Enablement Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enable the RuoYi IoT TDengine datasource in the local all-module profile and verify backend initialization against the locally running TDengine server.

**Architecture:** Keep the shared `application-local.yaml` template unchanged. Add the `tdengine` dynamic datasource only in `application-all-local.yaml`, using the installed WebSocket endpoint on `127.0.0.1:6041`; enable the guarded IoT table initializer. Initialize the `ruoyi_vue_pro` TDengine database before backend startup.

**Tech Stack:** Spring Boot 4, dynamic-datasource, TDengine 3.4.2.2 Enterprise, taos-jdbcdriver 3.8.4, Maven, Java 25.

## Global Constraints

- All edited text files use UTF-8.
- Preserve existing MySQL, Redis, and external-service exclusion settings.
- Use the existing `tdengine` dynamic datasource name required by `@TDengineDS`.
- Do not modify TDengine service binaries or existing time-series data.

---

### Task 1: Configure the Local TDengine Datasource

**Files:**
- Modify: `yudao-server/src/main/resources/application-all-local.yaml`

**Interfaces:**
- Consumes: TDengine WebSocket endpoint `jdbc:TAOS-WS://127.0.0.1:6041/ruoyi_vue_pro`.
- Produces: Dynamic datasource named `tdengine` for `@DS("tdengine")` mappers.

- [ ] Add `spring.datasource.dynamic.datasource.tdengine` with WebSocket driver, root credentials, lazy startup, and TDengine validation query.
- [ ] Set `yudao.iot.tdengine.enabled` to `true`.
- [ ] Confirm YAML parsing and property hierarchy preserve the `master` datasource.

### Task 2: Initialize and Verify the Time-Series Database

**Files:**
- Runtime state only: local TDengine database `ruoyi_vue_pro`.

**Interfaces:**
- Consumes: `C:\TDengine\taos.exe` authenticated as the local root user.
- Produces: Database `ruoyi_vue_pro` available to the JDBC driver.

- [ ] Run `CREATE DATABASE IF NOT EXISTS ruoyi_vue_pro`.
- [ ] Run `SHOW DATABASES` and confirm `ruoyi_vue_pro` is listed.
- [ ] Use a JDBC-backed application startup to prove the mapper routes to TDengine rather than MySQL.

### Task 3: Build and Start the IoT-Enabled Backend

**Files:**
- Build output: `yudao-server/target/yudao-server.jar`.

**Interfaces:**
- Consumes: `application-all-local.yaml`, MySQL, Redis, and TDengine.
- Produces: Backend on port `48080` with TDengine device message stable initialization complete.

- [ ] Run Maven package for the server and dependent enabled modules.
- [ ] Start the packaged backend with the `all-local` profile.
- [ ] Confirm the process stays running and logs contain no TDengine initialization error.
- [ ] Query TDengine for the expected IoT stable and check `http://127.0.0.1:48080/actuator/health` when available.
