var FCM = require('fcm-node');
var express = require('express');
var router = express.Router();
var bodyParser = require('body-parser');

router.use(bodyParser.urlencoded({ extended: true }));
router.use(bodyParser.json());

var User = require('./user');
var Info = require('./info');
var serverKey = 'AAAARdoOvjo:APA91bGEVmkAcYdxdW1LIiGckh7CXuXd53OR6r3fGI9Y7lUMzuhEvlFWzquGLl8B8rFqh1kJZp7XeO8llWkRnnBFONJO8VRpvj1b4_HJfdOVp6iYvirQnamb_tQ87jgqbEp4wg1fNtq_';
var fcm = new FCM(serverKey);

router.post('/fcm',function(req,res){
    var beaconid = req.body.beacon_id;
    var my_token_id = req.body.user_id;
    var score = req.body.score;
    var gps_mine;
    // beacond id로 target의 id를 select
    User.find({beacon_id: beaconid}, function(err, output){
        if(err) return res.status(500).json({error: err});
        if(!output) return res.status(404).json({error: 'user not found in User collections.'});
        
        console.log("output user id :"+output[0].user_id + " beacon: " +output[0].beacon_id + " my_token_id: "+my_token_id );
        target_token_id = output[0].user_id;
        //res.json(output);
    });
    // 내 token_id로 저장된 Gps를 select
    Info.find({user_id : my_token_id}).sort({"_id": -1}).findOne().exec(function(err,output){
        if(err) console.log("errerrererererererererererererererer");
        if(!output) console.log("output isn't exist.");
        console.log("output.gps: " + output.gps);
        gps_mine = output.gps;
        //res.json(output);

        var push_data = {
            to: target_token_id, // target 폰.
            data: {
                gps: gps_mine,//'내 gps',
                score: score//'내 운전 점수'
            },
            notification: {
                // background에서 돌아갈 때 보여줄 title 및 msg
                title: 'Example of Push Notification',
                msg: 'Body of your push notification'
            }
        };

        fcm.send(push_data, function(err, output) {
            if (err) {
                //console.error('Push메시지 발송에 실패했습니다.');
                //console.error(err4);
                console.log("fcm error: "+response);
                console.log("beacon id : "+beaconid);
                console.log("my_token_id: "+my_token_id);
                console.log("target_token : "+target_token_id);
                console.log("GPS : "+gps_mine);
                res.end();
                //res.json({result: 0});
                //return err;
                
            }
            console.log("target_token : "+target_token_id);
            console.log('Push메시지가 발송되었습니다.');
            console.log(output);
            //return output;
            res.end();
            //res.json({result: 1});
        });
    });
    
});
module.exports = router;// 등록 꼭 해줄것.