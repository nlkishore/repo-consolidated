const express = require('express');
const path = require('path');
const routes = require('./router/routes');

const app = express();
//const PORT = process.env.PORT || 3001;
const PORT = 3001; // Set a fixed port for simplicity


// Middleware
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.set('view engine', 'ejs');
app.set('views', path.join(__dirname, 'view'));

// Routes
app.use('/', routes);

// Start the server
app.listen(PORT, () => {
    console.log(`Server is running on http://localhost:${PORT}`);
});