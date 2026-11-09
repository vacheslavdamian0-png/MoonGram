# MoonGram Mini App

Starter Telegram Mini App with profile, badges and a protected admin panel.

## Features
- Server-side Telegram Web App initData HMAC verification.
- Owner access is based on numeric Telegram IDs, never usernames. Staff access is separate.
- Owners can issue one-time staff invitation tokens bound to a target Telegram ID. Tokens expire after 10 minutes; only SHA-256 hashes are stored; redemption is single-use.
- Staff can manage badges and MoonGram IDs, but cannot issue new staff tokens or promote themselves to owner.
- Assign team and supporter badges; change unique MoonGram IDs.
- Badge descriptions: team = “Этот пользователь член команды MoonGram”; supporter = “Этот пользователь поддержал разработку MoonGram”.
- SQLite persistence.

## VPS setup
1. Install Node.js 20+.
2. Copy .env.example to .env and set BOT_TOKEN to a NEW token from BotFather, MINIAPP_URL to the HTTPS URL, and ADMIN_IDS to comma-separated numeric owner Telegram IDs.
3. Run npm install and npm start.
4. Put the service behind HTTPS and configure the bot Mini App URL in BotFather.
5. Keep .env and the database out of Git.

The previously exposed bot token must be revoked in BotFather. Never paste the replacement token into chat or commit it. This starter is not yet production-hardened: add rate limiting, audit logs, backups, token revocation, and deployment configuration before public launch. Keep owner IDs limited to trusted owners.
