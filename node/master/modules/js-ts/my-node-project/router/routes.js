const express = require('express');
const router = express.Router();

router.get('/', (req, res) => {
    res.render('index'); // Renders view/index.ejs
});

module.exports = router;