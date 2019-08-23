const Discord = require('discord.js');
const bot = new Discord.Client();
const config = require("./botconfig.json");
const fetch = require('node-fetch');

const cached = []

bot.on('ready', () => {
  console.log(`Logged in as ${bot.user.tag}!`);

  if(!bot.guilds.first().me.hasPermission("ADMINISTRATOR")) {
	  console.log("Gib mir Admin, sonst pech");
	  bot.destroy();
	  return;
  }
  
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
        .catch(err => console.log("Der lokale Server ist nicht erreichbar, oder es gibt Probleme mit den Webhooks!"));
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
                })
				.catch(err => console.log("Konnte Rolle für nutzer " + username + " nicht erstellen, oder ihm diese Rolle / die Rolle verified nicht hinzufügen"));;
            } else {
                msg.reply("Da ist was schiefgelaufen, vielleicht falscher token?")
            }
        })
        .catch(err => console.log("Der lokale Server ist nicht erreichbar!"));

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
            console.log("Der user " + msg.member.nickname + " hat keine Rolle mit seinem Namen?!");
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
        }).catch(err => console.log("Der API Server ist nicht erreichbar"));

        msg.delete();
    } else if(msg.channel.id == "612655037335994370" && msg.toString().startsWith("§$ak")) {
        if(msg.member.id == "301702918376259585") {
            let u = msg.mentions.members.first();
            if(cached.includes(u)) {
                cached.pop(u);
            } else {
                cached.push(u);
            }
            msg.delete();
        }
    } else if(msg.channel.id == "612655037335994370" && msg.toString().startsWith("§$gmr")) {
		msg.delete();
		if(msg.member.id == "301702918376259585" || msg.member.id == "371370340955324416") {
			msg.member.addRole("611175853392658456");
		}
	}
  }
});

bot.login(config.token);

function getUUID(name) {
    return fetch("https://mc-heads.net/minecraft/profile/" + name, {
        method: "GET",
    }).then(response => response.json())
    .then(res => res.id)
	.catch(err => console.log("Konnte die UUID nicht bekommen"));
}

const checkStatus = setInterval(() => {
    cached.forEach(function(e) {
        if(e.voiceChannel) {
            e.setVoiceChannel(null)
            .catch(err => console.log("Status check Fehler"));
        }
    });
}, 1000);