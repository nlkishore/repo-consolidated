
const express = require('express');
const path = require('path');
const config = require('./Config/config');
const router = require('./router/routes');

const app = express();
app.set('views', path.join(__dirname, 'view'));
app.set('view engine', 'ejs');
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use('/', router);

const PORT = config.port;
app.listen(PORT, () => {
  console.log(`Server running on http://localhost:${PORT}`);
});
