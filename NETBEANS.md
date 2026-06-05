# Abrir IP Messenger en NetBeans

## Requisitos

- NetBeans 22 o superior (con soporte Java SE)
- JDK 21 o superior (el proyecto está configurado para Java 21)

## Nota sobre `nbproject/`

Las carpetas `nbproject/` **no se suben al repositorio** (están en `.gitignore`). Ahí vive la configuración local: JDK elegido, rutas absolutas de librerías, usuario de NetBeans, etc.

Al clonar, NetBeans puede regenerar `nbproject/` vacío o con valores por defecto incorrectos (por ejemplo Gson en una ruta que no existe). Usa las plantillas del repo:

| Proyecto | Plantilla (en el repo) | Copiar a (local, ignorado por git) |
|----------|------------------------|-------------------------------------|
| Servidor | `server/IPMessengerServer/project.properties.example` | `server/IPMessengerServer/nbproject/project.properties` |
| Cliente  | `client/IPMessengerClient/project.properties.example` | `client/IPMessengerClient/nbproject/project.properties` |

**Windows (PowerShell), desde la raíz del repo:**

```powershell
Copy-Item server\IPMessengerServer\project.properties.example server\IPMessengerServer\nbproject\project.properties -Force
Copy-Item client\IPMessengerClient\project.properties.example client\IPMessengerClient\nbproject\project.properties -Force
```

**Linux / macOS:**

```bash
cp server/IPMessengerServer/project.properties.example server/IPMessengerServer/nbproject/project.properties
cp client/IPMessengerClient/project.properties.example client/IPMessengerClient/nbproject/project.properties
```

Valores clave en la plantilla del servidor:

- `main.class=main.Main`
- `javac.source=21` y `javac.target=21`
- Librerías relativas: `../../lib/gson-2.11.0.jar` y `../../lib/sqlite-jdbc-3.47.2.0.jar`

Si necesitas rutas absolutas en tu máquina, edita solo `nbproject/project.properties`, nunca el `.example`.

## Pasos

1. Abre NetBeans.
2. **Archivo → Abrir proyecto** y selecciona `server/IPMessengerServer`.
3. Repite con **Archivo → Abrir proyecto** para `client/IPMessengerClient`.
4. Copia las plantillas `project.properties.example` a `nbproject/project.properties` (ver tabla arriba).
5. **Clean and Build** en ambos proyectos.

6. Las dependencias externas están en `lib/`:
   - `gson-2.11.0.jar`
   - `sqlite-jdbc-3.47.2.0.jar` (solo servidor)

7. Ejecutar:
   - **Servidor:** clic derecho en `IPMessengerServer` → **Ejecutar** (clase principal: `main.Main`).
   - **Cliente:** clic derecho en `IPMessengerClient` → **Ejecutar** (clase principal: `Main`).

## Configuración del servidor

Puerto y base de datos en `server/IPMessengerServer/src/config.properties`:

```
server.port=12345
db.url=jdbc:sqlite:messenger.db
```

La base de datos SQLite se crea automáticamente al iniciar el servidor.

## Uso en hotspot / red local

1. Ejecuta el servidor en la PC que comparte internet o hotspot.
2. En la consola verás las IPs disponibles, por ejemplo: `192.168.137.1:12345`.
3. En cada cliente usa esa IP en el campo **Servidor IP** (no uses `127.0.0.1` si el cliente está en otro equipo).
4. Permite el puerto `12345` en el firewall de Windows si no conecta.
5. Todos los usuarios registrados en esa red pueden iniciar sesión y chatear entre sí.
