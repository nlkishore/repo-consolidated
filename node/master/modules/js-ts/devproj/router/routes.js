
const express = require('express');
const router = express.Router();

router.get('/', (req, res) => {
  res.render('index', { title: 'Express Modular App', message: 'Welcome to your modular Express app!' });
});

module.exports = router;
