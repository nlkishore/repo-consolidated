# Admin Dashboard - Angular SPA Option

## Overview

This document describes the Angular Single Page Application (SPA) approach for the Admin Dashboard, as an alternative to the Turbine 7 Velocity template approach.

## Architecture

### Option 1: Turbine 7 Compatible (Recommended for Production)
- **Location**: `webapp/src/main/webapp/templates/app/screens/AdminDashboard.vm`
- **Screen Class**: `modules/screens/AdminDashboard.java`
- **Pros**: 
  - Fully integrated with Turbine 7 security
  - Uses existing Turbine services
  - No additional build process
  - Server-side rendering support
- **Cons**: 
  - Less modern UI framework
  - Limited client-side interactivity

### Option 2: Angular SPA (Modern Approach)
- **Location**: `webapp/src/main/webapp/angular-admin/`
- **Integration**: Served as static files, API calls to Turbine backend
- **Pros**:
  - Modern, responsive UI
  - Rich client-side interactions
  - Better user experience
  - Component-based architecture
- **Cons**:
  - Requires separate build process
  - More complex deployment
  - Need to create REST API endpoints

## Angular SPA Structure

```
angular-admin/
├── src/
│   ├── app/
│   │   ├── components/
│   │   │   ├── header/
│   │   │   ├── sidebar/
│   │   │   ├── workarea/
│   │   │   ├── user-details/
│   │   │   ├── group-details/
│   │   │   ├── roles-details/
│   │   │   ├── permission-details/
│   │   │   └── tooltip-window/
│   │   ├── services/
│   │   │   ├── user.service.ts
│   │   │   ├── group.service.ts
│   │   │   ├── role.service.ts
│   │   │   └── permission.service.ts
│   │   ├── models/
│   │   │   ├── user.model.ts
│   │   │   ├── group.model.ts
│   │   │   ├── role.model.ts
│   │   │   └── permission.model.ts
│   │   ├── app.component.ts
│   │   ├── app.module.ts
│   │   └── app-routing.module.ts
│   ├── assets/
│   ├── index.html
│   └── main.ts
├── package.json
├── angular.json
└── tsconfig.json
```

## Implementation Steps

### 1. Create Angular Project Structure

```bash
cd C:\Turbineprojects\uob-turbine7-portal-mm\webapp\src\main\webapp
ng new angular-admin --routing --style=css
cd angular-admin
```

### 2. Install Dependencies

```bash
npm install @angular/material @angular/cdk
npm install @angular/animations
```

### 3. Create Components

```bash
ng generate component components/header
ng generate component components/sidebar
ng generate component components/workarea
ng generate component components/user-details
ng generate component components/group-details
ng generate component components/roles-details
ng generate component components/permission-details
ng generate component components/tooltip-window
```

### 4. Create Services

```bash
ng generate service services/user
ng generate service services/group
ng generate service services/role
ng generate service services/permission
```

## Key Features Implementation

### Tooltip Window with Slider

```typescript
// tooltip-window.component.ts
export class TooltipWindowComponent {
  position: number = 50;
  visible: boolean = false;
  
  updatePosition(value: number) {
    this.position = value;
    // Calculate position within visible workarea
    const workarea = document.getElementById('workarea');
    const workareaRect = workarea.getBoundingClientRect();
    const tooltip = document.getElementById('tooltip-window');
    
    const maxTop = workareaRect.top;
    const maxBottom = workareaRect.bottom - tooltip.offsetHeight;
    const range = maxBottom - maxTop;
    const topPosition = maxTop + (range * (value / 100));
    
    tooltip.style.top = Math.max(maxTop, Math.min(topPosition, maxBottom)) + 'px';
  }
}
```

### Search with Grid Results

```typescript
// user-details.component.ts
export class UserDetailsComponent {
  searchTerm: string = '';
  users: User[] = [];
  
  search() {
    this.userService.search(this.searchTerm).subscribe(
      users => this.users = users,
      error => console.error('Search failed', error)
    );
  }
}
```

### REST API Endpoints Needed

Create Spring REST controllers (already exists in `spring-rest-api` module):

```java
@RestController
@RequestMapping("/api/admin")
public class AdminRestController {
    
    @GetMapping("/users")
    public List<User> searchUsers(@RequestParam String search) {
        // Implementation
    }
    
    @GetMapping("/groups")
    public List<Group> searchGroups(@RequestParam String search) {
        // Implementation
    }
    
    // Similar for roles and permissions
}
```

## Integration with Turbine 7

### Option A: Serve Angular as Static Files

1. Build Angular app: `ng build --prod`
2. Copy `dist/angular-admin/*` to `webapp/src/main/webapp/angular-admin/`
3. Access via: `http://localhost:8080/uob-t7-portal-mm-tomcat/angular-admin/`

### Option B: Integrate with Turbine Routing

1. Create Turbine screen that serves Angular index.html
2. Angular handles all routing client-side
3. API calls go to `/api/admin/*` endpoints

## Recommendation

**For Production Grade Application:**

1. **Start with Turbine 7 Compatible Version** (already created)
   - Fully integrated
   - Uses existing security
   - No additional build process
   - Easier to maintain

2. **Enhance with AJAX** for dynamic loading
   - Add REST endpoints in existing `spring-rest-api` module
   - Use JavaScript fetch/XMLHttpRequest for data loading
   - Keep Turbine security integration

3. **Consider Angular SPA Later** if needed
   - For more complex interactions
   - When team has Angular expertise
   - For better mobile experience

## Next Steps

1. Implement AJAX data loading in `AdminDashboard.vm`
2. Create REST API endpoints in `spring-rest-api` module
3. Add search functionality with backend filtering
4. Implement grid pagination and sorting
5. Add tooltip window positioning logic
6. Test with production data
