var app = require('./app');
var port = process.env.PORT || 8080;

var server = app.listen(port, function(){
    console.log("Express server has started on port " + port)
});