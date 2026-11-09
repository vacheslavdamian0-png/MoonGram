'use strict';
const crypto=require('node:crypto'),fs=require('node:fs'),path=require('node:path');
const express=require('express'),Database=require('better-sqlite3');
const app=express(); app.disable('x-powered-by'); app.use(express.json({limit:'32kb'}));
const PORT=Number(process.env.PORT||3000),BOT_TOKEN=process.env.BOT_TOKEN||'';
const ADMIN_IDS=new Set((process.env.ADMIN_IDS||'').split(',').map(x=>x.trim()).filter(x=>/^\d+$/.test(x)));
const file=path.resolve(process.env.DATABASE_FILE||'./data/moongram.sqlite'); fs.mkdirSync(path.dirname(file),{recursive:true});
const db=new Database(file); db.pragma('journal_mode = WAL');
db.exec("CREATE TABLE IF NOT EXISTS profiles(user_id TEXT PRIMARY KEY,moon_id TEXT UNIQUE,display_name TEXT NOT NULL DEFAULT '',username TEXT NOT NULL DEFAULT '',created_at INTEGER NOT NULL); CREATE TABLE IF NOT EXISTS badges(id INTEGER PRIMARY KEY AUTOINCREMENT,user_id TEXT NOT NULL,code TEXT NOT NULL,kind TEXT NOT NULL CHECK(kind IN ('team','supporter')),title TEXT NOT NULL,created_at INTEGER NOT NULL,UNIQUE(user_id,code)); CREATE TABLE IF NOT EXISTS panel_tokens(token_hash TEXT PRIMARY KEY,target_user_id TEXT NOT NULL,expires_at INTEGER NOT NULL,used_at INTEGER); CREATE TABLE IF NOT EXISTS panel_admins(user_id TEXT PRIMARY KEY,granted_at INTEGER NOT NULL)");
function verify(raw){
 if(!BOT_TOKEN||!raw||raw.length>8192)return null;
 const p=new URLSearchParams(raw),hash=p.get('hash'),authDate=Number(p.get('auth_date')),u=p.get('user');
 if(!hash||!u||!Number.isFinite(authDate))return null;
 const now=Math.floor(Date.now()/1000); if(authDate>now+30||now-authDate>86400)return null;
 p.delete('hash');p.delete('signature');
 const check=[...p.entries()].sort((a,b)=>a[0].localeCompare(b[0])).map(x=>x[0]+'='+x[1]).join('\n');
 const secret=crypto.createHmac('sha256','WebAppData').update(BOT_TOKEN).digest();
 const expected=crypto.createHmac('sha256',secret).update(check).digest('hex');
 const a=Buffer.from(expected,'hex'),b=Buffer.from(hash,'hex'); if(a.length!==b.length||!crypto.timingSafeEqual(a,b))return null;
 try{const user=JSON.parse(u);if(!/^\d+$/.test(String(user.id)))return null;return{id:String(user.id),username:String(user.username||''),name:[user.first_name,user.last_name].filter(Boolean).join(' ')}}catch{return null}
}
function auth(req,res,next){const user=verify(req.get('X-Telegram-Init-Data')||'');if(!user)return res.status(401).json({error:'Откройте Mini App через Telegram. Проверка не пройдена.'});req.user=user;db.prepare("INSERT INTO profiles(user_id,display_name,username,created_at) VALUES(?,?,?,?) ON CONFLICT(user_id) DO UPDATE SET display_name=excluded.display_name,username=excluded.username").run(user.id,user.name,user.username,Date.now());next()}
function isAdmin(id){return ADMIN_IDS.has(id)||!!db.prepare('SELECT 1 FROM panel_admins WHERE user_id=?').get(id)}
function admin(req,res,next){if(!isAdmin(req.user.id))return res.status(403).json({error:'Недостаточно прав администратора.'});next()}
function clean(v,n){return typeof v==='string'?v.trim().slice(0,n):''}
function profile(id){return{profile:db.prepare('SELECT user_id,moon_id,display_name,username FROM profiles WHERE user_id=?').get(id)||{user_id:id,moon_id:null,display_name:'',username:''},badges:db.prepare('SELECT code,kind,title FROM badges WHERE user_id=? ORDER BY id').all(id)}}
app.use(express.static(path.join(__dirname,'public')));
app.get('/health',(_,res)=>res.json({ok:true,service:'moongram-miniapp'}));
app.get('/api/me',auth,(req,res)=>res.json({user:req.user,isAdmin:isAdmin(req.user.id),profile:profile(req.user.id)}));
app.get('/api/profile/:id',auth,(req,res)=>/^\d+$/.test(req.params.id)?res.json(profile(req.params.id)):res.status(400).json({error:'Некорректный ID'}));
app.post('/api/admin/tokens',auth,admin,(req,res)=>{const target=String(req.body?.targetUserId||'');if(!/^\d+$/.test(target))return res.status(400).json({error:'Нужен числовой Telegram ID'});const token=crypto.randomBytes(24).toString('base64url'),hash=crypto.createHash('sha256').update(token).digest('hex'),expires=Date.now()+600000;db.prepare('INSERT INTO panel_tokens(token_hash,target_user_id,expires_at) VALUES(?,?,?)').run(hash,target,expires);res.json({token,targetUserId:target,expiresAt:expires})});
app.post('/api/panel/redeem',auth,(req,res)=>{const token=clean(req.body?.token,128);const hash=crypto.createHash('sha256').update(token).digest('hex'),row=db.prepare('SELECT * FROM panel_tokens WHERE token_hash=?').get(hash);if(!row||row.used_at||row.expires_at<Date.now()||row.target_user_id!==req.user.id)return res.status(401).json({error:'Токен недействителен, истёк или выдан другому аккаунту'});const tx=db.transaction(()=>{const changed=db.prepare('UPDATE panel_tokens SET used_at=? WHERE token_hash=? AND used_at IS NULL').run(Date.now(),hash);if(changed.changes!==1)throw new Error('Токен уже использован');db.prepare('INSERT OR IGNORE INTO panel_admins(user_id,granted_at) VALUES(?,?)').run(req.user.id,Date.now())});try{tx();res.json({ok:true,message:'Токен принят. Доступ к админ-панели открыт.'})}catch{return res.status(409).json({error:'Токен уже использован'})}});
app.post('/api/admin/badges',auth,admin,(req,res)=>{const id=String(req.body?.userId||''),code=clean(req.body?.code,32),title=clean(req.body?.title,48),kind=req.body?.kind;if(!/^\d+$/.test(id)||!/^[a-z0-9_-]+$/.test(code)||!title||!['team','supporter'].includes(kind))return res.status(400).json({error:'Проверьте ID, код, название и тип значка'});db.prepare('INSERT INTO badges(user_id,code,kind,title,created_at) VALUES(?,?,?,?,?) ON CONFLICT(user_id,code) DO UPDATE SET kind=excluded.kind,title=excluded.title').run(id,code,kind,title,Date.now());res.json({ok:true,profile:profile(id)})});
app.delete('/api/admin/badges/:id/:code',auth,admin,(req,res)=>{if(!/^\d+$/.test(req.params.id))return res.status(400).json({error:'Некорректный ID'});db.prepare('DELETE FROM badges WHERE user_id=? AND code=?').run(req.params.id,clean(req.params.code,32));res.json({ok:true})});
app.put('/api/admin/moon-id',auth,admin,(req,res)=>{const id=String(req.body?.userId||''),moon=clean(req.body?.moonId,24);if(!/^\d+$/.test(id)||!/^[A-Za-z0-9_-]{3,24}$/.test(moon))return res.status(400).json({error:'Неверный Telegram ID или MoonGram ID'});try{db.prepare('INSERT INTO profiles(user_id,moon_id,created_at) VALUES(?,?,?) ON CONFLICT(user_id) DO UPDATE SET moon_id=excluded.moon_id').run(id,moon,Date.now());res.json({ok:true,profile:profile(id)})}catch{res.status(409).json({error:'MoonGram ID уже занят'})}});
app.listen(PORT,'0.0.0.0',()=>{console.log('MoonGram Mini App on '+PORT);if(!BOT_TOKEN)console.warn('BOT_TOKEN missing; Telegram auth is disabled');if(!ADMIN_IDS.size)console.warn('ADMIN_IDS empty; no admins configured')});
