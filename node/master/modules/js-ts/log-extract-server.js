const express = require('express');
const bodyParser = require('body-parser');
const { exec } = require('child_process');
const app = express();

app.use(bodyParser.json());

// POST /extract-log
app.post('/extract-log', (req, res) => {
  const { country, server, username, password, logPath, startDate, startTime, durationMinutes } = req.body;
  if (!server || !username || !password || !logPath || !startDate || !startTime || !durationMinutes) {
    return res.status(400).json({ error: 'Missing required fields' });
  }
  const startDateTime = `${startDate} ${startTime}`;
  const start = new Date(startDateTime);
  if (isNaN(start.getTime())) {
    return res.status(400).json({ error: 'Invalid start date/time' });
  }
  const end = new Date(start.getTime() + durationMinutes * 60000);
  const startStr = start.toISOString().replace('T', ' ').substring(0, 19);
  const endStr = end.toISOString().replace('T', ' ').substring(0, 19);

  // Call the Python script
  const pyCmd = `python3 extract_log.py ${server} ${username} ${password} ${logPath} "${startStr}" "${endStr}"`;
  exec(pyCmd, { cwd: __dirname }, (error, stdout, stderr) => {
    if (error) {
      return res.status(500).json({ error: stderr || error.message });
    }
    res.json({ logs: stdout });
  });
});

app.listen(4000, () => {
  console.log('Node+Express log extraction API running on port 4000');
});
