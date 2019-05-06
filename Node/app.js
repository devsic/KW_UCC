var express = require('express');
var app = express();
var db = require('./db')

var UserController = require('./models/UserController');
var InfoController = require('./models/InfoController');
var Fcm = require('./models/fcm');
// router 등록
app.use(UserController);
app.use(InfoController);
app.use(Fcm);

module.exports = app;