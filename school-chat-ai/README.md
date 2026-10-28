# School Chat AI

Students, Classes aur Teachers ke modules + ek chat box. Chat mein sawal likho, jawab DB se table ke roop mein aata hai.

**Stack:** Java 17, Spring Boot 3.3 (Maven), MySQL, simple HTML UI, Gemini (Google AI Studio).

## Ye kaise kaam karta hai

```
UI (index.html)
   │  POST /api/chat  {"message": "Class 10-A mein kitne students hain?"}
   ▼
ChatService ──► Gemini: sirf sawal jata hai (DB ka data nahi)
   │            Gemini sirf JSON deta hai: {"tool":"count_students","className":"10","section":"A"}
   ▼
Allowlist + validation ──► ChatTools ──► MySQL (parameterized query)
   ▼
{answer, columns, rows}  ──►  UI chat bubble + table
```

- AI sirf **sawal samajhta hai** (kaunsa tool, kaunse parameters). Ginti aur data DB se aate hain, AI numbers nahi banata.
- Gemini ko SQL likhne nahi diya gaya. Sirf 5 fixed tools hain, aur unke parameters validate hote hain.
- Gemini key sirf backend mein rehti hai, browser mein kabhi nahi jati.

**Tools:** `count_students`, `list_students`, `list_teachers`, `list_classes`, `class_teacher`
Naya tool jodna ho: `ChatTools` mein method, `ChatService` ke `switch` mein case, aur system prompt mein ek line.

## Run karne ke steps

### 1. Gemini key
aistudio.google.com par login, **Get API key**, **Create API key**. Phir environment variable set karo:

PowerShell:
```powershell
$env:GEMINI_API_KEY="AIza...aapki_key"
```
IntelliJ: Run, Edit Configurations, Environment variables mein `GEMINI_API_KEY=AIza...`

### 2. MySQL
MySQL chalu hona chahiye. Database `school_chat` apne aap ban jata hai. Default login `root` / `root`, alag ho to set karo:
```powershell
$env:DB_USERNAME="root"; $env:DB_PASSWORD="aapka_password"
# optional: $env:DB_HOST, $env:DB_PORT, $env:DB_NAME
```
Pehli baar khaali DB mein demo data (6 teachers, 5 classes, 40 students) apne aap aa jata hai.

### 3. App chalao
**IntelliJ mein:** zip extract karo, **File, Open** se `pom.xml` (ya poora folder) kholo, Maven dependencies download hone do, phir `SchoolChatApplication` ko Run karo (Environment variables upar wale Run Configuration mein daalna).

**Terminal se** (Maven install hona chahiye):
```powershell
cd school-chat-ai
mvn spring-boot:run
```
Browser mein kholo: **http://localhost:8090** (port badalna ho to `$env:SERVER_PORT="8095"`).

MySQL ke bina turant dekhna ho (in-memory H2, restart par data jata hai):
```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=h2"
```
(IntelliJ mein: Run Configuration ke **Active profiles** mein `h2` likho.)

## APIs

| Method | URL | Kaam |
|---|---|---|
| POST | `/api/chat` | Chat. Body: `{"message":"..."}` |
| GET/POST | `/api/teachers` | list / naya teacher |
| GET/PUT/DELETE | `/api/teachers/{id}` | |
| GET/POST | `/api/classes` | list / nayi class. Body: `{"name":"10","section":"A","classTeacherId":1}` |
| GET/PUT/DELETE | `/api/classes/{id}` | |
| GET/POST | `/api/students` | list (`?className=10&section=A&name=aar`) / naya student |
| GET/PUT/DELETE | `/api/students/{id}` | |

Student body: `{"name":"Riya Gupta","rollNo":3,"gender":"Female","guardianName":"Mr. Gupta","phone":"98...","status":"ACTIVE","classId":3}`

Chat response:
```json
{ "answer": "Class 10-A mein 8 active students hain.",
  "tool": "count_students",
  "columns": ["Class","Section","Students"],
  "rows": [ {"Class":"10","Section":"A","Students":8} ] }
```
`tool` = `none` (data wala sawal nahi) ya `error` (Gemini limit/key/network ki dikkat) bhi ho sakta hai.

## Tests
```powershell
mvn test
```
Tests Gemini ko mock karte hain aur in-memory H2 use karte hain, to MySQL ya internet ki zarurat nahi.

## Dhyan rakhne wali baatein

- **Free tier ka data:** AI Studio ke free tier mein Google prompts ko apne products sudharne mein use kar sakta hai. Is app mein sirf user ka likha sawal Gemini ko jata hai (DB ka data nahi), phir bhi chat mein kisi asli student ka sensitive detail mat likho. Asli school data ke liye paid tier lo.
- **Free limit:** free tier mein requests per minute/day kam hote hain. Limit khatam hone par chat mein friendly message aata hai.
- **Model naam:** `gemini.model` (default `gemini-2.5-flash-lite`). Google purane models band karta rehta hai, to AI Studio mein naam check karte raho.
- **Login nahi hai:** ye basic project hai, APIs open hain. Asli use se pehle Spring Security (JWT) lagao.
- `app.seed-data=true` sirf khaali DB mein demo data daalta hai. Asli data ke saath `false` kar do.
- AI model ko "train" nahi karna padta. Is design mein model aapke data par train nahi hota, wo bas sawal ko tool mein badalta hai.
