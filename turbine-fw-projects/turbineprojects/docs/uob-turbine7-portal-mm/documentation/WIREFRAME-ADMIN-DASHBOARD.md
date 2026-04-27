# Admin Dashboard Wireframe Specification

## Purpose
This document provides detailed specifications for recreating the Admin Dashboard view to match legacy application appearance.

## Screen Information
- **Screen Name**: Admin Dashboard
- **URL**: `/app/admin/Dashboard.vm`
- **Screen Class**: `com.uob.modules.screens.admin.Dashboard`
- **Template**: `templates/app/screens/admin/Dashboard.vm`

## Layout Structure

### Overall Dimensions
- **Desktop Viewport**: 1920x1080 (full screen)
- **Minimum Width**: 1200px
- **Layout Type**: Fixed header + Flexbox body

### Layout Grid
```
┌─────────────────────────────────────────┐
│           HEADER (60px height)          │
├──────────┬──────────────────────────────┤
│          │                               │
│ SIDEBAR  │      MAIN CONTENT AREA       │
│ (250px)  │      (flexible width)        │
│          │                               │
│          │                               │
└──────────┴───────────────────────────────┘
```

## Component Specifications

### 1. Header Component
**Location**: Top of page, full width
**Height**: 60px
**Background Color**: #2196F3 (Blue)
**Text Color**: #FFFFFF (White)

**Content**:
- **Left Side**: 
  - Title: "Admin Dashboard"
  - Font: Arial, 24px, bold
  - Padding: 15px left
- **Right Side**:
  - Text: "Welcome, [First Name]"
  - Font: Arial, 14px, normal
  - Padding: 15px right

**CSS**:
```css
.admin-header {
    background-color: #2196F3;
    color: white;
    padding: 15px 20px;
    height: 60px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
```

### 2. Sidebar Component
**Location**: Left side of page
**Width**: 250px
**Height**: calc(100vh - 60px)
**Background Color**: #F5F5F5 (Light Gray)
**Position**: Fixed (stays visible on scroll)

**Menu Items**:
1. User Details
   - Icon: User icon (Font Awesome)
   - Text: "User Details"
   - Active state: Blue background (#2196F3)
2. Group Details
   - Icon: Group icon
   - Text: "Group Details"
3. Roles Details
   - Icon: Role icon
   - Text: "Roles Details"
4. Permission Details
   - Icon: Key icon
   - Text: "Permission Details"

**Menu Item Styling**:
- Font: Arial, 14px
- Padding: 15px 20px
- Hover: Light blue background (#E3F2FD)
- Active: Blue background (#2196F3), white text
- Border bottom: 1px solid #E0E0E0

**CSS**:
```css
.admin-sidebar {
    width: 250px;
    background-color: #F5F5F5;
    height: calc(100vh - 60px);
    position: fixed;
    left: 0;
    top: 60px;
    overflow-y: auto;
}

.sidebar-menu-item {
    padding: 15px 20px;
    cursor: pointer;
    border-bottom: 1px solid #E0E0E0;
}

.sidebar-menu-item:hover {
    background-color: #E3F2FD;
}

.sidebar-menu-item.active {
    background-color: #2196F3;
    color: white;
}
```

### 3. Main Content Area
**Location**: Right side of sidebar
**Width**: calc(100% - 250px)
**Margin Left**: 250px (to account for fixed sidebar)
**Background Color**: #FFFFFF (White)
**Padding**: 20px

**Sections** (4 total, stacked vertically):
1. User Details Section
2. Group Details Section
3. Roles Details Section
4. Permission Details Section

### 4. Work Area Section
**Structure**: Each section has same layout

**Components**:
1. **Section Header**
   - Background: #E0E0E0 (Light Gray)
   - Text: Section name (e.g., "User Details")
   - Font: Arial, 18px, bold
   - Padding: 10px 15px
   - Border: 1px solid #BDBDBD

2. **Search Box**
   - Width: 100%
   - Height: 40px
   - Border: 1px solid #BDBDBD
   - Border radius: 4px
   - Padding: 10px
   - Font: Arial, 14px
   - Placeholder: "Search [entity] by name..."

3. **Results Grid/Table**
   - Width: 100%
   - Border: 1px solid #E0E0E0
   - Header row: Gray background (#E0E0E0)
   - Data rows: Alternating white/light gray (#FAFAFA)
   - Cell padding: 10px
   - Font: Arial, 14px

**Section CSS**:
```css
.workarea-section {
    margin-bottom: 30px;
    background: white;
    border: 1px solid #E0E0E0;
    border-radius: 4px;
}

.section-header {
    background-color: #E0E0E0;
    padding: 10px 15px;
    border-bottom: 1px solid #BDBDBD;
    font-weight: bold;
    font-size: 18px;
}

.search-box {
    width: 100%;
    height: 40px;
    padding: 10px;
    border: 1px solid #BDBDBD;
    border-radius: 4px;
    font-size: 14px;
}

.results-table {
    width: 100%;
    border-collapse: collapse;
}

.results-table th {
    background-color: #E0E0E0;
    padding: 10px;
    text-align: left;
    font-weight: bold;
}

.results-table td {
    padding: 10px;
    border-bottom: 1px solid #E0E0E0;
}

.results-table tr:nth-child(even) {
    background-color: #FAFAFA;
}
```

### 5. Tooltip Window Component
**Type**: Floating window
**Position**: Fixed, bottom-right
**Size**: 200px width, 300px height
**Background**: White with shadow
**Visibility**: Toggle with Ctrl+T
**Content**: Quick navigation links

**CSS**:
```css
.tooltip-window {
    position: fixed;
    bottom: 20px;
    right: 20px;
    width: 200px;
    height: 300px;
    background: white;
    box-shadow: 0 4px 8px rgba(0,0,0,0.2);
    border-radius: 4px;
    padding: 15px;
    z-index: 1000;
    display: none; /* Toggle with JavaScript */
}
```

## Color Palette

| Element | Color | Hex Code |
|---------|-------|----------|
| Primary Blue | #2196F3 | Header, active menu items |
| Light Blue | #E3F2FD | Hover states |
| Light Gray | #F5F5F5 | Sidebar background |
| Medium Gray | #E0E0E0 | Borders, table headers |
| Dark Gray | #BDBDBD | Section borders |
| Very Light Gray | #FAFAFA | Alternating table rows |
| White | #FFFFFF | Main content background |
| Text Dark | #000000 | Primary text |
| Text Light | #757575 | Secondary text |

## Typography

- **Font Family**: Arial, sans-serif
- **Header Title**: 24px, bold
- **Section Headers**: 18px, bold
- **Menu Items**: 14px, normal
- **Table Headers**: 14px, bold
- **Table Data**: 14px, normal
- **Search Placeholder**: 14px, italic, #9E9E9E

## Spacing

- **Header Padding**: 15px vertical, 20px horizontal
- **Sidebar Item Padding**: 15px vertical, 20px horizontal
- **Content Padding**: 20px all sides
- **Section Margin**: 30px bottom
- **Section Header Padding**: 10px vertical, 15px horizontal
- **Table Cell Padding**: 10px all sides

## Interactive Elements

### Buttons
- **Primary Button**:
  - Background: #2196F3
  - Text: White
  - Padding: 12px 24px
  - Border radius: 4px
  - Hover: Darker blue (#1976D2)

- **Secondary Button**:
  - Background: #757575
  - Text: White
  - Padding: 12px 24px
  - Border radius: 4px

### Links
- **Color**: #2196F3
- **Hover**: Underline
- **Visited**: #1976D2

## Responsive Behavior

### Desktop (> 1200px)
- Full layout with sidebar
- All sections visible

### Tablet (768px - 1200px)
- Sidebar collapses to icon-only
- Content expands to fill space
- Sections stack vertically

### Mobile (< 768px)
- Sidebar becomes hamburger menu
- Full-width content
- Stacked sections
- Smaller fonts

## JavaScript Functionality

1. **Sidebar Navigation**
   - Click menu item → Scroll to section
   - Highlight active section
   - Smooth scrolling

2. **Search Functionality**
   - Real-time filtering
   - Search across all fields
   - Highlight matches

3. **Tooltip Window**
   - Toggle: Ctrl+T
   - Stay visible on scroll
   - Quick navigation links

4. **Table Interactions**
   - Sortable columns
   - Row selection
   - Action buttons

## Velocity Template Structure

```
Dashboard.vm
├── Header Component
├── Sidebar Component
└── Main Content
    ├── User Details Section
    │   ├── Search Box
    │   └── Results Grid
    ├── Group Details Section
    │   ├── Search Box
    │   └── Results Grid
    ├── Roles Details Section
    │   ├── Search Box
    │   └── Results Grid
    └── Permission Details Section
        ├── Search Box
        └── Results Grid
```

## Implementation Notes

1. **Framework**: Apache Turbine 7
2. **Template Engine**: Velocity
3. **CSS**: Custom CSS (can use W3.CSS as base)
4. **JavaScript**: Vanilla JS or jQuery
5. **Icons**: Font Awesome or similar
6. **Layout**: Flexbox for modern browsers

## Testing Checklist

- [ ] Header displays correctly
- [ ] Sidebar navigation works
- [ ] All sections visible
- [ ] Search boxes functional
- [ ] Tables display data
- [ ] Tooltip window toggles
- [ ] Responsive on mobile
- [ ] Colors match specification
- [ ] Fonts match specification
- [ ] Spacing matches specification

## Legacy Application Comparison

**Key Differences to Maintain**:
- Same color scheme
- Same layout structure
- Same navigation pattern
- Same interaction patterns

**Improvements Allowed**:
- Better responsive design
- Improved accessibility
- Modern CSS (flexbox, grid)
- Better performance
