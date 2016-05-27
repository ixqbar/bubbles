# NoticePushService


## protocol

### websocket-request
```
{
  "s":["player.login", "uuid", "token=md5(uuid+tokenKey)"], //请求体
  "i":1 //请求序列编号
}
```

### http-request
```
post http://localhost:8080/?token=md5(postData+tokenKey)
post data 
{
  "s":["notice.push", "uuid", "push-notice-data"], //请求体
  "i":1 //请求序列编号
}

{
  "s":["notice.pushAll", "push-notice-data"], //请求体
  "i":1 //请求序列编号
}
```

### response
```
{
  "n":true or false, //is notice response
  "c":0, //response code 0 is success
  "m":msg,//error message
  "d":data, //response data
  "i":i  //对应请求序列编号 notice时为第n次推送
}
```

## server-usage
```
mvn assembly:assembly -Dmaven.test.skip=true
cd target
tar zxvf NoticePush-0.0.1-SNAPSHOT-bin.tar.gz
cd NoticePush-0.0.1-SNAPSHOT/bin
./startup.sh
```

## client-usage
```
var websocket = new WebSocket('ws://localhost:8080/websocket');
websocket.onopen = function (evt) {
    console.log("Connected to WebSocket server.");
    var data = JSON.stringify({
        s : ["player.login", "123-456", "cea19f25fcc226ea4acc27b5a8aeb592"],
        i : 1
    });
    websocket.send(data);
};

websocket.onclose = function (evt) {
    console.log("Disconnected");
    websocket = null;
};

websocket.onmessage = function (evt) {
    console.log('Retrieved data from server: ' + evt.data);
};

websocket.onerror = function (evt, e) {
    console.log('Error occured: ' + evt.data);
};
```


## more usage
* @TODO

