# Admin View Module - Build Integration

## How It Works

The `admin-view` module is a **separate Maven submodule** that contains all view layer components. During the build process, these components are integrated into the main `webapp` module.

## Build Process

### Option 1: Automatic Copy (Current Implementation)

During build, components are copied to:
```
webapp/src/main/webapp/templates/app/admin/
├── Dashboard.vm
├── components/
│   ├── Header.vm
│   ├── Sidebar.vm
│   ├── SearchBox.vm
│   ├── ResultsGrid.vm
│   ├── TooltipWindow.vm
│   └── ContextualMenu.vm
├── styles/
│   └── admin-dashboard.css
└── scripts/
    └── admin-dashboard.js
```

### Option 2: Maven Resources Plugin (Recommended)

The `admin-view` module's `pom.xml` is configured to copy resources during build. However, for Turbine templates to work, they need to be in the `webapp` module's template directory.

## Current Setup

1. **Source**: `admin-view/src/main/webapp/`
2. **Target**: `webapp/src/main/webapp/templates/app/admin/`
3. **Manual Copy**: Currently using manual copy (can be automated)

## Automation Script

Create a build script to automatically copy files:

```powershell
# copy-admin-components.ps1
$source = "admin-view\src\main\webapp"
$target = "webapp\src\main\webapp\templates\app\admin"

Copy-Item "$source\components\*" "$target\components\" -Recurse -Force
Copy-Item "$source\styles\*" "$target\styles\" -Recurse -Force
Copy-Item "$source\scripts\*" "$target\scripts\" -Recurse -Force
Copy-Item "$source\Dashboard.vm" "$target\Dashboard.vm" -Force
```

## Maven Integration

Add to root `pom.xml` or `webapp/pom.xml`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-resources-plugin</artifactId>
    <executions>
        <execution>
            <id>copy-admin-view</id>
            <phase>process-resources</phase>
            <goals>
                <goal>copy-resources</goal>
            </goals>
            <configuration>
                <outputDirectory>${project.basedir}/webapp/src/main/webapp/templates/app/admin</outputDirectory>
                <resources>
                    <resource>
                        <directory>${project.basedir}/admin-view/src/main/webapp</directory>
                    </resource>
                </resources>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Development Workflow

1. **Edit Components**: Modify files in `admin-view/src/main/webapp/`
2. **Copy to Webapp**: Run copy script or Maven build
3. **Build**: `mvn clean package`
4. **Test**: Access `/app/admin/Dashboard.vm`

## Benefits

- **Source of Truth**: `admin-view/` is the source
- **Modular**: Components are organized
- **Maintainable**: Easy to find and modify
- **Isolated**: Doesn't interfere with main app
