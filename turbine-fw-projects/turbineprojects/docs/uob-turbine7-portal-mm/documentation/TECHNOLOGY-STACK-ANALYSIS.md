# Technology Stack Analysis: Images 2 & 3

## Overview

This document explains the technologies used to create the forms and interfaces shown in **Image 2** (Admin Dashboard Navigation) and **Image 3** (Admin Dashboard with User Details Table).

---

## Technology Stack Breakdown

### 1. **Backend Framework: Apache Turbine 7**

**What it is:**
- Apache Turbine is a Java-based web application framework
- Provides MVC (Model-View-Controller) architecture
- Handles request routing, security, and business logic

**How it's used:**
- **Screen Classes**: Java classes that act as controllers
  - Example: `Dashboard.java` (located in `turbine-model-controller/src/main/java/modules/screens/admin/`)
  - Extends `SecureScreen` for authentication/authorization
  - Loads data from database using `SecurityService`
  - Passes data to Velocity templates via `Context`

**Code Example:**
```java
package com.uob.modules.screens.admin;

public class Dashboard extends SecureScreen {
    @TurbineService
    private SecurityService securityService;
    
    @Override
    protected void doBuildTemplate(PipelineData data, Context context) {
        // Load users, groups, roles, permissions
        List<User> allUsers = security.getUserManager().retrieveList(criteria);
        context.put("allUsers", allUsers);
        // Data is now available in Velocity template
    }
}
```

---

### 2. **Template Engine: Apache Velocity**

**What it is:**
- Server-side template engine for Java
- Uses `.vm` (Velocity Template) files
- Combines HTML with Velocity Template Language (VTL)

**How it's used:**
- **Template Files**: `.vm` files contain HTML + VTL syntax
  - Example: `Dashboard.vm` (located in `webapp/src/main/webapp/templates/app/admin/`)
  - Uses `#parse` directive to include components
  - Uses `$variable` syntax to display data from Java
  - Uses `#if`, `#foreach`, `#set` for logic

**Code Example:**
```velocity
## Dashboard.vm
#if ($allUsers && !$allUsers.isEmpty())
    #foreach ($user in $allUsers)
        <tr>
            <td>$!user.getId()</td>
            <td>$!user.getName()</td>
            <td>$!user.getEmail()</td>
        </tr>
    #end
#else
    <tr><td colspan="3">No users found</td></tr>
#end
```

**Key Velocity Features Used:**
- `#parse("component.vm")` - Includes other templates
- `$variable` - Displays variable values
- `$!variable` - Safe display (handles null)
- `#if / #else / #end` - Conditional logic
- `#foreach / #end` - Loops
- `#set ($var = value)` - Sets variables

---

### 3. **CSS Framework: W3.CSS**

**What it is:**
- Lightweight CSS framework from W3Schools
- Provides utility classes for styling
- No JavaScript dependencies

**How it's used:**
- **External CDN**: Loaded from W3Schools CDN
  ```html
  <link rel="stylesheet" href="http://www.w3schools.com/lib/w3.css">
  <link rel="stylesheet" href="http://www.w3schools.com/lib/w3-theme-blue-grey.css">
  ```

**W3.CSS Classes Used:**
- `w3-navbar` - Navigation bar
- `w3-theme` - Theme colors
- `w3-dropdown-hover` - Dropdown menus
- `w3-card-4` - Card containers
- `w3-table` - Tables
- `w3-button` - Buttons
- `w3-container` - Container divs

**Code Example:**
```html
<nav class="w3-navbar w3-theme">
    <li><a href="#">Home</a></li>
    <li class="w3-dropdown-hover">
        <a href="#">Administration</a>
        <div class="w3-dropdown-content w3-white w3-card-4">
            <a href="#">User List</a>
        </div>
    </li>
</nav>
```

---

### 4. **Custom CSS: admin-dashboard.css**

**What it is:**
- Custom CSS file for Admin Dashboard styling
- Overrides and extends W3.CSS
- Provides specific styling for dashboard components

**How it's used:**
- **File Location**: `webapp/src/main/webapp/templates/app/admin/styles/admin-dashboard.css`
- **Loaded in template**: `<link rel="stylesheet" href="$link.getContextPath()/admin/styles/admin-dashboard.css">`

**Key Styles:**
```css
.admin-container {
    display: flex;
    flex-direction: column;
    height: 100vh;
}

.admin-header {
    background-color: #424242; /* Dark gray */
    color: white;
    padding: 15px 20px;
}

.admin-sidebar {
    width: 180px;
    background-color: #F5F5F5;
    border-right: 1px solid #E0E0E0;
}

.admin-workarea {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
}
```

---

### 5. **JavaScript: admin-dashboard.js**

**What it is:**
- Vanilla JavaScript (no frameworks)
- Handles client-side interactions
- Provides search functionality and section navigation

**How it's used:**
- **File Location**: `webapp/src/main/webapp/templates/app/admin/scripts/admin-dashboard.js`
- **Loaded in template**: `<script src="$link.getContextPath()/admin/scripts/admin-dashboard.js"></script>`

**Key Functions:**
```javascript
// Search functionality
function searchUsers() {
    var searchTerm = document.getElementById('user-search').value.toLowerCase();
    var rows = document.querySelectorAll('#user-grid-body tr');
    rows.forEach(function(row) {
        var text = row.textContent.toLowerCase();
        row.style.display = text.includes(searchTerm) ? '' : 'none';
    });
}

// Section navigation
function scrollToSection(sectionId) {
    var section = document.getElementById(sectionId);
    if (section) {
        section.scrollIntoView({ behavior: 'smooth' });
    }
}
```

---

### 6. **Component Architecture: Modular Velocity Templates**

**What it is:**
- Templates are broken into reusable components
- Each component is a separate `.vm` file
- Components are included using `#parse` directive

**Component Structure:**
```
templates/app/admin/
├── Dashboard.vm (Main template)
└── components/
    ├── Header.vm
    ├── Sidebar.vm
    ├── SearchBox.vm
    ├── ResultsGrid.vm
    └── TooltipWindow.vm
```

**How Components Work:**
```velocity
## Dashboard.vm (Main Template)
#parse("app/admin/components/Header.vm")
#parse("app/admin/components/Sidebar.vm")

## SearchBox.vm (Reusable Component)
## Parameters: $searchId, $placeholder, $onSearchFunction
<div class="search-box">
    <input type="text" id="$!searchId" placeholder="$!placeholder" />
    <button onclick="$!onSearchFunction">Search</button>
</div>

## Usage in Dashboard.vm
#set ($searchId = "user-search")
#set ($placeholder = "Search users...")
#set ($onSearchFunction = "searchUsers()")
#parse("app/admin/components/SearchBox.vm")
```

---

### 7. **Database Access: Torque ORM + SecurityService**

**What it is:**
- **Torque ORM**: Object-Relational Mapping for database access
- **SecurityService**: Turbine's security service for user/role/permission management

**How it's used:**
```java
// In Dashboard.java
SecurityService security = getSecurityService();

// Load users using Torque ORM
Criteria criteria = new Criteria();
List<User> users = (List<User>) security.getUserManager().retrieveList(criteria);

// Load groups, roles, permissions
GroupSet groups = security.getAllGroups();
RoleSet roles = security.getAllRoles();
PermissionSet permissions = security.getAllPermissions();
```

---

## Complete Technology Stack Summary

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Backend Framework** | Apache Turbine 7 | MVC framework, request handling, security |
| **Template Engine** | Apache Velocity | Server-side rendering, HTML generation |
| **CSS Framework** | W3.CSS | Base styling, utility classes |
| **Custom CSS** | admin-dashboard.css | Custom styling for dashboard |
| **JavaScript** | Vanilla JS | Client-side interactions, search |
| **Component System** | Velocity `#parse` | Modular template components |
| **Database Access** | Torque ORM | Database operations |
| **Security** | Turbine SecurityService | Authentication, authorization |
| **Build Tool** | Maven | Dependency management, WAR packaging |
| **Application Server** | Apache Tomcat 10 | Servlet container, deployment |

---

## Request Flow (How It Works)

```
1. User Request
   ↓
2. Apache Turbine Framework
   - Routes request to Dashboard.java screen class
   ↓
3. Dashboard.java (Controller)
   - Checks authorization (isAuthorized())
   - Loads data from database (doBuildTemplate())
   - Puts data into Velocity Context
   ↓
4. Velocity Template Engine
   - Processes Dashboard.vm template
   - Includes components (#parse)
   - Renders HTML with data
   ↓
5. Response to Browser
   - HTML + CSS + JavaScript
   - Browser renders the page
   ↓
6. Client-Side JavaScript
   - Handles search, navigation
   - Updates UI dynamically
```

---

## Key Characteristics

### Server-Side Rendering (SSR)
- ✅ HTML is generated on the server
- ✅ Data is embedded in HTML during rendering
- ✅ No client-side API calls for initial load
- ✅ SEO-friendly

### Component-Based Architecture
- ✅ Reusable Velocity components
- ✅ Modular template structure
- ✅ Easy to maintain and update

### Traditional Web Application
- ✅ Page-based navigation
- ✅ Form submissions reload pages
- ✅ Server handles all business logic
- ✅ Simple, straightforward architecture

---

## Comparison with Modern Frameworks

| Aspect | Current (Turbine/Velocity) | Modern (React/Vue) |
|--------|---------------------------|-------------------|
| **Rendering** | Server-side | Client-side |
| **Navigation** | Page-based | SPA (Single Page App) |
| **Data Loading** | Embedded in HTML | API calls (AJAX) |
| **Interactivity** | Limited (JS) | Rich (Component state) |
| **Performance** | Page reloads | No reloads |
| **Complexity** | Low | Higher |

---

## Image 2 & 3 Specific Technologies

### Image 2 (Navigation Menu):
- **W3.CSS**: `w3-navbar`, `w3-theme`, `w3-dropdown-hover`
- **Velocity**: `#if` for role-based menu items
- **Turbine**: `$link.setPage()` for URL generation
- **Custom CSS**: Header styling

### Image 3 (User Details Table):
- **Velocity**: `#foreach` loop to display users
- **W3.CSS**: Table styling (or custom CSS)
- **JavaScript**: Search functionality
- **Torque ORM**: Data loaded from database
- **Velocity Components**: SearchBox.vm, ResultsGrid.vm

---

## Conclusion

**Images 2 & 3 are built using:**

1. **Apache Turbine 7** - Backend MVC framework
2. **Apache Velocity** - Template engine for server-side rendering
3. **W3.CSS** - CSS framework for styling
4. **Custom CSS/JavaScript** - Additional styling and interactivity
5. **Torque ORM** - Database access
6. **Component-based Velocity templates** - Modular architecture

This is a **traditional server-side rendered web application** using Java-based technologies, as opposed to modern Single Page Applications (SPA) using React, Vue, or Angular.
