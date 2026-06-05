# Abrir IP Messenger en NetBeans

## Requisitos

- NetBeans 22 o superior (con soporte Java SE)
- JDK 21 o superior (el proyecto está configurado para Java 21)

## Nota sobre `nbproject/`

Las carpetas `nbproject/` **no se suben al repositorio** (están en `.gitignore`). Cada desarrollador las mantiene en local. Si al clonar no existen, NetBeans las regenera al abrir el proyecto Java que tiene `build.xml`.

Configuración local recomendada del servidor (`server/IPMessengerServer/nbproject/project.properties`):

- `main.class=main.Main`
- `javac.source=21` y `javac.target=21`
- Librerías en `lib/`: `gson-2.11.0.jar` y `sqlite-jdbc-3.47.2.0.jar`

## Pasos

1. Abre NetBeans.
2. **Archivo → Abrir proyecto** y selecciona `server/IPMessengerServer`.
3. Repite con **Archivo → Abrir proyecto** para `client/IPMessengerClient`.

   También puedes abrir el grupo completo desde la carpeta raíz `IP-Messenger` (NetBeans detectará `nbproject/projectgroup.properties`).

4. Las dependencias externas están en `lib/`:
   - `gson-2.11.0.jar`
   - `sqlite-jdbc-3.47.2.0.jar` (solo servidor)

5. Ejecutar:
   - **Servidor:** clic derecho en `IPMessengerServer` → **Ejecutar** (clase principal: `main.Main`).
   - **Cliente:** clic derecho en `IPMessengerClient` → **Ejecutar** (clase principal: `Main`).

## Configuración del servidor

Puerto y base de datos en `server/IPMessengerServer/src/config.properties`:

```
server.port=12345
db.url=jdbc:sqlite:messenger.db
```

La base de datos SQLite se crea automáticamente al iniciar el servidor.
