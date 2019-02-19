var FCM = require('fcm-node');
 
/** Firebase(구글 개발자 사이트)에서 발급받은 서버키 */
// 가급적 이 값은 별도의 설정파일로 분리하는 것이 좋다.
var serverKey = 'AAAARdoOvjo:APA91bGEVmkAcYdxdW1LIiGckh7CXuXd53OR6r3fGI9Y7lUMzuhEvlFWzquGLl8B8rFqh1kJZp7XeO8llWkRnnBFONJO8VRpvj1b4_HJfdOVp6iYvirQnamb_tQ87jgqbEp4wg1fNtq_';
 
/** 안드로이드 단말에서 추출한 token값 */
// 안드로이드 App이 적절한 구현절차를 통해서 생성해야 하는 값이다.
// 안드로이드 단말에서 Node server로 POST방식 전송 후,
// Node서버는 이 값을 DB에 보관하고 있으면 된다.
var client_token = 'djEWP3S58xE:APA91bG5j_lwW2h3NjCOr2N83F25YYzJFo8KuHMXiUu7_kVJG_DwiU9yhq3kCfxnncqpizQCsIKuK1RI-KGb68rck-JhUA5iRGBXlwy3Z89rGQWZ1SC5l4UYqOx4SaJmMuVE5WI5m9zN';
 
/** 발송할 Push 메시지 내용 */
var push_data = {
    to: "dXOw3cT5AzA:APA91bHruCOIY7dVYlFST80PA5M4EQvYgiJAyBb62CVWvPcVu4F8ZNCG5xSueZHsmJgAoxHu8pnps9MQensg4ByLresmWO2jdPrbselQt9gSks5rc0E36kTHgOC2gG9P6aTjo1Kbve7i",
    data: {
        key1: 'Node에서 보내는 메시지'
    },
    notification: {
        // background에서 돌아갈 때 보여줄 title 및 msg
        title: 'Title of your push notification',
        msg: 'Body of your push notification'
    }
};
// push message 발송절차
var fcm = new FCM(serverKey);
 
fcm.send(push_data, function(err, response) {
    if (err) {
        console.error('Push메시지 발송에 실패했습니다.');
        console.error(err);
        return;
    }
 
    console.log('Push메시지가 발송되었습니다.');
    console.log(response);
});