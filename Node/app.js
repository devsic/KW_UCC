var express = require('express');
var app = express();
var db = require('./db')

var UserController = require('./models/UserController');
// router 등록
app.use(UserController);

module.exports = app;