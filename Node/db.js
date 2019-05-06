var mongoose    = require('mongoose');

// [ CONFIGURE mongoose ]
var db = mongoose.connection;
db.on('error', console.error);
// mongodb와 connection이 진행되면 콜백.
db.once('open', function(){
    console.log("Connected to mongod server");
});
// 서버에 접속. 따로 설정할 파라미터가 존재한다면 아래와 같이 사용.
// mongoose.connect('mongodb://username:password@host:port/database?options...');
mongoose.connect('mongodb://localhost/mongodb_tutorial');
