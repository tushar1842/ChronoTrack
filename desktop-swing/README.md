# ChronoTrack Desktop Swing Dashboard ☕

A modern, high-performance **Java Swing Desktop Application** for **ChronoTrack** — allowing you to monitor, visualize, explore, and manage your browsing history in real time.

---

## ✨ Features

- **📊 Comprehensive Dashboard Overview**
  - Real-time metric cards: **Total Visits**, **Normal Browsing**, **Incognito Browsing**, and **Unique Websites**.
  - Interactive activity visualizations and recent activity feed with double-click to open URLs.
- **📋 Full History Explorer**
  - Instant live search by website title, URL, or domain.
  - Multi-criteria filtering: Mode filter (*All*, *Normal*, *Incognito*) and Domain dropdown.
  - Custom pill badges for browsing modes (Normal vs Incognito).
  - Right-click context menu (*Open in Browser*, *Copy URL*, *Delete Record*).
- **📈 Advanced Analytics & Custom Java2D Charts**
  - **Donut Chart**: Mode distribution (Normal vs. Incognito ratio).
  - **Horizontal Bar Chart**: Top 10 most visited domains with visit counters.
  - Domain ranking table with percentage shares and visit breakdown.
- **☕ Built-in Embedded Java Server**
  - Native pure Java HTTP server on port 5000 with CORS support.
  - Implements `GET /api/history`, `POST /api/history`, and `DELETE /api/history`.
  - Enables running the entire ChronoTrack suite (including the Chrome extension) **without needing Node.js or MongoDB**!
- **🌐 Dual-Mode Connectivity**
  - Connect seamlessly to the Node.js/Express backend at `http://127.0.0.1:5000`.
  - Or switch to the native Java backend with one click.
  - Automatic reconnection and local offline caching.
- **📥 Data Export & Management**
  - Export browsing history to **CSV** (for Excel/Spreadsheets) or **JSON**.
  - Manual visit logging dialog (`+ Log Visit`).
  - Sample data generator for instant testing.

---

## 🚀 Quick Start

### Prerequisites
- Java JDK 11 or higher (JDK 17 / 21 / 26 recommended).

### Running with Batch / PowerShell Scripts (No Maven Required)

#### Windows:
Double-click `run.bat` or run in terminal:
```cmd
cd desktop-swing
run.bat
```

Or via PowerShell:
```powershell
cd desktop-swing
.\run.ps1
```

#### Linux / macOS:
```bash
cd desktop-swing
chmod +x run.sh
./run.sh
```

---

### Running with Apache Maven (Optional)

If you use Maven or open the project in an IDE (IntelliJ IDEA, Eclipse, VS Code):
```bash
# Compile and run
mvn compile exec:java

# Or build executable JAR
mvn clean package
java -jar target/chronotrack-desktop-swing-1.0.0.jar
```

---

## ⚙️ Command-Line Options

You can launch the application with CLI flags:
```cmd
# Start desktop GUI and auto-activate the built-in Java server on port 5000
run.bat --server

# Start desktop GUI with pre-seeded sample data for testing
run.bat --seed

# Customize embedded server port
run.bat --server --port 5050
```

---

## 📁 Project Architecture

```
desktop-swing/
├── build.bat                                  # Compiles sources using javac
├── run.bat                                    # Runs main class with java
├── run.ps1                                    # PowerShell launcher
├── run.sh                                     # Bash launcher
├── pom.xml                                    # Maven project definition
└── src/
    └── main/
        └── java/
            └── com/
                └── chronotrack/
                    ├── Main.java              # Application entry point
                    ├── TestRunner.java        # Diagnostic test runner
                    ├── model/
                    │   ├── HistoryRecord.java # Model for browsing history entries
                    │   └── FilterCriteria.java# Search and filter criteria
                    ├── service/
                    │   ├── ChronoTrackApiClient.java # REST client (HttpClient)
                    │   ├── EmbeddedJavaServer.java   # Native Java HTTP Server (port 5000)
                    │   └── HistoryService.java       # Coordinates storage, stats & listeners
                    ├── ui/
                    │   ├── MainFrame.java     # Main window with tabs & header
                    │   ├── theme/
                    │   │   └── ModernTheme.java # Custom colors, fonts, styles
                    │   ├── components/
                    │   │   ├── MetricCard.java    # Stat KPI cards
                    │   │   ├── ModernTable.java   # Polished JTable
                    │   │   ├── BadgeRenderer.java # Pill badge renderer
                    │   │   └── ChartsPanel.java   # Java2D custom charts
                    │   ├── tabs/
                    │   │   ├── DashboardTab.java  # Dashboard overview
                    │   │   ├── HistoryTab.java    # Explorer & table
                    │   │   ├── AnalyticsTab.java  # Visual charts & breakdown
                    │   │   └── ServerTab.java     # Connection & server control
                    │   └── dialogs/
                    │       ├── AddRecordDialog.java # Manual log entry dialog
                    │       └── ExportDialog.java    # CSV / JSON export dialog
                    └── util/
                        ├── JsonHelper.java    # Pure Java JSON serializer & parser
                        └── UrlHelper.java     # URL validation, domain extraction
```
