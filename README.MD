# IP Messenger – Sistema de Mensajería Avanzada en Java

[![Java](https://img.shields.io/badge/Java-11-blue.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![SQLite](https://img.shields.io/badge/DB-SQLite-07405E)](https://www.sqlite.org/)
[![Socket](https://img.shields.io/badge/Network-Sockets-orange)](https://docs.oracle.com/javase/8/docs/api/java/net/Socket.html)

Proyecto final para la asignatura **Programación Avanzada** – Cliente-servidor con chat en tiempo real, compresión LZ77, mensajes pendientes y gestión dinámica de grupos.

## 📋 Descripción

IP Messenger es una aplicación de mensajería instantánea escrita en Java. Implementa un servidor multihilo, una interfaz Swing en el cliente, persistencia en SQLite, y lógica compleja de mensajería (amigos, grupos y chat general).

## 🚀 Características principales

- Autenticación (login, registro, recuperación de cuenta tras 3 fallos)
- Chat general (“Todos”) sin persistencia (solo en memoria)
- Amigos: solicitudes, aceptación/rechazo, mensajes pendientes
- Grupos: creación con 3 invitados, se activa con 2 aceptaciones, 1 rechazo elimina el grupo
- Todos los mensajes se comprimen con **LZ77** y se serializan en **JSON**
- Interfaz Swing con ventanas modales y panel de notificaciones

## 🛠️ Tecnologías

- Java 11
- Java Swing (GUI)
- Sockets (java.net)
- ExecutorService (Thread Pool)
- SQLite (o MySQL)
- LZ77 + JSON (Gson/Jackson)

## 🗄️ Base de datos (SQL)

La base de datos contiene las siguientes tablas:

- Users
- Friendships
- Groups
- GroupMembers
- Messages

Consulta el archivo `schema.sql` en `/docs` para ver la estructura completa.

## 📂 Estructura del proyecto
```
/src/main/java/com/messenger/
├── server/ → Servidor principal, ClientHandler, ThreadManager
├── client/ → UI Swing, LoginFrame, Dashboard
├── database/ → DAO, conexión DB, consultas SQL
├── model/ → POJOs: User, Group, Message, Friendship
├── network/ → Protocolo, compresor LZ77, JSONParser
└── service/ → Lógica de negocio (FriendsService, GroupService, PendingService)
```


## 🏃 Cómo ejecutar

1. Clonar el repositorio:
   {codigo}bash
   git clone https://github.com/TU-USUARIO/IP-Messenger.git
   cd IP-Messenger
   {codigo}

2. Compilar y ejecutar el servidor:
   {codigo}bash
   javac -d out src/main/java/com/messenger/server/Main.java
   java -cp out com.messenger.server.Main
   {codigo}

3. Ejecutar el cliente (tantas veces como se desee):
   {codigo}bash
   javac -d out src/main/java/com/messenger/client/Main.java
   java -cp out com.messenger.client.Main
   {codigo}

> La base de datos `messenger.db` se creará automáticamente al iniciar el servidor.

## 📄 Licencia

MIT. Ver archivo `LICENSE`.

## 👨‍🏫 Asignatura

Programación Avanzada – Universidad XYZ – Equipo de 10 personas.
