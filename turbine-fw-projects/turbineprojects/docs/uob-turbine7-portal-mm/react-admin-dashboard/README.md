# React Admin Dashboard - Reference UI Style

A modern React Single Page Application (SPA) that replicates the reference UI design while displaying Admin Dashboard data (Users, Groups, Roles, Permissions).

## Features

- ✅ **Reference UI Design**: Orange dropdown navigation widget matching reference UI
- ✅ **SPA Architecture**: Single Page Application with React Router
- ✅ **Admin Dashboard Data**: Displays Users, Groups, Roles, and Permissions
- ✅ **Search Functionality**: Real-time search in each section
- ✅ **Responsive Design**: Works on different screen sizes
- ✅ **Modern React**: Uses React 18 with TypeScript
- ✅ **Component-Based**: Modular, reusable components

## Technology Stack

- **React 18** - UI library
- **TypeScript** - Type safety
- **Vite** - Build tool and dev server
- **Axios** - HTTP client for API calls
- **CSS Modules** - Component-scoped styling

## Project Structure

```
react-admin-dashboard/
├── src/
│   ├── components/
│   │   ├── Dashboard.tsx          # Main dashboard component
│   │   ├── Header.tsx             # Header component
│   │   ├── Sidebar.tsx            # Sidebar navigation
│   │   ├── FloatingDropdown.tsx   # Orange dropdown widget (Reference UI)
│   │   ├── WorkArea.tsx           # Main work area
│   │   ├── Footer.tsx             # Footer component
│   │   ├── SearchBox.tsx          # Reusable search component
│   │   ├── DataGrid.tsx           # Reusable data grid component
│   │   └── sections/              # Section components
│   │       ├── UserDetailsSection.tsx
│   │       ├── GroupDetailsSection.tsx
│   │       ├── RolesDetailsSection.tsx
│   │       └── PermissionDetailsSection.tsx
│   ├── services/
│   │   └── api.ts                 # API service (with mock data)
│   ├── types/
│   │   └── index.ts               # TypeScript type definitions
│   ├── App.tsx                    # Root component
│   ├── main.tsx                   # Entry point
│   └── index.css                  # Global styles
├── index.html                     # HTML template
├── package.json                   # Dependencies
├── tsconfig.json                  # TypeScript config
├── vite.config.ts                 # Vite config
└── README.md                      # This file
```

## Getting Started

### Prerequisites

- Node.js 18+ and npm/yarn

### Installation

```bash
cd react-admin-dashboard
npm install
```

### Development

```bash
npm run dev
```

The application will be available at `http://localhost:3000`

### Build

```bash
npm run build
```

The built files will be in the `dist` directory.

## API Integration

The application is integrated with the Spring REST API from `uob-turbine7-portal-mm` project.

### Backend Setup

1. **Deploy WAR**: Ensure the Turbine application WAR is deployed to Tomcat
2. **Start Tomcat**: The REST API will be available at `http://localhost:8080/api/*`
3. **CORS**: CORS is configured in `SpringConfig.java` to allow React app

### API Endpoints

The React app calls these endpoints:

- `GET /api/users` - Fetch all users
- `GET /api/groups` - Fetch all groups
- `GET /api/roles` - Fetch all roles
- `GET /api/permissions` - Fetch all permissions

### DTO Mapping

The API service automatically maps backend DTOs to React types:
- `userId` → `id`, `loginName` → `name` (User)
- `groupId` → `id`, `groupName` → `name` (Group)
- `roleId` → `id`, `roleName` → `name` (Role)
- `permissionId` → `id`, `permissionName` → `name` (Permission)

### Fallback Behavior

If API calls fail, the app falls back to mock data for development purposes.

## Key Components

### FloatingDropdown (Reference UI Style)
- Orange button (#FF9800) matching reference UI
- Dropdown menu with section navigation
- "Show/Hide Form Summary | Top" toggle
- Fixed position, stays visible while scrolling

### Sections
Each section (User, Group, Role, Permission) includes:
- Search functionality
- Data grid display
- Edit links

## Styling

- **Reference UI Colors**: Orange (#FF9800) for dropdown, matching reference design
- **Component-scoped CSS**: Each component has its own CSS file
- **Responsive**: Flexbox layout for responsive design

## Integration with Turbine Backend

To integrate with the existing Turbine application:

1. **Deploy React Build**: Copy `dist` folder contents to `webapp/src/main/webapp/react-dashboard/`
2. **Add Route**: Configure Turbine to serve React app at `/react-dashboard`
3. **API Endpoints**: Create REST API endpoints in Spring Boot module
4. **Authentication**: Integrate with Turbine's authentication system

## Development Notes

- Uses mock data when API is unavailable
- TypeScript for type safety
- Component-based architecture for maintainability
- CSS modules for scoped styling

## Future Enhancements

- [ ] Real-time updates via WebSocket
- [ ] Pagination for large datasets
- [ ] Advanced filtering options
- [ ] Export functionality
- [ ] Bulk operations
- [ ] User authentication integration
