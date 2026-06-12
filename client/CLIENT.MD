# IP Messenger – Client

## Purpose
The **client** part of the *IP‑Messenger* project is a Swing GUI application that acts as the user‑facing front end of the instant messaging system.  It is responsible for:

1. **Authentication** – showing the login screen (`StartWindow`), the register dialog (`RegisterModal`) and the password‑reset dialog (`RecoverAccountModal`).
2. **Display** – presenting the main dashboard (`DashboardWindow`) where users can:
   - see conversations with friends
   - view all registered users
   - manage group memberships
   - accept / reject group invitations
3. **Interaction** – providing modal windows for real‑time chat with friends or groups (`FriendRequestModal`, `GroupInviteModal`, `ChatModal`).
4. **Temporary messaging** – a lightweight way to send data that is not persisted on the server (`TemporalMessageModal`).

The implementation is currently **purely UI‑only** – all of the UI widgets expose callback interfaces (`OnConnectListener`, `OnRegisterListener`, …) but the actual *networking* and *database* operations are missing.  Data is bound to the UI via dummy objects in `Main.java` for visual verification.

---

## Current Features

| Screen | State | Notes |
|--------|------|------|
| **Login** (`StartWindow`) | Functional | Fires `onConnect` callback, performs basic field validation. |
| **Register** (`RegisterModal`) | Functional | Exposes `onRegister`, `onCancel`, `onSwitchToLogin` callbacks. |
| **Dashboard** (`DashboardWindow`) | Fully visual | Shows three columns; HTML‑style list rendering; badge counters update when data is bound. |
| **Chat** (`ChatModal`) | Fully visual | Simulates three‑column view; provides `onFriendChatSelected`, `onGroupSelected`, `onUserAction`, etc. |
| **Invitation** (`GroupInviteModal`, `FriendRequestModal`) | UI only | Buttons trigger callbacks. |
| **Temporary Messages** (`TemporalMessageModal`) | UI only | Uses `JTextField` placeholder and a button dialog; no backend. |
| **Pending Messages** (`PendingMessagesModal`) | UI only | Displays a simple `JList` of message objects. |
| **Aggregated notifications** | UI only | When any new message arrives (friend, group, temporary or general), the client aggregates them and shows a single pop‑up window with the title "Nuevos mensajes" and the text "Tienes X mensajes nuevos" after a short delay. This replaces the previous behavior of opening a separate window per incoming message. |

No server communication is currently wired – all data is supplied manually in the client test code.

---

## How It Should Communicate with the Server

The original design envisions a *protocol* on top of TCP sockets managed by the `network` package.  Each client would maintain a single persistent connection to a server instance that offers the following services:

1. **Authentication** – Sent at connection time; server replies with success / failure + a session token.
2. **Chat** – A line‑based JSON frame for each message, optionally compressed with LZ77 before transmission.
3. **Friend / Group Operations** – Requests to add friends, invite to groups, accept / reject invitations, etc., all formatted as JSON commands.
4. **State Synchronisation** – Server pushes *pending messages* and *notifications* to the client as they arrive.

A lightweight **controller layer** (yet to be written) should sit between the UI and the `network` package.  The controller would:

* Convert user actions (button clicks, menu selections) into protocol messages that it sends over the socket.
* Parse responses / push notifications from the server and translate them into UI‑ready data (the POJOs used by `DashboardWindow`, `ChatModal`, etc.).
* Persist local state with SQLite (via the client‑side `models` package) where appropriate.

The client initial version should therefore expose **only the callback interfaces** in each UI component; the implementation of those callbacks will be filled in by the controller.  Using this separation allows unit‑testing of the UI independently from the networking code.

---

## Future Work

* Implement the **controller** and networking protocol.
* Wire up the DAOs and database on the client side for offline data (e.g., message history).
* Add authentication and authorization logic.
* Integrate the LZ77 compression and JSON serialization from the server side.
* Add unit tests for UI components and controller logic.

---