var express = require('express');
var router = express.Router();
var bodyParser = require('body-parser');

router.use(bodyParser.urlencoded({ extended: true }));
router.use(bodyParser.json());
var User = require('./user');

 // GET ALL USERS
router.get('/api/users', function(req,res){
    // parameter를 안주면 전체를 조회.
    User.find(function(err, users){
        if(err) return res.status(500).send({error: 'database failure'});
        res.json(users);
    })
});

// beacon_id로 user 조회.
router.get('/api/users/:beaconid', function(req, res){
    User.findOne({beacon_id: req.params.beaconid}, function(err, user){
        console.log("WEGWEF<JNWEFKLE")
        if(err) return res.status(500).json({error: err});
        if(!user) return res.status(404).json({error: 'user not found'});
        res.json(user);
    })
});
/*
// user_id로 user조회.
router.get('/api/users/:userid', function(req, res){
    User.findOne({user_id: req.params.userid}, function(err, user){
        console.log("HIHIHIHIUSER")
        if(err) return res.status(500).json({error: err});
        if(!user) return res.status(404).json({error: 'user not found'});
        res.json(user);
    })
});
*/
// CREATE USER
router.post('/api/users', function(req, res){
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

// UPDATE THE USER
// post로 받아온 data로 update.
router.put('/api/users/:userid', function(req, res){
    User.findOne({user_id: req.params.userid}, function(err, user){
        if(err) return res.status(500).json({ error: 'database failure' });
        if(!user) return res.status(404).json({ error: 'User not found' });
        
        if(req.body.user_id) user.user_id = req.body.user_id;
        if(req.body.beacon_id) user.beacon_id = req.body.beacon_id;

        user.save(function(err){
            if(err) res.status(500).json({error: 'failed to update'});
            res.json({message: 'user updated'});
        });

    });

});

module.exports = router;
