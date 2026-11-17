import sys
import os
from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.enum.shapes import MSO_SHAPE

# --- Presentation Setup ---
prs = Presentation()
prs.slide_width = Inches(13.333)
prs.slide_height = Inches(7.5)
blank_layout = prs.slide_layouts[6] # completely blank layout

# --- Color Palette ---
COLOR_BG_DARK = RGBColor(15, 23, 42)      # Slate 900
COLOR_BG_LIGHT = RGBColor(248, 250, 252)  # Slate 50
COLOR_CARD_BG = RGBColor(255, 255, 255)   # Pure White
COLOR_CARD_DARK = RGBColor(30, 41, 59)    # Slate 800
COLOR_BORDER = RGBColor(226, 232, 240)    # Slate 200
COLOR_BORDER_DARK = RGBColor(51, 65, 85)  # Slate 700

COLOR_PRIMARY = RGBColor(79, 70, 229)     # Indigo 600
COLOR_CYAN = RGBColor(6, 182, 212)        # Cyan 500
COLOR_EMERALD = RGBColor(16, 185, 129)    # Emerald 500
COLOR_PURPLE = RGBColor(124, 58, 237)     # Purple 600
COLOR_AMBER = RGBColor(245, 158, 11)      # Amber 500

COLOR_TEXT_MAIN = RGBColor(15, 23, 42)
COLOR_TEXT_MUTED = RGBColor(100, 116, 139)
COLOR_TEXT_WHITE = RGBColor(255, 255, 255)
COLOR_TEXT_LIGHT_MUTED = RGBColor(148, 163, 184)

FONT_HEADING = "Segoe UI"
FONT_BODY = "Segoe UI"

def set_slide_background(slide, color):
    bg_shape = slide.shapes.add_shape(
        MSO_SHAPE.RECTANGLE, Inches(0), Inches(0), prs.slide_width, prs.slide_height
    )
    bg_shape.fill.solid()
    bg_shape.fill.fore_color.rgb = color
    bg_shape.line.fill.background() # no border
    return bg_shape

def add_header(slide, title_text, category_text="CHRONOTRACK PROJECT", dark_mode=False):
    # Category tag
    tb_cat = slide.shapes.add_textbox(Inches(0.8), Inches(0.5), Inches(11.7), Inches(0.4))
    p_cat = tb_cat.text_frame.paragraphs[0]
    p_cat.text = category_text.upper()
    p_cat.font.name = FONT_HEADING
    p_cat.font.size = Pt(11)
    p_cat.font.bold = True
    p_cat.font.color.rgb = COLOR_CYAN if dark_mode else COLOR_PRIMARY
    
    # Title
    tb_title = slide.shapes.add_textbox(Inches(0.8), Inches(0.85), Inches(11.7), Inches(0.7))
    p_title = tb_title.text_frame.paragraphs[0]
    p_title.text = title_text
    p_title.font.name = FONT_HEADING
    p_title.font.size = Pt(24)
    p_title.font.bold = True
    p_title.font.color.rgb = COLOR_TEXT_WHITE if dark_mode else COLOR_TEXT_MAIN

def create_card(slide, left, top, width, height, bg_color=COLOR_CARD_BG, border_color=COLOR_BORDER):
    shape = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, height)
    shape.fill.solid()
    shape.fill.fore_color.rgb = bg_color
    if border_color:
        shape.line.color.rgb = border_color
        shape.line.width = Pt(1.2)
    else:
        shape.line.fill.background()
    return shape

# ==============================================================================
# SLIDE 1: Title Slide (Dark Theme)
# ==============================================================================
s1 = prs.slides.add_slide(blank_layout)
set_slide_background(s1, COLOR_BG_DARK)

# Accent gradient/pill at top
top_pill = s1.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(0.8), Inches(1.2), Inches(3.6), Inches(0.45))
top_pill.fill.solid()
top_pill.fill.fore_color.rgb = COLOR_CARD_DARK
top_pill.line.color.rgb = COLOR_PRIMARY
top_pill.line.width = Pt(1)
p_pill = top_pill.text_frame.paragraphs[0]
p_pill.text = "⚡ ACTIVITY MONITORING SUITE"
p_pill.font.name = FONT_HEADING
p_pill.font.size = Pt(11)
p_pill.font.bold = True
p_pill.font.color.rgb = COLOR_CYAN
p_pill.alignment = PP_ALIGN.CENTER

# Main Title
tb1 = s1.shapes.add_textbox(Inches(0.8), Inches(1.85), Inches(11.5), Inches(1.8))
tf1 = tb1.text_frame
tf1.word_wrap = True
p1 = tf1.paragraphs[0]
p1.text = "ChronoTrack"
p1.font.name = FONT_HEADING
p1.font.size = Pt(56)
p1.font.bold = True
p1.font.color.rgb = COLOR_TEXT_WHITE

p1_sub = tf1.add_paragraph()
p1_sub.text = "Smart Cross-Platform Browser Activity Tracker & Analytics Suite"
p1_sub.font.name = FONT_BODY
p1_sub.font.size = Pt(22)
p1_sub.font.color.rgb = COLOR_TEXT_LIGHT_MUTED
p1_sub.space_before = Pt(8)

# 4 Key Pillars Cards at bottom
pillars = [
    ("🌐 Chrome Extension", "Manifest V3 background worker capturing normal & incognito tabs in real-time.", COLOR_CYAN),
    ("🚀 Node.js & MongoDB", "Express REST API backend with secure persistence and aggregation.", COLOR_EMERALD),
    ("☕ Java Swing Client", "Native desktop GUI with analytics, live search, and custom Java2D charts.", COLOR_PRIMARY),
    ("⚙️ Embedded Java Server", "Zero-dependency native Java HTTP server on port 5000 with CORS.", COLOR_PURPLE)
]

for idx, (title, desc, accent) in enumerate(pillars):
    c_left = Inches(0.8 + idx * 2.95)
    c_top = Inches(4.3)
    card = create_card(s1, c_left, c_top, Inches(2.8), Inches(2.2), COLOR_CARD_DARK, COLOR_BORDER_DARK)
    
    tb = s1.shapes.add_textbox(c_left + Inches(0.15), c_top + Inches(0.15), Inches(2.5), Inches(1.9))
    tf = tb.text_frame
    tf.word_wrap = True
    
    p = tf.paragraphs[0]
    p.text = title
    p.font.name = FONT_HEADING
    p.font.size = Pt(15)
    p.font.bold = True
    p.font.color.rgb = accent
    
    p_body = tf.add_paragraph()
    p_body.text = desc
    p_body.font.name = FONT_BODY
    p_body.font.size = Pt(12)
    p_body.font.color.rgb = COLOR_TEXT_LIGHT_MUTED
    p_body.space_before = Pt(8)

s1.notes_slide.notes_text_frame.text = (
    "Welcome everyone. Today we are presenting ChronoTrack: a comprehensive, cross-platform browsing activity tracker "
    "and productivity suite. ChronoTrack unifies browser tracking via a Manifest V3 Chrome Extension, scalable cloud "
    "persistence via Node.js/MongoDB, and now a complete native Java Swing desktop application featuring custom Java2D "
    "visualizations and an embedded Java REST server."
)

# ==============================================================================
# SLIDE 2: Executive Summary & Motivation
# ==============================================================================
s2 = prs.slides.add_slide(blank_layout)
set_slide_background(s2, COLOR_BG_LIGHT)
add_header(s2, "Executive Summary & Motivation", "OVERVIEW")

# 3 Column Cards
cards_s2 = [
    ("The Challenge", [
        "Modern users spend 6+ hours daily on browsers without tracking actual productivity.",
        "Incognito and private windows create blind spots in traditional browser history.",
        "Most tracking tools are intrusive, cloud-only, or lock users into closed platforms.",
        "Lack of unified local desktop analysis and immediate offline feedback."
    ], COLOR_AMBER, "⚠️"),
    ("The ChronoTrack Solution", [
        "Lightweight Chrome Extension tracks both standard and incognito browsing sessions transparently.",
        "REST API layer standardizes event payloads across web and desktop endpoints.",
        "Rich Java Swing desktop dashboard provides zero-latency offline management and visualization.",
        "Complete user sovereignty with local CSV and JSON export capabilities."
    ], COLOR_PRIMARY, "💡"),
    ("Key Business & Technical Value", [
        "Dual-Backend Freedom: Run seamlessly with Node.js/MongoDB OR 100% pure Java.",
        "Zero External Jar Dependencies: Java Swing client runs out of the box with standard JDK.",
        "Multi-Platform: Windows, macOS, and Linux compatibility across all subsystems.",
        "Extensible Architecture: Open REST API allows easy extension to other browsers and mobile."
    ], COLOR_EMERALD, "🚀")
]

for idx, (title, points, color, icon) in enumerate(cards_s2):
    left = Inches(0.8 + idx * 3.95)
    create_card(s2, left, Inches(1.8), Inches(3.8), Inches(5.0))
    
    tb = s2.shapes.add_textbox(left + Inches(0.25), Inches(2.0), Inches(3.3), Inches(4.6))
    tf = tb.text_frame
    tf.word_wrap = True
    
    p_title = tf.paragraphs[0]
    p_title.text = f"{icon}  {title}"
    p_title.font.name = FONT_HEADING
    p_title.font.size = Pt(17)
    p_title.font.bold = True
    p_title.font.color.rgb = color
    
    for pt in points:
        p_pt = tf.add_paragraph()
        p_pt.text = "• " + pt
        p_pt.font.name = FONT_BODY
        p_pt.font.size = Pt(12)
        p_pt.font.color.rgb = COLOR_TEXT_MAIN
        p_pt.space_before = Pt(10)

s2.notes_slide.notes_text_frame.text = (
    "This slide outlines the core problem statement, our solution, and technical value. Traditional history tools "
    "are fragmented and fail to address private browsing analysis. ChronoTrack solves this by providing a unified, "
    "privacy-first tracking and analytics engine accessible both through browser popups and a dedicated Java desktop app."
)

# ==============================================================================
# SLIDE 3: System Architecture & Data Flow
# ==============================================================================
s3 = prs.slides.add_slide(blank_layout)
set_slide_background(s3, COLOR_BG_LIGHT)
add_header(s3, "System Architecture & 3-Tier Design", "ARCHITECTURE")

# Top flow banner
banner = create_card(s3, Inches(0.8), Inches(1.8), Inches(11.7), Inches(1.1), COLOR_PRIMARY, None)
tb_b = s3.shapes.add_textbox(Inches(1.0), Inches(1.9), Inches(11.3), Inches(0.9))
tf_b = tb_b.text_frame
tf_b.word_wrap = True
pb1 = tf_b.paragraphs[0]
pb1.text = "End-to-End Data Pipeline: Browser Event ➔ REST API ➔ Database & Desktop GUI"
pb1.font.name = FONT_HEADING
pb1.font.size = Pt(16)
pb1.font.bold = True
pb1.font.color.rgb = COLOR_TEXT_WHITE

pb2 = tf_b.add_paragraph()
pb2.text = "Chrome webNavigation listener emits JSON payload ➔ Handled by Express or Java HttpServer on :5000 ➔ Persisted and visualized."
pb2.font.name = FONT_BODY
pb2.font.size = Pt(12)
pb2.font.color.rgb = COLOR_TEXT_LIGHT_MUTED
pb2.space_before = Pt(4)

# 3 Architecture Tier Cards
tiers = [
    ("Tier 1: Client Layer", [
        ("Chrome Extension (MV3)", "Background service worker listening to tab completions; injects OAuth and captures mode."),
        ("Desktop Client (Swing)", "High-performance native GUI with multi-tab dashboard, real-time polling, and chart rendering."),
        ("Web Extension Popup", "HTML5/JS extension popup for instant quick stats and settings toggles.")
    ], COLOR_CYAN),
    ("Tier 2: Service / API Layer", [
        ("Node.js / Express Server", "Modular server on port 5000 managing Mongoose models, validation, and REST routes."),
        ("Embedded Java Server", "Native com.sun.net.httpserver on port 5000 implementing full ChronoTrack REST spec."),
        ("CORS & Security Bridge", "Cross-origin headers allowing seamless browser and desktop communication.")
    ], COLOR_PRIMARY),
    ("Tier 3: Persistence Layer", [
        ("MongoDB Database", "Document database storing indexed history entries with timestamps and domain keys."),
        ("Local File Storage", "chronotrack_storage.json for zero-dependency standalone Java persistence."),
        ("Export Engine", "Standardized CSV & JSON export formats for reporting and data portability.")
    ], COLOR_PURPLE)
]

for idx, (title, items, color) in enumerate(tiers):
    left = Inches(0.8 + idx * 3.95)
    create_card(s3, left, Inches(3.1), Inches(3.8), Inches(3.8))
    
    tb = s3.shapes.add_textbox(left + Inches(0.2), Inches(3.25), Inches(3.4), Inches(3.5))
    tf = tb.text_frame
    tf.word_wrap = True
    
    p = tf.paragraphs[0]
    p.text = title
    p.font.name = FONT_HEADING
    p.font.size = Pt(16)
    p.font.bold = True
    p.font.color.rgb = color
    
    for subtitle, text in items:
        p_sub = tf.add_paragraph()
        p_sub.text = "▪ " + subtitle
        p_sub.font.name = FONT_HEADING
        p_sub.font.size = Pt(12)
        p_sub.font.bold = True
        p_sub.font.color.rgb = COLOR_TEXT_MAIN
        p_sub.space_before = Pt(10)
        
        p_desc = tf.add_paragraph()
        p_desc.text = text
        p_desc.font.name = FONT_BODY
        p_desc.font.size = Pt(11)
        p_desc.font.color.rgb = COLOR_TEXT_MUTED
        p_desc.space_before = Pt(2)

s3.notes_slide.notes_text_frame.text = (
    "ChronoTrack follows a clean 3-tier architecture. Notice the dual-backend capability in Tier 2: a developer or "
    "user can run ChronoTrack using Node.js and MongoDB, or run entirely in Java without installing any databases or "
    "runtime dependencies. Both backends adhere to the exact same REST API schema."
)

# ==============================================================================
# SLIDE 4: Chrome Extension (Frontend Tracker)
# ==============================================================================
s4 = prs.slides.add_slide(blank_layout)
set_slide_background(s4, COLOR_BG_LIGHT)
add_header(s4, "Chrome Extension: Intelligent Browser Tracking", "FRONTEND")

# Left: Features Card
create_card(s4, Inches(0.8), Inches(1.8), Inches(6.8), Inches(5.0))
tb_ext = s4.shapes.add_textbox(Inches(1.1), Inches(2.0), Inches(6.2), Inches(4.6))
tf_ext = tb_ext.text_frame
tf_ext.word_wrap = True

pe_title = tf_ext.paragraphs[0]
pe_title.text = "Manifest V3 Service Worker Architecture"
pe_title.font.name = FONT_HEADING
pe_title.font.size = Pt(18)
pe_title.font.bold = True
pe_title.font.color.rgb = COLOR_PRIMARY

features_ext = [
    ("Main Frame Interception", "Listens to chrome.webNavigation.onCommitted for frameId: 0, preventing duplicate tracking from embedded iframes and third-party trackers."),
    ("Dual Mode Awareness", "Inspects tab.incognito state to tag events as 'normal' or 'incognito'. Incognito split mode guarantees privacy compliance."),
    ("Configurable Privacy Settings", "Provides user toggles for master tracking, incognito recording, auto-refresh, and desktop notifications via chrome.storage.local."),
    ("Non-Blocking Async Dispatch", "Transmits browsing records asynchronously via fetch() to http://127.0.0.1:5000/api/history without impacting page load speed.")
]

for title, desc in features_ext:
    p_t = tf_ext.add_paragraph()
    p_t.text = "✔ " + title
    p_t.font.name = FONT_HEADING
    p_t.font.size = Pt(13)
    p_t.font.bold = True
    p_t.font.color.rgb = COLOR_TEXT_MAIN
    p_t.space_before = Pt(10)
    
    p_d = tf_ext.add_paragraph()
    p_d.text = desc
    p_d.font.name = FONT_BODY
    p_d.font.size = Pt(11)
    p_d.font.color.rgb = COLOR_TEXT_MUTED
    p_d.space_before = Pt(2)

# Right: Payload Spec & Code Mockup Card
create_card(s4, Inches(7.9), Inches(1.8), Inches(4.6), Inches(5.0), COLOR_BG_DARK, COLOR_BORDER_DARK)
tb_code = s4.shapes.add_textbox(Inches(8.15), Inches(2.0), Inches(4.1), Inches(4.6))
tf_code = tb_code.text_frame
tf_code.word_wrap = True

pc_title = tf_code.paragraphs[0]
pc_title.text = "Event Payload Specification"
pc_title.font.name = FONT_HEADING
pc_title.font.size = Pt(16)
pc_title.font.bold = True
pc_title.font.color.rgb = COLOR_CYAN

code_lines = [
    "POST /api/history HTTP/1.1",
    "Host: 127.0.0.1:5000",
    "Content-Type: application/json",
    "",
    "{",
    '  "url": "https://github.com",',
    '  "title": "GitHub · Platform",',
    '  "browserMode": "incognito"',
    "}",
    "",
    "// Response: 201 Created",
    "{",
    '  "message": "History saved",',
    '  "data": {',
    '    "_id": "6735e5d3...",',
    '    "domain": "github.com",',
    '    "visitTime": "2026-11-17T..."',
    "  }",
    "}"
]

for line in code_lines:
    p_c = tf_code.add_paragraph()
    p_c.text = line
    p_c.font.name = "Consolas"
    p_c.font.size = Pt(10.5)
    if line.startswith("POST") or line.startswith("//"):
        p_c.font.color.rgb = COLOR_AMBER
    elif '"browserMode"' in line:
        p_c.font.color.rgb = COLOR_PURPLE
    elif '"message"' in line:
        p_c.font.color.rgb = COLOR_EMERALD
    else:
        p_c.font.color.rgb = COLOR_TEXT_LIGHT_MUTED
    p_c.space_before = Pt(1)

s4.notes_slide.notes_text_frame.text = (
    "The Chrome extension operates in the background with zero perceptible CPU or memory footprint. "
    "It uses Manifest V3 standards, capturing user navigation only on main frames. Notice the clean JSON "
    "payload structure: url, title, and browserMode, which are parsed and indexed by the backend."
)

# ==============================================================================
# SLIDE 5: Java & Java Swing Desktop Application
# ==============================================================================
s5 = prs.slides.add_slide(blank_layout)
set_slide_background(s5, COLOR_BG_LIGHT)
add_header(s5, "Java & Java Swing: Native Desktop Client", "DESKTOP CLIENT")

# 4 Grid Feature Cards
swing_features = [
    ("📊 Overview Dashboard", "Real-time KPI metric counters for Total Visits, Normal Mode, Incognito Mode, and Unique Domains with automated 5s polling.", COLOR_PRIMARY, "01"),
    ("🔍 History Explorer", "Searchable, sortable JTable with live keyword filtering across URLs, titles, and domains, plus browsing mode pill badge renderers.", COLOR_CYAN, "02"),
    ("📈 Native Visual Analytics", "Custom Java2D vector graphics: Donut Chart for privacy mode distribution and Horizontal Bar Chart for top 10 visited domains.", COLOR_PURPLE, "03"),
    ("⚙️ Server & Connectivity Hub", "Embedded control panel to test remote API connections, toggle the built-in Java server, or generate sample test data.", COLOR_EMERALD, "04")
]

for idx, (title, desc, color, num) in enumerate(swing_features):
    row = idx // 2
    col = idx % 2
    left = Inches(0.8 + col * 5.95)
    top = Inches(1.8 + row * 2.55)
    
    create_card(s5, left, top, Inches(5.75), Inches(2.35))
    
    # Number badge
    nb = s5.shapes.add_shape(MSO_SHAPE.OVAL, left + Inches(0.25), top + Inches(0.25), Inches(0.55), Inches(0.55))
    nb.fill.solid()
    nb.fill.fore_color.rgb = color
    nb.line.fill.background()
    p_nb = nb.text_frame.paragraphs[0]
    p_nb.text = num
    p_nb.font.name = FONT_HEADING
    p_nb.font.size = Pt(12)
    p_nb.font.bold = True
    p_nb.font.color.rgb = COLOR_TEXT_WHITE
    p_nb.alignment = PP_ALIGN.CENTER
    
    tb = s5.shapes.add_textbox(left + Inches(0.95), top + Inches(0.15), Inches(4.6), Inches(2.0))
    tf = tb.text_frame
    tf.word_wrap = True
    
    p_t = tf.paragraphs[0]
    p_t.text = title
    p_t.font.name = FONT_HEADING
    p_t.font.size = Pt(16)
    p_t.font.bold = True
    p_t.font.color.rgb = color
    
    p_d = tf.add_paragraph()
    p_d.text = desc
    p_d.font.name = FONT_BODY
    p_d.font.size = Pt(12)
    p_d.font.color.rgb = COLOR_TEXT_MUTED
    p_d.space_before = Pt(6)

s5.notes_slide.notes_text_frame.text = (
    "Here we introduce the brand new Java Swing application added in desktop-swing. It provides a polished, "
    "responsive UI that feels right at home on modern desktop OSes. It includes four core views: Dashboard, History "
    "Explorer, Analytics, and Server Controls."
)

# ==============================================================================
# SLIDE 6: UI & Design System in Swing
# ==============================================================================
s6 = prs.slides.add_slide(blank_layout)
set_slide_background(s6, COLOR_BG_LIGHT)
add_header(s6, "Desktop UI & Modern Swing Design System", "SWING UI")

# Left Column: Design principles
create_card(s6, Inches(0.8), Inches(1.8), Inches(5.6), Inches(5.0))
tb_ui = s6.shapes.add_textbox(Inches(1.05), Inches(2.0), Inches(5.1), Inches(4.6))
tf_ui = tb_ui.text_frame
tf_ui.word_wrap = True

pui_head = tf_ui.paragraphs[0]
pui_head.text = "Modern Look & Feel Engineering"
pui_head.font.name = FONT_HEADING
pui_head.font.size = Pt(18)
pui_head.font.bold = True
pui_head.font.color.rgb = COLOR_PRIMARY

ui_points = [
    ("Native System Integration", "Utilizes UIManager.getSystemLookAndFeelClassName() with antialiased font rendering (KEY_TEXT_ANTIALIASING)."),
    ("Polished Component Styling", "ModernTheme.java defines unified Slate color palettes, rounded borders, and smooth button hover states."),
    ("Custom TableCellRenderer", "BadgeRenderer creates color-coded pill badges: emerald for 'Normal' and purple for 'Incognito'."),
    ("Zebra Striped ModernTable", "Alternating row backgrounds, 38px touch-friendly row heights, and custom header renderers."),
    ("Context-Sensitive Actions", "Right-click context menu to open URLs in system browser, copy URLs to clipboard, or delete entries.")
]

for title, desc in ui_points:
    pt = tf_ui.add_paragraph()
    pt.text = "✦ " + title
    pt.font.name = FONT_HEADING
    pt.font.size = Pt(12.5)
    pt.font.bold = True
    pt.font.color.rgb = COLOR_TEXT_MAIN
    pt.space_before = Pt(8)
    
    pd = tf_ui.add_paragraph()
    pd.text = desc
    pd.font.name = FONT_BODY
    pd.font.size = Pt(11)
    pd.font.color.rgb = COLOR_TEXT_MUTED
    pd.space_before = Pt(2)

# Right Column: Visual UI Mockup Card
create_card(s6, Inches(6.7), Inches(1.8), Inches(5.8), Inches(5.0), COLOR_CARD_BG, COLOR_BORDER)
tb_mock = s6.shapes.add_textbox(Inches(6.95), Inches(2.0), Inches(5.3), Inches(4.6))
tf_mock = tb_mock.text_frame
tf_mock.word_wrap = True

pm_title = tf_mock.paragraphs[0]
pm_title.text = "Interactive UI Elements Breakdown"
pm_title.font.name = FONT_HEADING
pm_title.font.size = Pt(18)
pm_title.font.bold = True
pm_title.font.color.rgb = COLOR_CYAN

mock_items = [
    ("Header Status Pill", "Live connection indicator showing [● Connected to Backend], [● Java Server Active], or [○ Backend Offline].", COLOR_EMERALD),
    ("Metric Cards Grid", "Prominent stat widgets displaying large 26pt bold counters and dynamic percentage shares.", COLOR_PRIMARY),
    ("Real-Time Filter Bar", "Instant search input with DocumentListener triggering filter updates on each keystroke.", COLOR_CYAN),
    ("Action Dialogs", "Modal dialogs for logging manual visits with URL validation and choosing export formats.", COLOR_PURPLE)
]

for title, desc, acc in mock_items:
    pt = tf_mock.add_paragraph()
    pt.text = "▪ " + title
    pt.font.name = FONT_HEADING
    pt.font.size = Pt(13)
    pt.font.bold = True
    pt.font.color.rgb = acc
    pt.space_before = Pt(10)
    
    pd = tf_mock.add_paragraph()
    pd.text = desc
    pd.font.name = FONT_BODY
    pd.font.size = Pt(11.5)
    pd.font.color.rgb = COLOR_TEXT_MUTED
    pd.space_before = Pt(2)

s6.notes_slide.notes_text_frame.text = (
    "Many legacy Swing applications look outdated, but ChronoTrack uses modern design patterns: Slate color palettes, "
    "pill badges, rounded card borders, and smooth interaction states. This demonstrates how standard Java Swing can look "
    "as sleek and clean as contemporary web applications."
)

# ==============================================================================
# SLIDE 7: Custom Visualizations & Analytics
# ==============================================================================
s7 = prs.slides.add_slide(blank_layout)
set_slide_background(s7, COLOR_BG_LIGHT)
add_header(s7, "Native Java2D Charts & Analytics Engine", "ANALYTICS")

# Left: Donut Chart Feature
c1 = create_card(s7, Inches(0.8), Inches(1.8), Inches(5.75), Inches(5.0))
tb_c1 = s7.shapes.add_textbox(Inches(1.05), Inches(2.05), Inches(5.25), Inches(4.5))
tf_c1 = tb_c1.text_frame
tf_c1.word_wrap = True

p1 = tf_c1.paragraphs[0]
p1.text = "🍩 Mode Distribution Donut Chart"
p1.font.name = FONT_HEADING
p1.font.size = Pt(18)
p1.font.bold = True
p1.font.color.rgb = COLOR_PURPLE

c1_points = [
    ("Mathematical Arc Computation", "Computes precise degree angles: (normalCount / total) * 360 with antialiased Arc2D arcs."),
    ("Visual Privacy Breakdown", "Emerald green segment displays Normal visits; vibrant purple segment displays Incognito visits."),
    ("Central Summary Metric", "Renders total visits count right in the center ring with font metrics alignment."),
    ("Responsive Auto-Scaling", "Dynamically resizes with window geometry while maintaining circular proportions.")
]
for title, desc in c1_points:
    pt = tf_c1.add_paragraph()
    pt.text = "• " + title
    pt.font.name = FONT_HEADING
    pt.font.size = Pt(12.5)
    pt.font.bold = True
    pt.font.color.rgb = COLOR_TEXT_MAIN
    pt.space_before = Pt(8)
    
    pd = tf_c1.add_paragraph()
    pd.text = desc
    pd.font.name = FONT_BODY
    pd.font.size = Pt(11)
    pd.font.color.rgb = COLOR_TEXT_MUTED
    pd.space_before = Pt(2)

# Right: Bar Chart Feature
c2 = create_card(s7, Inches(6.8), Inches(1.8), Inches(5.75), Inches(5.0))
tb_c2 = s7.shapes.add_textbox(Inches(7.05), Inches(2.05), Inches(5.25), Inches(4.5))
tf_c2 = tb_c2.text_frame
tf_c2.word_wrap = True

p2 = tf_c2.paragraphs[0]
p2.text = "📊 Top Domains Horizontal Bar Chart"
p2.font.name = FONT_HEADING
p2.font.size = Pt(18)
p2.font.bold = True
p2.font.color.rgb = COLOR_PRIMARY

c2_points = [
    ("Domain Aggregation", "Extracts root domain keys and sorts by frequency descending (e.g. github.com, stackoverflow.com)."),
    ("Proportional Fill Tracks", "Draws soft gray background tracks and fills indigo rounded bars proportional to the top visit count."),
    ("Truncation & Clean Typography", "Ellipsizes long domains to maintain crisp vertical alignment across any dataset size."),
    ("Zero Third-Party Charting Libraries", "Rendered 100% natively via Graphics2D without JFreeChart or heavy dependencies.")
]
for title, desc in c2_points:
    pt = tf_c2.add_paragraph()
    pt.text = "• " + title
    pt.font.name = FONT_HEADING
    pt.font.size = Pt(12.5)
    pt.font.bold = True
    pt.font.color.rgb = COLOR_TEXT_MAIN
    pt.space_before = Pt(8)
    
    pd = tf_c2.add_paragraph()
    pd.text = desc
    pd.font.name = FONT_BODY
    pd.font.size = Pt(11)
    pd.font.color.rgb = COLOR_TEXT_MUTED
    pd.space_before = Pt(2)

s7.notes_slide.notes_text_frame.text = (
    "A key technical achievement here is the custom Graphics2D chart engine in ChartsPanel.java. Instead of bloating "
    "the project with third-party charting libraries, we engineered vector-based Donut and Horizontal Bar charts from "
    "scratch using Java2D. This makes the project completely self-contained and ultra-fast."
)

# ==============================================================================
# SLIDE 8: Built-in Embedded Java Server
# ==============================================================================
s8 = prs.slides.add_slide(blank_layout)
set_slide_background(s8, COLOR_BG_LIGHT)
add_header(s8, "Embedded Java Server: Pure Java Stack", "JAVA BACKEND")

# Left: Highlights
create_card(s8, Inches(0.8), Inches(1.8), Inches(6.5), Inches(5.0))
tb_srv = s8.shapes.add_textbox(Inches(1.05), Inches(2.0), Inches(6.0), Inches(4.6))
tf_srv = tb_srv.text_frame
tf_srv.word_wrap = True

ps_title = tf_srv.paragraphs[0]
ps_title.text = "Lightweight Native HTTP Server on Port 5000"
ps_title.font.name = FONT_HEADING
ps_title.font.size = Pt(18)
ps_title.font.bold = True
ps_title.font.color.rgb = COLOR_EMERALD

srv_items = [
    ("com.sun.net.httpserver.HttpServer", "Built directly on the standard JDK HTTP server module. No Tomcat, Jetty, or Spring Boot required."),
    ("Complete REST API Parity", "Handles GET /api/history, POST /api/history, DELETE /api/history/:id, and OPTIONS preflight identically to Node.js."),
    ("CORS Compliance", "Configured with Access-Control-Allow-Origin: * to accept background browser activity from Chrome Extension directly."),
    ("Real-Time Event Pub/Sub", "Emits Consumer<HistoryRecord> notifications when new visits arrive, updating the Swing UI instantaneously."),
    ("Local Persistence", "Automatically loads and flushes history to chronotrack_storage.json on disk across restarts.")
]

for title, desc in srv_items:
    pt = tf_srv.add_paragraph()
    pt.text = "⚡ " + title
    pt.font.name = FONT_HEADING
    pt.font.size = Pt(12.5)
    pt.font.bold = True
    pt.font.color.rgb = COLOR_TEXT_MAIN
    pt.space_before = Pt(8)
    
    pd = tf_srv.add_paragraph()
    pd.text = desc
    pd.font.name = FONT_BODY
    pd.font.size = Pt(11)
    pd.font.color.rgb = COLOR_TEXT_MUTED
    pd.space_before = Pt(2)

# Right: Dual Mode Comparison Card
create_card(s8, Inches(7.6), Inches(1.8), Inches(4.9), Inches(5.0), COLOR_BG_DARK, COLOR_BORDER_DARK)
tb_comp = s8.shapes.add_textbox(Inches(7.85), Inches(2.0), Inches(4.4), Inches(4.6))
tf_comp = tb_comp.text_frame
tf_comp.word_wrap = True

pcomp_t = tf_comp.paragraphs[0]
pcomp_t.text = "Dual-Backend Flexibility"
pcomp_t.font.name = FONT_HEADING
pcomp_t.font.size = Pt(17)
pcomp_t.font.bold = True
pcomp_t.font.color.rgb = COLOR_CYAN

comp_rows = [
    ("Mode A: Full-Stack Mode", "Runs Node.js + Express + MongoDB.\nBest for scalable multi-user deployments and cloud sync.", COLOR_AMBER),
    ("Mode B: Pure Java Standalone", "Runs Embedded Java Server inside Swing.\nZero installation needed: no Node, no npm, no MongoDB.", COLOR_EMERALD),
    ("Seamless Compatibility", "The Chrome Extension talks to port 5000 regardless of which backend is active!", COLOR_TEXT_WHITE)
]

for title, desc, acc in comp_rows:
    pt = tf_comp.add_paragraph()
    pt.text = "▶ " + title
    pt.font.name = FONT_HEADING
    pt.font.size = Pt(13)
    pt.font.bold = True
    pt.font.color.rgb = acc
    pt.space_before = Pt(14)
    
    pd = tf_comp.add_paragraph()
    pd.text = desc
    pd.font.name = FONT_BODY
    pd.font.size = Pt(11)
    pd.font.color.rgb = COLOR_TEXT_LIGHT_MUTED
    pd.space_before = Pt(3)

s8.notes_slide.notes_text_frame.text = (
    "The embedded server is an architectural highlight. If a professor, evaluator, or user doesn't have Node.js or "
    "MongoDB installed, they can run ChronoTrack purely in Java with a single command. The Chrome Extension connects "
    "to the Java server just as easily as it connects to Express."
)

# ==============================================================================
# SLIDE 9: Technology Stack Summary
# ==============================================================================
s9 = prs.slides.add_slide(blank_layout)
set_slide_background(s9, COLOR_BG_LIGHT)
add_header(s9, "Full Technology Stack Comparison", "TECH STACK")

tech_columns = [
    ("Browser Extension", COLOR_CYAN, [
        ("Language", "JavaScript (ES6+)"),
        ("Platform", "Chrome Extensions MV3"),
        ("APIs", "chrome.webNavigation, chrome.storage, chrome.tabs, chrome.notifications"),
        ("Auth", "Google OAuth 2.0"),
        ("UI", "HTML5, CSS3, Responsive Grid")
    ]),
    ("Node.js Backend", COLOR_EMERALD, [
        ("Runtime", "Node.js (v18+)"),
        ("Framework", "Express.js 5"),
        ("Database", "MongoDB / Mongoose 9"),
        ("Security", "CORS, dotenv, jsonwebtoken"),
        ("Protocol", "RESTful JSON over HTTP")
    ]),
    ("Java Desktop Suite", COLOR_PRIMARY, [
        ("Language", "Java 17 / 21 / 26"),
        ("GUI Toolkit", "Java Swing (javax.swing)"),
        ("Graphics", "Java2D (java.awt.Graphics2D)"),
        ("HTTP Client", "java.net.http.HttpClient"),
        ("Embedded Srv", "com.sun.net.httpserver"),
        ("Build Tool", "Maven (pom.xml) + Batch scripts")
    ])
]

for idx, (col_title, col_color, attributes) in enumerate(tech_columns):
    left = Inches(0.8 + idx * 3.95)
    create_card(s9, left, Inches(1.8), Inches(3.8), Inches(5.0))
    
    tb = s9.shapes.add_textbox(left + Inches(0.25), Inches(2.0), Inches(3.3), Inches(4.6))
    tf = tb.text_frame
    tf.word_wrap = True
    
    p = tf.paragraphs[0]
    p.text = col_title
    p.font.name = FONT_HEADING
    p.font.size = Pt(17)
    p.font.bold = True
    p.font.color.rgb = col_color
    
    for key, val in attributes:
        pk = tf.add_paragraph()
        pk.text = key.upper()
        pk.font.name = FONT_HEADING
        pk.font.size = Pt(10.5)
        pk.font.bold = True
        pk.font.color.rgb = COLOR_TEXT_MUTED
        pk.space_before = Pt(10)
        
        pv = tf.add_paragraph()
        pv.text = val
        pv.font.name = FONT_BODY
        pv.font.size = Pt(12)
        pv.font.bold = True
        pv.font.color.rgb = COLOR_TEXT_MAIN
        pv.space_before = Pt(2)

s9.notes_slide.notes_text_frame.text = (
    "This slide summarizes the cross-platform tech stack. Each technology was selected for specific strengths: "
    "Manifest V3 for seamless web capture, Express and MongoDB for schema flexibility, and Java SE + Swing for "
    "robust, zero-dependency desktop performance."
)

# ==============================================================================
# SLIDE 10: How to Run & Quickstart
# ==============================================================================
s10 = prs.slides.add_slide(blank_layout)
set_slide_background(s10, COLOR_BG_LIGHT)
add_header(s10, "Execution & Setup Guide", "SETUP & DEMO")

steps = [
    ("Step 1: Run Java Swing App", [
        "Navigate to desktop-swing directory:",
        "cd desktop-swing",
        "Run with batch script: run.bat",
        "Or run with pure Java server: run.bat --server --seed",
        "Or use Maven: mvn compile exec:java"
    ], COLOR_PRIMARY, "☕"),
    ("Step 2: Load Chrome Extension", [
        "Open Chrome and navigate to chrome://extensions/",
        "Enable 'Developer mode' toggle in top-right corner.",
        "Click 'Load unpacked'.",
        "Select the d:\\ChronoTrack\\extension directory.",
        "Extension is active and monitoring browsing visits!"
    ], COLOR_CYAN, "🧩"),
    ("Step 3: (Optional) Run Node Backend", [
        "If choosing MongoDB mode:",
        "cd backend",
        "npm install",
        "npm start (Runs Express on port 5000)",
        "Desktop app connects automatically!"
    ], COLOR_EMERALD, "🌐")
]

for idx, (title, lines, color, icon) in enumerate(steps):
    left = Inches(0.8 + idx * 3.95)
    create_card(s10, left, Inches(1.8), Inches(3.8), Inches(5.0))
    
    tb = s10.shapes.add_textbox(left + Inches(0.25), Inches(2.0), Inches(3.3), Inches(4.6))
    tf = tb.text_frame
    tf.word_wrap = True
    
    p = tf.paragraphs[0]
    p.text = f"{icon}  {title}"
    p.font.name = FONT_HEADING
    p.font.size = Pt(16)
    p.font.bold = True
    p.font.color.rgb = color
    
    for line in lines:
        pl = tf.add_paragraph()
        if line.startswith("cd ") or line.startswith("run.bat") or line.startswith("npm ") or line.startswith("mvn "):
            pl.text = "$ " + line
            pl.font.name = "Consolas"
            pl.font.size = Pt(11)
            pl.font.bold = True
            pl.font.color.rgb = COLOR_PRIMARY
        else:
            pl.text = "• " + line
            pl.font.name = FONT_BODY
            pl.font.size = Pt(11.5)
            pl.font.color.rgb = COLOR_TEXT_MAIN
        pl.space_before = Pt(6)

s10.notes_slide.notes_text_frame.text = (
    "Getting started with ChronoTrack is effortless. The user can launch the Java Swing app in seconds using run.bat, "
    "load the extension in Chrome via developer mode, and start tracking immediately. No complex build tools are required."
)

# ==============================================================================
# SLIDE 11: Roadmap & Future Directions
# ==============================================================================
s11 = prs.slides.add_slide(blank_layout)
set_slide_background(s11, COLOR_BG_LIGHT)
add_header(s11, "Roadmap & Future Enhancements", "FUTURE OUTLOOK")

roadmap_cards = [
    ("Phase 1: Multi-Browser Expansion", [
        "Port extension manifest to Firefox (WebExtensions) and Safari.",
        "Add unified sync across multiple browsers on the same machine.",
        "Active tab duration tracking (time spent per page)."
    ], COLOR_CYAN, "🌐"),
    ("Phase 2: AI Productivity Scoring", [
        "Automated domain categorization (Productivity, Social, Learning).",
        "Local LLM integration for daily browsing summarization.",
        "Custom goal tracking and distraction alert thresholds."
    ], COLOR_PRIMARY, "🧠"),
    ("Phase 3: Enterprise & Team Analytics", [
        "Encrypted team history aggregation for collaborative teams.",
        "Role-based access control and organizational dashboards.",
        "Automated PDF export reports and executive audit summaries."
    ], COLOR_PURPLE, "🏢")
]

for idx, (title, items, color, icon) in enumerate(roadmap_cards):
    left = Inches(0.8 + idx * 3.95)
    create_card(s11, left, Inches(1.8), Inches(3.8), Inches(5.0))
    
    tb = s11.shapes.add_textbox(left + Inches(0.25), Inches(2.0), Inches(3.3), Inches(4.6))
    tf = tb.text_frame
    tf.word_wrap = True
    
    p = tf.paragraphs[0]
    p.text = f"{icon}  {title}"
    p.font.name = FONT_HEADING
    p.font.size = Pt(16)
    p.font.bold = True
    p.font.color.rgb = color
    
    for item in items:
        pi = tf.add_paragraph()
        pi.text = "✔ " + item
        pi.font.name = FONT_BODY
        pi.font.size = Pt(12)
        pi.font.color.rgb = COLOR_TEXT_MAIN
        pi.space_before = Pt(12)

s11.notes_slide.notes_text_frame.text = (
    "Looking forward, the architecture is ready for future enhancements: multi-browser support for Firefox and Safari, "
    "AI productivity scoring to categorize sites automatically, and enterprise team analytics."
)

# ==============================================================================
# SLIDE 12: Conclusion & Q&A (Dark Theme)
# ==============================================================================
s12 = prs.slides.add_slide(blank_layout)
set_slide_background(s12, COLOR_BG_DARK)

tb_end = s12.shapes.add_textbox(Inches(1.5), Inches(1.6), Inches(10.3), Inches(4.5))
tf_end = tb_end.text_frame
tf_end.word_wrap = True

pe1 = tf_end.paragraphs[0]
pe1.text = "Thank You!"
pe1.font.name = FONT_HEADING
pe1.font.size = Pt(50)
pe1.font.bold = True
pe1.font.color.rgb = COLOR_TEXT_WHITE
pe1.alignment = PP_ALIGN.CENTER

pe2 = tf_end.add_paragraph()
pe2.text = "ChronoTrack: Empowering Smart, Private, and Insightful Browsing Analytics"
pe2.font.name = FONT_BODY
pe2.font.size = Pt(20)
pe2.font.color.rgb = COLOR_CYAN
pe2.alignment = PP_ALIGN.CENTER
pe2.space_before = Pt(12)

pe3 = tf_end.add_paragraph()
pe3.text = "Open Source · Cross-Platform · Dual-Backend Architecture"
pe3.font.name = FONT_BODY
pe3.font.size = Pt(14)
pe3.font.color.rgb = COLOR_TEXT_LIGHT_MUTED
pe3.alignment = PP_ALIGN.CENTER
pe3.space_before = Pt(8)

pe4 = tf_end.add_paragraph()
pe4.text = "Questions & Demonstration"
pe4.font.name = FONT_HEADING
pe4.font.size = Pt(24)
pe4.font.bold = True
pe4.font.color.rgb = COLOR_EMERALD
pe4.alignment = PP_ALIGN.CENTER
pe4.space_before = Pt(36)

s12.notes_slide.notes_text_frame.text = (
    "Thank you for your time. ChronoTrack demonstrates a complete, cohesive full-stack solution bridging browser extensions, "
    "REST services, and native desktop Java Swing applications. We are now open for live questions and demonstrations."
)

# --- Save Presentation ---
output_path = os.path.abspath("d:/ChronoTrack/ChronoTrack_Presentation.pptx")
prs.save(output_path)
print(f"Presentation successfully created at: {output_path}")
