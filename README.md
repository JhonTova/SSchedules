<div align="center">

# ⏰ SSchedules

### Automatiza tu servidor. Comandos y anuncios en el momento exacto.

Programa mensajes, broadcasts, títulos y comandos por tiempo, con reglas potentes.

[![build](https://github.com/JhonTova/SSchedules/actions/workflows/build.yml/badge.svg)](https://github.com/JhonTova/SSchedules/actions/workflows/build.yml) [![release](https://img.shields.io/github/v/release/JhonTova/SSchedules?label=release)](https://github.com/JhonTova/SSchedules/releases/latest) ![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11%2B-62B47A) ![Java](https://img.shields.io/badge/Java-21-E76F00)

</div>

---

## Características

- **Programación por tiempo** — ejecuta acciones a la hora, el día o el intervalo que definas.
- **Acciones integradas**: `msg!`, `broadcast!`, `actionbar!`, `title!`, `subtitle!`, `kickall!`.
- **Modos de selección** de comandos: aleatorio, aleatorio con repetición, o lineal.
- **Condiciones**: mínimo de jugadores, costo de dinero o experiencia, permisos.
- **Horarios de exclusión** — evita que algo se ejecute en ciertas franjas.
- **Retrasos y cadenas** de comandos (`delay!`, `;;`).
- Soporta placeholders de PlaceholderAPI.

## Comandos

| Comando | Acción |
|---|---|
| `/sschedules` | Gestión de los schedules. |

## Requisitos

Requiere **[SiriusCore](https://github.com/JhonTova/SiriusCore)**. PlaceholderAPI opcional.

## Instalación

`SiriusCore.jar` + `SSchedules.jar` en `plugins/`. Spigot / Paper / Purpur 1.21.11+.

## Descarga

El `.jar` de cada versión está en [Releases](https://github.com/JhonTova/SSchedules/releases/latest).

## Compilar desde el código

Requiere Java 21 y Maven. SiriusCore no está en un repositorio Maven público, así que
primero se instala en local la versión que declara el `pom.xml`:

```bash
git clone --branch v1.1.1 https://github.com/JhonTova/SiriusCore.git
mvn -f SiriusCore/pom.xml install -DskipTests
mvn package        # en este repositorio; el .jar queda en target/
```

Cada push y pull request se compila en GitHub Actions. Al subir una tag `v*`, el `.jar`
se adjunta solo a la release.
