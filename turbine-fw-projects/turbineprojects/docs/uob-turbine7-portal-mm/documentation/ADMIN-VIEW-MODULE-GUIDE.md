# Admin View Module - Complete Guide

## Architecture

The Admin Dashboard is now implemented as a **separate Maven submodule** with fully modularized view components.

## Module Structure

```
uob-turbine7-portal-mm/
├── pom.xml                          # Root POM (includes admin-view)
├── admin-view/                      # NEW: Separate view module
│   ├── pom.xml                      # Module POM
│   ├── src/main/webapp/
│   │   ├── components/              # Modularized components
│   │   │   ├── Header.vm
│   │   │   ├── Sidebar.vm
│   │   │   ├── SearchBox.vm
│   │   │   ├── ResultsGrid.vm
│   │   │   ├── TooltipWindow.vm
│   │   │   └── ContextualMenu.vm
│   │   ├── styles/
│   │   │   └── admin-dashboard.css
│   │   ├── scripts/
│   │   │   └── admin-dashboard.js
│   │   └── Dashboard.vm
│   └── README.md
├── turbine-model-controller/
│   └── src/main/java/modules/screens/admin/
│       └── AdminDashboard.java      # Screen class
└── webapp/
    └── pom.xml                      # Includes admin-view dependency
```

## Benefits

### 1. Modularization
- **Components**: Each UI element is a separate file
- **Styles**: All CSS in one organized file
- **Scripts**: JavaScript organized by functionality
- **Easy to Find**: Know exactly where each piece is

### 2. Maintainability
- **Isolated Changes**: Modify one component without affecting others
- **Clear Structure**: Easy to understand and navigate
- **Version Control**: Better git diffs (changes per component)

### 3. Debugging
- **Component-Level**: Debug individual components
- **Style Issues**: Check CSS file directly
- **Script Issues**: Check JavaScript file directly
- **No Mixing**: Styles/scripts don't interfere with main app

### 4. Reusability
- **SearchBox**: Can be reused in other screens
- **ResultsGrid**: Generic grid component
- **Header/Sidebar**: Reusable layout components

### 5. Separation
- **Different URL**: `/app/admin/Dashboard.vm`
- **Different Package**: `com.uob.modules.screens.admin`
- **Different Module**: `admin-view` Maven module
- **No Interference**: Completely isolated

## Component Details

### Header.vm
```velocity
## Simple header with title and user info
#parse("components/Header.vm")
```

### Sidebar.vm
```velocity
## Navigation sidebar
#parse("components/Sidebar.vm")
```

### SearchBox.vm
```velocity
## Reusable search component
#set ($searchId = "user-search")
#set ($placeholder = "Search users...")
#set ($onSearchFunction = "searchUsers()")
#parse("components/SearchBox.vm")
```

### ResultsGrid.vm
```velocity
## Reusable grid component
#set ($gridId = "user-grid")
#set ($headers = ["ID", "Name", "Email"])
#set ($tbodyId = "user-grid-body")
#parse("components/ResultsGrid.vm")
```

## Build Process

1. **Build Admin View Module**:
   ```bash
   cd admin-view
   mvn clean install
   ```

2. **Build Full Project**:
   ```bash
   cd ..
   mvn clean package
   ```

3. **Resources Copied**:
   - Templates → `webapp/target/.../WEB-INF/classes/META-INF/resources/admin/`
   - CSS → `/admin/styles/admin-dashboard.css`
   - JS → `/admin/scripts/admin-dashboard.js`

## Access Points

- **Dashboard**: `http://localhost:8080/uob-t7-portal-mm-tomcat/app/admin/Dashboard.vm`
- **CSS**: `http://localhost:8080/uob-t7-portal-mm-tomcat/admin/styles/admin-dashboard.css`
- **JS**: `http://localhost:8080/uob-t7-portal-mm-tomcat/admin/scripts/admin-dashboard.js`

## Development Workflow

### Adding a New Component

1. Create `admin-view/src/main/webapp/components/NewComponent.vm`
2. Add to `Dashboard.vm`:
   ```velocity
   #parse("components/NewComponent.vm")
   ```

### Modifying Styles

1. Edit `admin-view/src/main/webapp/styles/admin-dashboard.css`
2. Rebuild: `mvn clean package`
3. Refresh browser (clear cache if needed)

### Modifying Scripts

1. Edit `admin-view/src/main/webapp/scripts/admin-dashboard.js`
2. Rebuild: `mvn clean package`
3. Refresh browser

### Debugging a Component

1. **Check Component File**: `components/YourComponent.vm`
2. **Check Parameters**: Ensure variables are set before `#parse`
3. **Check CSS**: Look in `admin-dashboard.css`
4. **Check JS**: Look in `admin-dashboard.js`
5. **Browser Console**: Check for JavaScript errors

## Testing

1. **Build**: `mvn clean package`
2. **Deploy**: Copy WAR to Tomcat/JBoss
3. **Access**: `/app/admin/Dashboard.vm`
4. **Verify**: All components load correctly
5. **Test Features**: Search, navigation, tooltip, etc.

## Troubleshooting

### Component Not Rendering
- Check `#parse` statement in `Dashboard.vm`
- Verify component file exists
- Check Velocity syntax

### Styles Not Applied
- Verify CSS file is loaded: Check browser Network tab
- Check CSS path: `/admin/styles/admin-dashboard.css`
- Clear browser cache

### JavaScript Not Working
- Check browser console for errors
- Verify JS file is loaded: Check browser Network tab
- Check JS path: `/admin/scripts/admin-dashboard.js`

### Build Issues
- Ensure `admin-view` is in root `pom.xml` modules
- Ensure `admin-view` dependency in `webapp/pom.xml`
- Run `mvn clean install` from root

## Removing the Module

If you need to remove the admin view module:

1. Remove from root `pom.xml`:
   ```xml
   <!-- Remove this line -->
   <module>admin-view</module>
   ```

2. Remove from `webapp/pom.xml`:
   ```xml
   <!-- Remove this dependency -->
   <dependency>
       <groupId>com.uob</groupId>
       <artifactId>admin-view</artifactId>
   </dependency>
   ```

3. Delete `admin-view/` directory

4. Rebuild: `mvn clean package`

**Main application remains completely unaffected.**

## Best Practices

1. **Keep Components Small**: One component = one responsibility
2. **Use Parameters**: Pass data via Velocity variables
3. **Isolate Styles**: All admin styles in `admin-dashboard.css`
4. **Namespace JavaScript**: Use `Admin*` prefixes
5. **Document Components**: Add comments explaining parameters
6. **Test Independently**: Test each component separately

## Next Steps

1. Implement REST API endpoints for data loading
2. Complete AJAX functions in `admin-dashboard.js`
3. Add pagination to grids
4. Add sorting functionality
5. Add export features
6. Add filtering options
