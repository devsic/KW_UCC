var express = require('express');
var router = express.Router();
var bodyParser = require('body-parser');

router.use(bodyParser.urlencoded({ extended: true }));
router.use(bodyParser.json());

var Info = require('./info');

// gps data 전체 조회.
router.get('/infos', function(req,res){
    // parameter를 안주면 전체를 조회.
    Info.find(function(err, infos){
        if(err) return res.status(500).send({error: 'database failure'});
        res.json(infos);
    })
});
// user_id에 해당하는 gps와 timestamp를 계속하여 로깅.
// 전송 받는 data는 user_id(token) + gps + timestamp
// 이를 가지고 server에 그냥 save. userid 중복 상관 없음.
router.post('/infos/gps', function(req, res){
    var info = new Info();
    info.user_id = req.body.user_id;
    info.gps = req.body.gps;
    //info.timestamp = req.body.timestamp;
    
    info.save(function(err){
        if(err){
            console.error(err);
            res.json({result: 0});
            return;
        }
        res.json({result: 1});

    });
});

// 해당 user의 gps, timestamp 전부를 json으로 return해줌.
// android에서 차량 주행 기록을 띄우기 위한것.
// android에서는 여기서 Gps 파싱해서 쓰면 될 듯.
router.get('/infos/gps/:userid',function(req,res){
    Info.find({user_id: req.params.userid}, function(err, info){
        if(err) return res.status(500).json({error: err});
        if(!info) return res.status(404).json({error: 'user not found in Info collections.'});
        res.json(info);
    })
});

// 해당 User의 가장 최근 gps만 return해줌. // 사실 이건 fcm에서 쓰일 듯. fcm모듈로 바꿔주어야 할 듯.
// find후 전체 데이터를 역순으로 sort. 그 후 findOne을 통해 가장 최근 data만 return.(시간으로 비교하기는 변수가 많을듯. 따라서 가장 마지막에 들어간 data로 해주어야 할 듯(Ex,밤 -> 새벽))
// 쿼리면에서 더 좋은 방법이 있을 듯 찾아보기.
router.get('/infos/gps/:userid/recent',function(req,res){
    
    // 각 query마다 callback을 달아 진행한다면 비동기이기 때문에 결과가 이상해짐.
    // 따라서 sequence하게 쿼리를 처리 후 exec를 해주면 됨.
    Info.find({user_id: req.params.userid}).sort({"_id": -1}).findOne().exec(function(err,output){
        if(err) return res.status(500).json({error: err});
        if(!output) return res.status(404).json({error: 'user not found in Info collections.'});
        //console.log(output.user_id +" "+output.gps+" "+output.timestamp);
        console.log(output.user_id +" "+output.gps);
        res.json(output);
    });
});
/*
// userid에 해당하는 모든 데이터를 지움. 만약 앱이 종료될 때 저장된 gps값을 모두 지우려면 이렇게 구현하면 될 듯.
router.delete('/infos/gps/:userid',function(req,res){
    Info.remove({ user_id: req.params.userid }, function(err, info){
        if(err) return res.status(500).json({ error: "database failure" });

        res.status(204).end();
    });
});
*/

module.exports = router;