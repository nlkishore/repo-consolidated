const express = require('express');
const router = express.Router();

router.get('/', (req, res) => {
  res.render('feedback', { title: 'Send Feedback' });
});

router.post('/', (req, res) => {
  // Placeholder logic
  res.send('Feedback received');
});

module.exports = router;