var mongoose = require('mongoose');
var Schema = mongoose.Schema;
// Schema는 document(tuple)의 구조를 보여줌.
// document가 모여서 group(table)
var userSchema = new Schema({
    user_id: String,
    beacon_id: String
});

// 위에서 UserSchema를 만들어놓고 require 하는 것.
// module.exports를 사용하여 해당 model을 반환.
module.exports = mongoose.model('user', userSchema);