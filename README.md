# ChronoTrack ⏱️

**ChronoTrack** is a cross-platform browsing activity tracker and analytics suite consisting of a browser extension, a REST API backend, and a modern Java Swing desktop dashboard.

---

## 🏗️ Project Architecture

```
ChronoTrack/
├── extension/        # Chrome Extension (Manifest V3) for tracking normal & incognito tabs
├── backend/          # Node.js Express REST API server + MongoDB
└── desktop-swing/    # Modern Java Swing Desktop Dashboard & Built-in Java REST Server
```

---

## 🌟 Components

### 1. ☕ Java Swing Desktop Dashboard (`desktop-swing/`)
A native desktop GUI built with **Java Swing** (`javax.swing` + `java.awt`):
- **Dashboard Overview**: KPI metric cards (Total Visits, Normal vs. Incognito ratio, Unique Domains).
- **History Explorer**: Searchable, filterable table with mode badges, context menu, and browser link launching.
- **Analytics & Visualizations**: Custom Java2D charts (Donut chart for mode distribution and Horizontal Bar chart for top domains).
- **Embedded Java Server**: Built-in HTTP server on port 5000 with CORS — allows running ChronoTrack completely without Node.js or MongoDB!
- **Data Export**: Export browsing activity to CSV or JSON.

**How to run:**
```cmd
cd desktop-swing
run.bat
```
*(Or use `.\run.ps1` for PowerShell, `./run.sh` for Linux/macOS, or `mvn compile exec:java`)*

---

### 2. 🧩 Chrome Extension (`extension/`)
- Tracks user web navigation events across normal and incognito browsing sessions.
- Transmits visit data (`url`, `title`, `browserMode`) to `http://127.0.0.1:5000/api/history`.
- Includes popup dashboard (`login.html`, `dashboard.html`, `history.html`, `analytics.html`).

**How to install in Chrome/Brave/Edge:**
1. Open `chrome://extensions/`
2. Enable **Developer mode** (top right).
3. Click **Load unpacked** and select the `d:\ChronoTrack\extension` folder.

---

### 3. 🌐 Node.js REST API Backend (`backend/`)
- Express.js API connecting to MongoDB to persist browsing history.
- Endpoints:
  - `GET /` — Health check
  - `GET /api/history` — Get all browsing records (sorted newest first)
  - `POST /api/history` — Log a new visit (`{ url, title, browserMode }`)
  - `DELETE /api/history/:id` — Delete a specific record
  - `DELETE /api/history` — Clear all records

**How to run:**
```cmd
cd backend
npm install
npm start
```

---

## ⚡ Quick Testing / Running Without MongoDB or Node.js

You can run the entire system using pure Java:
1. Start the Java desktop application with the embedded server:
   ```cmd
   cd desktop-swing
   run.bat --server --seed
   ```
2. Load the Chrome extension (`extension/`) in your browser.
3. Browse any website — visits will be automatically captured by the Chrome extension and displayed in real time inside the Java Swing Dashboard!
