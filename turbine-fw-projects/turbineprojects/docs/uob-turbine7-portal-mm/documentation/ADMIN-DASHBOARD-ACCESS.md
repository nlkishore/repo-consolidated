# Admin Dashboard - How to Access

## Access the New Admin Dashboard UI

### For Admin Users

After logging in as an admin user, you will see a new menu item:

**"✨ Admin Dashboard (New UI)"** in the main navigation bar

Click this link to access the new Admin Dashboard with:
- Modern sidebar navigation
- New look and feel
- Search functionality
- Grid results
- Tooltip window with slider

### Direct URL Access

You can also access directly via URL:

```
http://localhost:8080/uob-t7-portal-mm-tomcat/app/admin/Dashboard.vm
```

### Features of New UI

1. **Header**: Blue header with "Admin Dashboard" title
2. **Side Navigation**: Left sidebar with section links
3. **Work Area**: Main content area with 4 sections:
   - User Details
   - Group Details
   - Roles Details
   - Permission Details
4. **Search**: Each section has a search box
5. **Grid Results**: Tabular display of search results
6. **Tooltip Window**: Floating navigation (press Ctrl+T to toggle)
7. **Contextual Menu**: Jump-to menu in User Details section

### If You Don't See the Link

1. **Check User Role**: Make sure you're logged in as an admin user
2. **Rebuild**: Run `mvn clean package` to rebuild
3. **Redeploy**: Copy new WAR to Tomcat/JBoss
4. **Clear Cache**: Clear browser cache and refresh

### Troubleshooting

**Issue**: Link not visible in menu
- **Solution**: Verify user has admin role
- Check: `Menu.vm` has `#if ( $isAdmin )` check

**Issue**: Page shows old UI
- **Solution**: Verify CSS is loading
- Check browser console for CSS errors
- Verify: `/admin/styles/admin-dashboard.css` is accessible

**Issue**: Sidebar not showing
- **Solution**: Check CSS file is loaded
- Verify: Browser Network tab shows CSS file loaded
- Check: CSS path is correct

### Visual Differences

**Old UI**:
- Uses W3.CSS theme
- Standard navigation
- Traditional layout

**New UI**:
- Custom blue header
- Left sidebar navigation
- Modern card-based sections
- Floating tooltip window
- Smooth scrolling

### Next Steps

1. Click "✨ Admin Dashboard (New UI)" link
2. Explore the new interface
3. Test search functionality
4. Try the tooltip window (Ctrl+T)
5. Use contextual menu for quick navigation
