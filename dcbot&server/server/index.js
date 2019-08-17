var express = require('express');
var app = express();

let msgs = [];
let online = false;

app.use(express.json())

// GET method route
app.get('/', function (req, res) {
    if(msgs.length > 0) {
        res.send(msgs.pop());
		res.end();
    }
    res.statusCode = 400;
    res.end();
});
  
// POST method route
/*
*   { "username": username, "text": message text}
*/
app.post('/', function (req, res) {
    msgs.push(req.body);
    res.statusCode = 200;
    res.end();
});


let auth = {};

//post method verifying get 
app.post('/authget', function(req, res){
    res.send(JSON.stringify(auth[req.body.token]));
    delete auth[req.body.token];
});

//post method verify
/*
*   { "token": token, "username": username}
*/
app.post('/auth', function(req, res) {
    auth[req.body.token] = req.body.username;
    res.statusCode = 200;
    res.end();
});


//bot first connection
app.post('/start', function(req, res) {
   msgs = [];
   online = true;
});

app.get('/start', function(req, res){
    res.send(online);
});

app.listen(61111, function() {
    console.log("listening on port 61111");
});
