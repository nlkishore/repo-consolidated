# Admin View Module

## Overview

This is a **separate Maven submodule** for the Admin Dashboard view layer. It provides a modularized, maintainable structure for the admin interface components.

## Module Structure

```
admin-view/
├── pom.xml                          # Maven module definition
├── src/
│   └── main/
│       ├── java/                    # Java classes (if needed)
│       ├── resources/               # Resources
│       └── webapp/                  # View layer
│           ├── components/          # Modularized Velocity components
│           │   ├── Header.vm
│           │   ├── Sidebar.vm
│           │   ├── SearchBox.vm
│           │   ├── ResultsGrid.vm
│           │   ├── WorkAreaSection.vm
│           │   ├── TooltipWindow.vm
│           │   └── ContextualMenu.vm
│           ├── styles/              # CSS files
│           │   └── admin-dashboard.css
│           ├── scripts/             # JavaScript files
│           │   └── admin-dashboard.js
│           └── Dashboard.vm         # Main dashboard template
└── README.md
```

## Benefits of Modularization

1. **Easy Debugging**: Each component is in its own file
2. **Maintainability**: Changes to one component don't affect others
3. **Reusability**: Components can be reused across different screens
4. **Separation of Concerns**: CSS, JS, and templates are separated
5. **Independent Development**: Can be developed and tested separately
6. **No Interference**: Completely isolated from main application

## Components

### 1. Header.vm
- Displays dashboard title and user welcome message
- Reusable header component

### 2. Sidebar.vm
- Navigation sidebar with section links
- Handles active state highlighting

### 3. SearchBox.vm
- Reusable search input component
- Parameters: `$searchId`, `$placeholder`, `$onSearchFunction`

### 4. ResultsGrid.vm
- Reusable data grid component
- Parameters: `$gridId`, `$headers`, `$tbodyId`

### 5. TooltipWindow.vm
- Floating navigation window with slider
- Position control within visible workarea

### 6. ContextualMenu.vm
- Jump-to menu for quick navigation
- Positioned in User Details section

## Styles

### admin-dashboard.css
- All admin dashboard styles in one file
- Isolated from main application CSS
- Prefixed with `.admin-*` to avoid conflicts

## Scripts

### admin-dashboard.js
- Modularized JavaScript with namespaces:
  - `AdminTooltip` - Tooltip window management
  - `AdminNavigation` - Navigation functions
  - `AdminSearch` - Search functionality
  - `AdminDataLoader` - AJAX data loading

## Integration with Webapp Module

The admin-view module is included as a dependency in the webapp module. During build:

1. Resources are copied to `META-INF/resources/admin/`
2. Templates are available at `/admin/*`
3. CSS is available at `/admin/styles/*`
4. JS is available at `/admin/scripts/*`

## Access URL

- **Admin Dashboard**: `/app/admin/Dashboard.vm`
- **CSS**: `/admin/styles/admin-dashboard.css`
- **JS**: `/admin/scripts/admin-dashboard.js`

## Development Workflow

1. **Edit Components**: Modify individual `.vm` files in `components/`
2. **Update Styles**: Edit `styles/admin-dashboard.css`
3. **Update Scripts**: Edit `scripts/admin-dashboard.js`
4. **Build**: `mvn clean package` (from root)
5. **Test**: Access `/app/admin/Dashboard.vm`

## Debugging Tips

1. **Component Issues**: Check individual component files
2. **Style Issues**: Check `admin-dashboard.css`
3. **Script Issues**: Check browser console and `admin-dashboard.js`
4. **Template Issues**: Check `Dashboard.vm` and component includes

## Adding New Components

1. Create new `.vm` file in `components/`
2. Use `#parse("components/YourComponent.vm")` in `Dashboard.vm`
3. Pass parameters via Velocity variables

## Removing Module

To remove this module:

1. Remove from root `pom.xml` modules list
2. Remove dependency from `webapp/pom.xml`
3. Delete `admin-view/` directory
4. Rebuild project

**No other files need to be modified** - complete isolation.
