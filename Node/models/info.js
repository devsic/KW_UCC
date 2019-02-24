var mongoose = require('mongoose');
var Schema = mongoose.Schema;
// Schema는 document(tuple)의 구조를 보여줌.
// document가 모여서 group(table)
var infoSchema = new Schema({
    user_id: String,
    gps: String,
    timestamp : Date
});

// module.exports를 사용하여 해당 model을 반환.
module.exports = mongoose.model('info', infoSchema);