# Jakarta UI Dashboard - Reference UI Implementation

## Overview

The Jakarta Dashboard is a modern, Jakarta-styled admin interface that matches the reference UI provided. It provides a clean, professional design with dropdown navigation, form sections, and modern UI components.

## Features

- **Modern Jakarta UI Styling**: Clean, professional design matching reference UI
- **Dropdown Navigation Menu**: Orange floating dropdown widget (like reference UI)
- **Section-Based Layout**: Multiple sections for different functionalities
- **Form Fields**: Read-only form fields displaying general details
- **File Upload**: File upload capability (Max 5 files)
- **Transaction Details**: Collapsible transaction details section
- **Add Charge**: Charge management section
- **Responsive Design**: Works on different screen sizes

## Access

**URL:** `/app/admin/JakartaDashboard.vm`

**Menu Link:** "Jakarta Dashboard (Reference UI)" - visible to admin users only

## File Structure

```
turbine-model-controller/src/main/java/modules/screens/admin/
└── JakartaDashboard.java          # Screen class

webapp/src/main/webapp/templates/app/
├── screens/admin/
│   └── JakartaDashboard.vm          # Main template
└── jakarta/
    ├── styles/
    │   └── jakarta-dashboard.css    # Jakarta UI styles
    └── scripts/
        └── jakarta-dashboard.js     # JavaScript functionality
```

## Components

### 1. Header
- Dark gray background (#424242)
- Welcome message with user name
- Logout link

### 2. Sidebar Navigation
- Left sidebar with navigation items
- Active state highlighting
- Icons for each section
- Sections:
  - General Details
  - File Upload
  - Transaction Details
  - Add Charge

### 3. Floating Dropdown Widget (Reference UI Style)
- Orange button (#FF9800) with dropdown menu
- Fixed position (stays visible while scrolling)
- Menu options:
  - Show/Hide Form Summary | Top
  - Transaction Details
  - Reporting Message Details
  - Add Charge
  - File Upload Details
  - General Details

### 4. Work Area Sections

#### General Details Section
- Form fields displaying:
  - Company ID
  - Transfer From
  - Corporate
  - Payment Type
  - Lump Sum Debit
  - Total amount of records
  - Highest Amount of records
  - Bank (with dropdown widget)
  - BIB Ref
  - Parent Reference(s)
  - Application Date
  - Product Group
  - Transfer Date

#### File Upload Section
- File upload area
- Max 5 files
- File list display
- Remove file functionality

#### Add Charge Section
- Charges list
- Add charge button
- Empty state when no charges

#### Transaction Details Section
- Collapsible section
- Transaction table
- Show/Hide toggle

## Styling

### Color Scheme
- **Header**: Dark gray (#424242)
- **Sidebar**: White background with gray borders
- **Dropdown Widget**: Orange (#FF9800) with darker border (#F57C00)
- **Section Headers**: Orange background (#FF9800)
- **Footer**: Purple (#9C27B0)
- **Primary Buttons**: Blue (#2196F3)

### Typography
- Font Family: Segoe UI, Tahoma, Geneva, Verdana, sans-serif
- Base Font Size: 14px
- Section Titles: 16px, bold
- Form Labels: 12px, medium weight

## JavaScript Functionality

### Main Functions

1. **showSection(sectionId)**: Shows a specific section and hides others
2. **toggleDropdown()**: Toggles the dropdown menu visibility
3. **hideDropdown()**: Hides the dropdown menu
4. **toggleFormSummary()**: Shows/hides form summary sections
5. **toggleTransactionDetails()**: Shows/hides transaction details
6. **handleFileUpload(files)**: Handles file upload (to be implemented)
7. **removeFile(fileId)**: Removes a file (to be implemented)
8. **showAddChargeForm()**: Shows add charge form (to be implemented)

### Event Handlers

- Click outside dropdown to close
- ESC key to close dropdown
- Section navigation via sidebar
- Section navigation via dropdown menu

## Data Model

The screen class (`JakartaDashboard.java`) provides:

- **generalDetails**: Map containing form field values
- **uploadedFiles**: List of uploaded files
- **charges**: List of charges
- **showTransactionDetails**: Boolean for transaction details visibility
- **maxFiles**: Maximum number of files (5)

## Authorization

Only users with admin roles can access:
- `turbineadmin`
- `admin`
- `ADMIN`
- `Admin`
- Username containing "admin" (case-insensitive)

## Browser Compatibility

- Modern browsers (Chrome, Firefox, Edge, Safari)
- Responsive design for tablets and mobile devices
- CSS Grid and Flexbox support required

## Future Enhancements

1. **File Upload**: Implement actual file upload functionality
2. **Add Charge**: Implement charge form and submission
3. **Transaction Details**: Load real transaction data
4. **Reporting Message**: Implement reporting message details
5. **Form Submission**: Add form submission and validation
6. **AJAX Loading**: Load data dynamically via AJAX
7. **Real-time Updates**: WebSocket support for real-time updates

## Troubleshooting

### CSS/JS Not Loading

Check file paths:
- CSS: `/templates/app/jakarta/styles/jakarta-dashboard.css`
- JS: `/templates/app/jakarta/scripts/jakarta-dashboard.js`

### Dropdown Not Working

1. Check browser console for JavaScript errors
2. Verify `jakarta-dashboard.js` is loaded
3. Check if jQuery or other dependencies are required

### Sections Not Showing

1. Check if JavaScript is enabled
2. Verify `showSection()` function is called correctly
3. Check browser console for errors

## Related Files

- `Dashboard.java`: Original admin dashboard screen class
- `Dashboard.vm`: Original admin dashboard template
- `admin-dashboard.css`: Original admin dashboard styles
- `admin-dashboard.js`: Original admin dashboard JavaScript

## Notes

- This is a separate module that doesn't interfere with existing application
- Uses Velocity templates (not Jakarta Faces/JSF)
- "Jakarta UI" refers to the styling theme, not Jakarta EE framework
- Compatible with Turbine 7 framework
