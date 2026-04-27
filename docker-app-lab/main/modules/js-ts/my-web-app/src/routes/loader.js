// src/routes/loader.js
const fs = require('fs');
const path = require('path');

module.exports = function (app) {
  const routesDir = __dirname;

  fs.readdirSync(routesDir).forEach((file) => {
    if (file === 'loader.js') return;

    const routeModule = require(path.join(routesDir, file));
    const routeBase = file === 'index.js' ? '/' : `/${path.basename(file, '.js')}`;
    app.use(routeBase, routeModule);
  });
};