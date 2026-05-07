# 🔍 SessionTracer

SessionTracer is a blazing-fast, fully asynchronous anti-alt account plugin for Paper servers. Control IP limits, whitelist players, and notify your staff—with zero server lag.

## ✨ Quick Features
- 🚀 **Zero Lag**: 100% async SQLite (configurable) database & Caffeine caching.
- 🚨 **Smart Alerts**: Live staff notifications when known alts join.
- 📋 **Interactive Whitelist**: Manage allowed alts easily via clickable chat buttons.
- 🔄 **Live-Reloads**: Adapts to new IP limits on the fly.

## 🔜 Coming Soon
- A fully interactive chest-based Admin GUI to manage IPs, limits, and whitelists without typing commands! 
- Implement various Datasources (e.g. Redis, MongoDB, MySQL).
- Auto Tab Completion on the `/trace` command!

## 📥 Installation
1. Drop `SessionTracer.jar` into your `/plugins/` folder.
2. Restart the server.
3. Configure your limits and messages in the generated `config.yaml`.

> ⚠️ **Proxy Users:** Ensure IP-Forwarding (e.g., Velocity, BungeeCord, TCPShield) is enabled!

## 💻 Commands
*Staff members need the `trace.alert` permission to see alt warnings in chat.*

| Command | Description |
| :--- | :--- |
| `/trace reload` | Reloads config & applies new limits instantly. |
| `/trace whitelist` | Shows whitelisted players (with clickable `[DELETE]`). |
| `/trace whitelist add <Name>` | Bypasses the IP limit for a player. |
| `/trace whitelist remove <Name>`| Removes a player from the whitelist. |

## 🛠️ Developer API
SessionTracer fires an asynchronous `DuplicateSessionEvent` right before a player joins. You can listen to this event in your own plugins to trigger custom actions, like sending Discord webhooks. 

*(There are also easy ways to implement other Datasources, just look in the source code!)*
