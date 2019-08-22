const Discord = require('discord.js');
const bot = new Discord.Client();
const config = require("./botconfig.json");
const fetch = require('node-fetch');

const cached = []

bot.on('ready', () => {
  console.log(`Logged in as ${bot.user.tag}!`);
  //interval to get clan msgs
    const chat = setInterval(async () => {
        fetch(config.server + "/chat", {
            method: "GET",
            headers: { 'Content-Type': 'application/json'}
        })
        .then(response => response.json())
        .then(res => {
            if(res.text === undefined) {
                if(res.stop) {
                    bot.channels.get(config.syncChannel).send(res.stop);
                }
                return;
            }
            let username = res.username;
            let text = res.text.replace(/&%'/g, '"');

            getUUID(username)
            .then(uuid => bot.channels.get(config.syncChannel).createWebhook(username, "https://mc-heads.net/avatar/"+ uuid)
                .then(h => {
                    h.send(text)
                    return h;
                })
                .then(h => h.delete()));
        })
        .catch(err => {});
    }, 50);
});

bot.on('message', msg => {
  if (!msg.author.bot) {
    //.verify
    if(msg.channel.id == config.verifyChannel) {
        if(!msg.toString().startsWith(".verify")) {
            msg.delete();
            return;
        }
        parts = msg.toString().split(" ");
        console.log(msg.author.username + " : " + parts[1]);

        let username = null
        fetch(config.server + "/authget", {
            method: "POST",
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                "token": parts[1]
            })
        })
        .then(response => response.json())
        .then(res => username = res.name)
        .then(function() {

            if(username != "" && username != null && username != undefined) {
                if(msg.guild.roles.exists("name", username)) {
                    msg.reply("Dieser Nutzername ist bereits verifiziert!");
                    return;
                }
                msg.guild.roles.delete(msg.member.roles.find(r => r.color === 37887));
                msg.guild.createRole({
                    name: username,
                    color: 37887
                }).then(async() => {
                    msg.member.addRole(msg.guild.roles.find(r => r.name === username));
                    msg.member.addRole(msg.guild.roles.find(r => r.name === "verified"));
                    msg.reply("Erfolgreich verifiziert!");
                });
            } else {
                msg.reply("Da ist was schiefgelaufen, vielleicht falscher token?")
            }
        })
        .catch(err => console.error(err));

        return;
    } else if(msg.channel.id == config.syncChannel) {
        //if not .verify
        if(!msg.member.roles.find(r => r.name === "verified")) {
            msg.reply("Du musst dich zuerst verifizieren!");
            return;
        }

        //get username
        let username;
        try {
            username = msg.member.roles.find(r => r.color === 37887).name;
        }catch(e) {
            console.error(e);
            return;
        }
        //get message
        let text = msg.toString();

        let specMaxLength = 100 - 3 - username.length - 2;

        if(text.length > specMaxLength) {
            msg.reply("Deine Nachricht darf nicht länger als " + specMaxLength + " Zeichen sein!");
            return;
        }

		console.log(username + ": " + text);
		
        // create webhook
        getUUID(username)
        .then(uuid => bot.channels.get(config.syncChannel).createWebhook(username, "https://mc-heads.net/avatar/"+ uuid)
            .then(h => {
                h.send(text)
                return h;
            })
            .then(h => h.delete()));
        
        fetch(config.server, {
            method: "POST",
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username: msg.member.roles.find(r => r.color === 37887).name,
                text: text.replace(/"/g, "&%'")
            })
        }).catch(err => msg.reply("Der API Server ist nicht erreichbar"));

        msg.delete();
    } else if(msg.channel.id == "612655037335994370" && msg.toString().startsWith(".ak")) {
        if(msg.member.id == "301702918376259585") {
            let u = msg.mentions.members.first();
            if(cached.includes(u)) {
                cached.pop(u);
            } else {
                cached.push(u);
            }
            msg.delete();
        }
    }
  }
});

bot.login(config.token);

function getUUID(name) {
    return fetch("https://mc-heads.net/minecraft/profile/" + name, {
        method: "GET",
    }).then(response => response.json())
    .then(res => res.id);
}

const checkStatus = setInterval(() => {
    cached.forEach(function(e) {
        if(e.voiceChannel) {
            e.setVoiceChannel(null)
            .catch();
        }
    });
}, 1000);