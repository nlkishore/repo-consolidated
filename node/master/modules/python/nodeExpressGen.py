import os
import sys

CONFIG_TEMPLATE = '''[app]
appName = {app_name}
port = 3000
'''

ROUTER_TEMPLATE = '''const express = require('express');
const router = express.Router();

router.get('/', (req, res) => {
  res.render('{view_file}', {{{{ title: '{app_name}', message: 'Welcome to {app_name}!' }}}});
});

module.exports = router;
'''

VIEW_TEMPLATE = '''<!DOCTYPE html>
<html>
<head>
  <title><%= title %></title>
</head>
<body>
  <h1><%= message %></h1>
</body>
</html>
'''

def create_node_express_app(app_name, base_dir):
    config_dir = os.path.join(base_dir, 'Config')
    router_dir = os.path.join(base_dir, 'router')
    view_dir = os.path.join(base_dir, 'view')
    os.makedirs(config_dir, exist_ok=True)
    os.makedirs(router_dir, exist_ok=True)
    os.makedirs(view_dir, exist_ok=True)

    # Create config file
    config_path = os.path.join(config_dir, f'{app_name}-config.ini')
    with open(config_path, 'w') as f:
        f.write(CONFIG_TEMPLATE.format(app_name=app_name))

    # Create router file
    router_path = os.path.join(router_dir, f'{app_name}.js')
    with open(router_path, 'w') as f:
        f.write(ROUTER_TEMPLATE.format(app_name=app_name, view_file=f'{{app_name}}', router_file=f'{{app_name}}.js'))

    # Create view file
    view_path = os.path.join(view_dir, f'{app_name}.ejs')
    with open(view_path, 'w') as f:
        f.write(VIEW_TEMPLATE)

    print(f"Node+Express template created for '{app_name}' in {base_dir}")

    # Append route entry to app.js
    app_js_path = os.path.join(base_dir, 'app.js')
    if os.path.exists(app_js_path):
        with open(app_js_path, 'r') as f:
            lines = f.readlines()
        # Add require and use statements if not present
        require_line = f"const {app_name}Router = require('./router/{app_name}');\n"
        use_line = f"app.use('/{app_name}', {app_name}Router);\n"
        inserted_require = False
        inserted_use = False
        new_lines = []
        for line in lines:
            if not inserted_require and line.strip().startswith('const') and 'express' in line:
                new_lines.append(line)
                new_lines.append(require_line)
                inserted_require = True
            elif not inserted_use and line.strip().startswith('app.use('):
                new_lines.append(use_line)
                new_lines.append(line)
                inserted_use = True
            else:
                new_lines.append(line)
        # If not inserted, append at the end
        if not inserted_require:
            new_lines.insert(1, require_line)
        if not inserted_use:
            # Find last app.use or before app.listen
            for i, l in enumerate(new_lines):
                if 'app.listen' in l:
                    new_lines.insert(i, use_line)
                    break
            else:
                new_lines.append(use_line)
        with open(app_js_path, 'w') as f:
            f.writelines(new_lines)
        print(f"Route entry for '/{app_name}' appended to app.js.")
    else:
        print(f"app.js not found in {base_dir}. Please add the route manually.")

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python nodeExpressGen.py <application_name> <base_directory>")
        sys.exit(1)
    app_name = sys.argv[1]
    base_dir = sys.argv[2]
    create_node_express_app(app_name, base_dir)
