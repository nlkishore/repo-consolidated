const express = require('express');
const path = require('path');
const app = express();

// Set EJS as view engine and set views directory
app.set('views', path.join(__dirname, 'view'));
app.set('view engine', 'ejs');

// Serve static files (for style.css)
app.use(express.static(path.join(__dirname, 'view')));

// Render the layout with workarea as body
app.get('/', (req, res) => {
  res.render('layout', {
    title: 'EJS Layout Example',
    body: '<%- include("workarea") %>'
  });
});

// Optionally, direct route for workarea only
app.get('/workarea', (req, res) => {
  res.render('workarea', { title: 'Work Area' });
});

const PORT = 3002;
app.listen(PORT, () => {
  console.log(`App running at http://localhost:${PORT}`);
});
