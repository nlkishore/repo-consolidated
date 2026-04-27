# Wireframe Guide for Legacy UI Recreation

## Overview
This guide explains how to create wireframes and use them with Cursor to recreate legacy application views, minimizing customer impact.

## 1. Wireframe Creation Tools

### Recommended Tools:
1. **Figma** (Free, Web-based)
   - Best for detailed wireframes
   - Export as images or PDF
   - Collaborative

2. **Draw.io / diagrams.net** (Free, Web-based)
   - Simple wireframes
   - Export as PNG, SVG, PDF
   - Good for quick sketches

3. **Balsamiq** (Paid, Desktop/Web)
   - Purpose-built for wireframes
   - Low-fidelity mockups
   - Export as PNG, PDF

4. **Pencil Project** (Free, Desktop)
   - Open-source wireframing
   - Export as PNG, PDF

5. **Pen & Paper + Scanner**
   - Draw wireframes manually
   - Scan/photograph
   - Simple and fast

## 2. Wireframe Components to Document

### Essential Elements:
1. **Layout Structure**
   - Header (height, content)
   - Sidebar/Navigation (width, position)
   - Main content area
   - Footer (height, content)

2. **Navigation Elements**
   - Menu items and hierarchy
   - Dropdown menus
   - Breadcrumbs
   - Action buttons

3. **Content Sections**
   - Form fields and labels
   - Data tables/grids
   - Search boxes
   - Filter options

4. **Visual Elements**
   - Colors (hex codes if possible)
   - Fonts and sizes
   - Spacing and padding
   - Borders and dividers

5. **Interactive Elements**
   - Buttons (primary, secondary)
   - Links
   - Tooltips
   - Modals/dialogs

## 3. Wireframe Documentation Format

### Recommended Structure:

```markdown
# [Screen Name] Wireframe

## Layout
- Header: 60px height, blue background (#2196F3)
- Sidebar: 200px width, left side, gray background
- Content: Remaining width, white background
- Footer: 40px height, purple background

## Components
1. Header
   - Logo (left)
   - User name (right)
   - Logout button (right)

2. Sidebar
   - Menu item 1
   - Menu item 2 (with submenu)
   - Menu item 3

3. Main Content
   - Search box (top)
   - Results table (below)
   - Pagination (bottom)

## Colors
- Primary: #2196F3
- Secondary: #757575
- Background: #FFFFFF
- Text: #000000

## Typography
- Headers: Arial, 18px, bold
- Body: Arial, 14px, normal
```

## 4. Using Wireframes with Cursor

### Method 1: Image + Description
1. Create wireframe image (PNG/JPG)
2. Attach image to Cursor chat
3. Provide detailed description:
   ```
   "Create a view matching this wireframe:
   - Header with blue background, 60px height
   - Left sidebar, 200px width
   - Main content area with search and table
   - Use Turbine 7 framework, Velocity templates"
   ```

### Method 2: Structured Documentation
1. Create detailed markdown document
2. Include:
   - Layout specifications
   - Component descriptions
   - Color codes
   - Font specifications
3. Reference in Cursor: "Create view based on WIREFRAME-SPEC.md"

### Method 3: HTML/CSS Mockup
1. Create static HTML/CSS mockup
2. Share code with Cursor
3. Request: "Convert this HTML/CSS to Turbine Velocity template"

## 5. Best Practices for Legacy UI Recreation

### 1. Capture All Details
- Take screenshots of legacy application
- Document exact pixel measurements
- Note all colors (use color picker)
- Record font families and sizes

### 2. Document User Flows
- How users navigate between screens
- Form submission flows
- Error handling displays
- Success messages

### 3. Component Library
- Create reusable component specifications
- Document common patterns
- Maintain consistency across screens

### 4. Responsive Considerations
- Document breakpoints
- Note which elements are fixed vs. responsive
- Mobile/tablet adaptations

## 6. Example Wireframe Template

```markdown
# Admin Dashboard Wireframe

## Screen Dimensions
- Desktop: 1920x1080
- Tablet: 768x1024
- Mobile: 375x667

## Layout Grid
- Header: 100% width, 60px height
- Body: Flexbox layout
  - Sidebar: 250px width, 100vh height
  - Content: calc(100% - 250px) width

## Header Component
- Background: #2196F3 (blue)
- Text: White, 16px, Arial
- Left: Logo (50px height)
- Right: "Hello [Username]" + Logout button
- Padding: 15px horizontal

## Sidebar Component
- Background: #F5F5F5 (light gray)
- Width: 250px
- Menu items:
  - User Management (icon + text)
  - Group Management (icon + text)
  - Role Management (icon + text)
  - Permission Management (icon + text)
- Active item: Blue background (#2196F3)
- Hover: Light blue (#E3F2FD)

## Main Content Area
- Background: White
- Padding: 20px
- Sections:
  1. User Details
     - Search box (full width)
     - Results table (below)
  2. Group Details
     - Same structure
  3. Roles Details
     - Same structure
  4. Permission Details
     - Same structure

## Table/Grid Specifications
- Header row: Gray background (#E0E0E0)
- Alternating rows: White/Light gray (#FAFAFA)
- Border: 1px solid #E0E0E0
- Cell padding: 10px
- Font: 14px Arial

## Buttons
- Primary: Blue (#2196F3), white text, 12px padding
- Secondary: Gray (#757575), white text, 12px padding
- Border radius: 4px
```

## 7. Cursor Prompt Template

Use this template when asking Cursor to create views:

```
Create a [Screen Name] view for Turbine 7 application matching this wireframe:

LAYOUT:
- Header: [specifications]
- Sidebar: [specifications]
- Content: [specifications]

COMPONENTS:
1. [Component 1]: [details]
2. [Component 2]: [details]

STYLING:
- Colors: [list]
- Fonts: [list]
- Spacing: [list]

TECHNICAL REQUIREMENTS:
- Framework: Apache Turbine 7
- Template Engine: Velocity
- CSS Framework: W3.CSS (or custom)
- Must be responsive
- Must match legacy application appearance

Please create:
1. Velocity template (.vm file)
2. CSS file (if needed)
3. JavaScript file (if needed)
4. Screen class (Java) if needed
```

## 8. Tools for Capturing Legacy UI

### Screenshot Tools:
- **Snipping Tool** (Windows built-in)
- **Greenshot** (Free, Windows)
- **ShareX** (Free, Windows)
- **Lightshot** (Free, Cross-platform)

### Color Picker Tools:
- **ColorPix** (Free, Windows)
- **Just Color Picker** (Free, Windows)
- **Browser DevTools** (F12 → Color picker)

### Measurement Tools:
- **Browser DevTools** (Inspect element → Computed)
- **PixelRuler** (Free, Windows)
- **ScreenRuler** (Free, Windows)

## 9. Documentation Checklist

Before requesting Cursor to create a view, ensure you have:

- [ ] Wireframe image or detailed sketch
- [ ] Layout specifications (dimensions, positioning)
- [ ] Color palette (hex codes)
- [ ] Typography specifications
- [ ] Component list and descriptions
- [ ] User interaction flows
- [ ] Responsive breakpoints
- [ ] Legacy application screenshots
- [ ] Technical requirements (framework, browser support)

## 10. Example: Complete Wireframe Document

See `WIREFRAME-ADMIN-DASHBOARD.md` for a complete example.

## 11. Tips for Minimizing Customer Impact

1. **Visual Consistency**
   - Match colors exactly
   - Use same fonts
   - Maintain spacing patterns

2. **Functional Parity**
   - All features from legacy must work
   - Same navigation structure
   - Same form layouts

3. **Gradual Migration**
   - Start with one screen
   - Get user feedback
   - Iterate before moving to next screen

4. **User Testing**
   - Test with actual users
   - Gather feedback
   - Make adjustments

## 12. Next Steps

1. Create wireframes for each legacy screen
2. Document specifications in markdown
3. Take screenshots of legacy application
4. Use Cursor with wireframes to generate code
5. Test and refine
6. Deploy incrementally
