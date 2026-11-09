# MoonGram Mini App

Starter Telegram Mini App with profile, badges and a protected admin panel.

## Features
- Server-side Telegram Web App initData HMAC verification.
- Administrator access based on numeric Telegram IDs, never usernames.
- Admin can issue one-time invitation tokens bound to a target Telegram ID. Tokens expire after 10 minutes; only SHA-256 hashes are stored; redemption is single-use.
- Assign team and supporter badges; change unique MoonGram IDs.
- Badge descriptions: team = “Этот пользователь член команды MoonGram”; supporter = “Этот пользователь поддержал разработку MoonGram”.
- SQLite persistence.

## VPS setup
1. Install Node.js 20+.
2. Copy .env.example to .env and set BOT_TOKEN to a NEW token from BotFather, MINIAPP_URL to the HTTPS URL, and ADMIN_IDS to comma-separated numeric Telegram IDs.
3. Run npm install and npm start.
4. Put the service behind HTTPS and configure the bot Mini App URL in BotFather.
5. Keep .env and the database out of Git.

The previously exposed bot token must be revoked in BotFather. Never paste the replacement token into chat or commit it. This starter is not yet production-hardened: add rate limiting, audit logs, backups, and deployment configuration before public launch.
