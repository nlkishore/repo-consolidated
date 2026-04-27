# Cursor Prompt Template for Wireframe-Based Development

## How to Use This Template

1. Fill in the wireframe details below
2. Attach wireframe image/screenshot
3. Copy the completed prompt to Cursor
4. Cursor will generate the code

---

## Prompt Template

```
I need to create a [SCREEN NAME] view for an Apache Turbine 7 application. 
Please create the view matching the attached wireframe and following these specifications:

### WIREFRAME DETAILS
[Attach wireframe image/screenshot here]

### LAYOUT SPECIFICATIONS
- Screen Dimensions: [e.g., 1920x1080 desktop]
- Layout Type: [e.g., Header + Sidebar + Content]
- Header: [height, background color, content]
- Sidebar: [width, position, background color, menu items]
- Main Content: [width, padding, background color]
- Footer: [if applicable]

### COMPONENT SPECIFICATIONS

#### Component 1: [Name]
- Location: [where it appears]
- Size: [dimensions]
- Background: [color]
- Content: [what it displays]
- Styling: [fonts, borders, etc.]

#### Component 2: [Name]
[Repeat for each component]

### COLOR PALETTE
- Primary: #[hex code] - [usage]
- Secondary: #[hex code] - [usage]
- Background: #[hex code] - [usage]
- Text: #[hex code] - [usage]
[List all colors]

### TYPOGRAPHY
- Font Family: [e.g., Arial, sans-serif]
- Headers: [size, weight]
- Body: [size, weight]
- Links: [size, color, hover state]

### INTERACTIVE ELEMENTS
- Buttons: [style, colors, sizes]
- Forms: [field styles, labels]
- Tables: [header style, row style, borders]
- Navigation: [menu style, hover states]

### TECHNICAL REQUIREMENTS
- Framework: Apache Turbine 7
- Template Engine: Velocity (.vm files)
- CSS Framework: [W3.CSS / Custom / Bootstrap]
- JavaScript: [Vanilla JS / jQuery / None]
- Browser Support: [e.g., Modern browsers, IE11+]
- Responsive: [Yes/No, breakpoints]

### FUNCTIONAL REQUIREMENTS
- [Feature 1]: [description]
- [Feature 2]: [description]
- [Feature 3]: [description]

### LEGACY APPLICATION MATCHING
- Must match legacy application appearance: [Yes/No]
- Key elements to preserve: [list]
- Improvements allowed: [list]

### FILES TO CREATE
Please create:
1. Velocity template: [path/filename.vm]
2. CSS file: [path/filename.css] (if needed)
3. JavaScript file: [path/filename.js] (if needed)
4. Screen class: [package.ClassName.java] (if needed)

### ADDITIONAL NOTES
[Any other important information]
```

---

## Example: Filled Template

```
I need to create a User Management view for an Apache Turbine 7 application. 
Please create the view matching the attached wireframe and following these specifications:

### WIREFRAME DETAILS
[User Management wireframe image attached]

### LAYOUT SPECIFICATIONS
- Screen Dimensions: 1920x1080 desktop, responsive
- Layout Type: Header + Sidebar + Content + Footer
- Header: 60px height, #2196F3 background, "User Management" title left, user info right
- Sidebar: 250px width, left side, #F5F5F5 background, navigation menu
- Main Content: Flexible width, 20px padding, white background
- Footer: 40px height, purple background, "Powered by Apache Turbine"

### COMPONENT SPECIFICATIONS

#### Component 1: Search Box
- Location: Top of main content
- Size: 100% width, 40px height
- Background: White
- Content: Text input with placeholder "Search users..."
- Styling: 1px solid #BDBDBD border, 4px border radius, 10px padding

#### Component 2: User Table
- Location: Below search box
- Size: 100% width
- Background: White
- Content: Table with columns: ID, Name, Email, Role, Actions
- Styling: Alternating row colors, gray header row

### COLOR PALETTE
- Primary: #2196F3 - Header, buttons, links
- Secondary: #757575 - Secondary buttons
- Background: #FFFFFF - Main content
- Sidebar: #F5F5F5 - Sidebar background
- Table Header: #E0E0E0 - Table header row
- Text: #000000 - Primary text

### TYPOGRAPHY
- Font Family: Arial, sans-serif
- Headers: 18px, bold
- Body: 14px, normal
- Links: 14px, #2196F3, underline on hover

### INTERACTIVE ELEMENTS
- Buttons: Blue (#2196F3), white text, 12px padding, 4px border radius
- Forms: White background, gray border, 10px padding
- Tables: Gray header, alternating white/light gray rows
- Navigation: Gray background, blue on hover/active

### TECHNICAL REQUIREMENTS
- Framework: Apache Turbine 7
- Template Engine: Velocity (.vm files)
- CSS Framework: W3.CSS (already in use)
- JavaScript: Vanilla JS for interactions
- Browser Support: Modern browsers (Chrome, Firefox, Edge)
- Responsive: Yes, breakpoints at 768px and 1200px

### FUNCTIONAL REQUIREMENTS
- Search: Real-time filtering of user table
- Table: Sortable columns, pagination
- Actions: Edit and Delete buttons per row
- Navigation: Sidebar menu for different sections

### LEGACY APPLICATION MATCHING
- Must match legacy application appearance: Yes
- Key elements to preserve: Color scheme, layout structure, navigation pattern
- Improvements allowed: Better responsive design, improved accessibility

### FILES TO CREATE
Please create:
1. Velocity template: webapp/src/main/webapp/templates/app/screens/UserManagement.vm
2. CSS file: webapp/src/main/webapp/styles/user-management.css
3. JavaScript file: webapp/src/main/webapp/scripts/user-management.js
4. Screen class: turbine-model-controller/src/main/java/modules/screens/UserManagement.java

### ADDITIONAL NOTES
- This is part of an admin module
- Must integrate with existing Turbine security
- Should use existing navigation structure
```

---

## Quick Reference: Common Elements

### Header Pattern
```
Header: 60px height, [color] background
- Left: Logo/Title
- Right: User info + Logout
```

### Sidebar Pattern
```
Sidebar: 250px width, [color] background
- Menu items with icons
- Active state highlighting
- Hover effects
```

### Table Pattern
```
Table: 100% width
- Header row: [color] background
- Data rows: Alternating colors
- Borders: 1px solid [color]
- Cell padding: 10px
```

### Form Pattern
```
Form: Full width
- Labels: Left aligned, 14px
- Inputs: Full width, 40px height
- Buttons: Right aligned
```

---

## Tips for Best Results

1. **Be Specific**: Include exact measurements, colors, and fonts
2. **Provide Context**: Mention legacy application if matching
3. **Include Examples**: Reference existing working screens
4. **Specify Framework**: Always mention Turbine 7 and Velocity
5. **Attach Images**: Wireframe images help Cursor understand layout
6. **Break Down**: For complex screens, break into smaller requests
7. **Iterate**: Start with basic layout, then add details

---

## Example Prompts by Screen Type

### List/Table Screen
```
Create a [Entity] List screen showing a table of [entities] with:
- Search box at top
- Table with columns: [list]
- Pagination at bottom
- Action buttons per row
- Match legacy application styling
```

### Form Screen
```
Create a [Entity] Form screen with:
- Form fields: [list]
- Labels on left, inputs on right
- Save and Cancel buttons
- Validation messages
- Match legacy application layout
```

### Dashboard Screen
```
Create a Dashboard screen with:
- Header with title
- Sidebar navigation
- Multiple content sections
- Charts/graphs (if needed)
- Summary cards
- Match legacy application appearance
```
