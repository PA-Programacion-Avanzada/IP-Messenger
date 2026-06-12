# IP Messenger – Sistema de Mensajería Avanzada en Java

[![Java](https://img.shields.io/badge/Java-11-blue.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![SQLite](https://img.shields.io/badge/DB-SQLite-07405E)](https://www.sqlite.org/)
[![Socket](https://img.shields.io/badge/Network-Sockets-orange)](https://docs.oracle.com/javase/8/docs/api/java/net/Socket.html)

Proyecto final para la asignatura **Programación Avanzada** – Cliente-servidor con chat en tiempo real, compresión LZ77, mensajes pendientes y gestión dinámica de grupos.

## 📋 Descripción

IP Messenger es una aplicación de mensajería instantánea escrita en Java. Implementa un servidor multihilo, una interfaz Swing en el cliente, persistencia en SQLite, y lógica compleja de mensajería (amigos, grupos y mensajería directa entre usuarios).  Los usuarios intercambian mensajes directamente uno‑a‑uno, ya sea entre amigos (persistidos) o con usuarios que no son amigos (mensajes temporales almacenados localmente).

## 🚀 Características principales

- Autenticación (login, registro, recuperación de cuenta tras 3 fallos)
- Mensajería directa entre usuarios (1 a 1) sin persistencia en el servidor; los mensajes entre usuarios que no son amigos se tratan como *mensajes temporales* y se almacenan localmente en la sesión del cliente.
- Amigos: solicitudes, aceptación/rechazo, mensajes pendientes
- Grupos: creación con 3 invitados, se activa con 2 aceptaciones, 1 rechazo elimina el grupo
- Todos los mensajes se comprimen con **LZ77** y se serializan en **JSON**
- Interfaz Swing con ventanas modales y panel de notificaciones, incluyendo una alerta agregada que muestra "Nuevos mensajes" con el número de mensajes recibidos.
## 🛠️ Tecnologías

- Java 21
- Java Swing (GUI)
- Sockets (java.net)
- ExecutorService (Thread Pool)
- SQLite (o MySQL)
- LZ77 + JSON (Gson/Jackson)

## 🗄️ Base de datos (SQL)

La base de datos `messenger.db` contiene las siguientes tablas:

- **Users** (id, username, password_hash, status, last_seen)
- **Friendships** (id, user_id, friend_id, status, created_at)
- **Groups** (id, name, creator_id, created_at)
- **GroupMembers** (id, group_id, user_id, status, joined_at)
- **Messages** (id, sender_id, receiver_type, receiver_id, content, status, timestamp)

Para ver el esquema completo, consulta `docs/schema.sql`.

## 📂 Estructura del proyecto
```
IP-Messenger/
├── .gitignore
├── README.md
├── LICENSE
├── docs/
├── server/ # Proyecto IPMessengerServer
│ └── src/com/ipmessenger/server/
│ │ ├── Main.java
│ │ ├── core/
│ │ ├── database/
│ │ ├── logic/
│ │ ├── network/
│ │ └── utils/
└── client/ # Proyecto IPMessengerClient
│ └── src/com/ipmessenger/client/
│ │ ├── Main.java
│ │ ├── ui/
│ │ ├── core/
│ │ ├── network/
│ │ └── models/
```

## 🏃 Cómo ejecutar

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/PA-Programacion-Avanzada/IP-Messenger.git
   cd IP-Messenger
   ```

2. Compilar y ejecutar el servidor (desde la carpeta server/):
   ```bash
	javac -d out src/com/ipmessenger/server/Main.java
	java -cp out com.ipmessenger.server.Main
   ```

3. Ejecutar el cliente (desde la carpeta client/):
   ```bash
	javac -d out src/com/ipmessenger/client/Main.java
	java -cp out com.ipmessenger.client.Main
   ```

## 📄 Licencia

MIT. Ver archivo `LICENSE`.

## 👨‍🏫 Asignatura

Programación Avanzada II – Centro de Enseñanza Técnica Industrial – Equipo de 10 personas.
