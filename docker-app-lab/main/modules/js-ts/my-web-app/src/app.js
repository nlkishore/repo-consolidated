const express = require('express');
const path = require('path');

const indexRoutes = require('./routes/index');
const userRoutes = require('./routes/user');
const feedbackRoutes = require('./routes/feedback');
const app = express();
require('./routes/loader')(app);



app.set('view engine', 'ejs');
app.set('views', path.join(__dirname, 'views'));
app.use(express.urlencoded({ extended: true }));
app.use(express.static('public'));

/*app.use('/', indexRoutes);
app.use('/user', userRoutes);
app.use('/feedback', feedbackRoutes);*/

const port = process.env.PORT || 3000;
app.listen(port, () => {
  console.log(`🚀 App running at http://localhost:${port}`);
});