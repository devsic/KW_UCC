var express = require('express');
var router = express.Router();
var bodyParser = require('body-parser');

router.use(bodyParser.urlencoded({ extended: true }));
router.use(bodyParser.json());
var User = require('./user');

 // GET ALL USERS from usertable.(user_id,beacon_id)
router.get('/users', function(req,res){
    // parameter를 안주면 전체를 조회.
    User.find(function(err, users){
        if(err) return res.status(500).send({error: 'database failure'});
        res.json(users);
    })
});
// 지워줄 것. 테스트용.
// 이렇게 보내면 중복된 값으로 계속 저장됨. 어차피 이렇게 쓰면 안됨. gps같은 경우 로깅하기 위해 쓴것.
router.post('/users', function(req, res){
    var user = new User();
    user.user_id = req.body.user_id;
    user.beacon_id = req.body.beacon_id;
    
    
    user.save(function(err){
        if(err){
            console.error(err);
            res.json({result: 0});
            return;
        }
        res.json({result: 1});

    });
});
// 지워줄 것. 테스트용.
router.delete('/users/:userid',function(req,res){
    User.remove({ user_id: req.params.userid }, function(err, user){
        if(err) return res.status(500).json({ error: "database failure" });

        res.status(204).end();
    });
});
module.exports = router;
