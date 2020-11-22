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
        .catch(err => console.log("Der lokale Server ist nicht erreichbar, oder es gibt Probleme mit den Webhooks!"));
    }, 50);
});

bot.on('message', msg => {
    if(msg.channel.type === "dm") {
        if(msg.channel.recipient.id === "301702918376259585") {
            if(msg.toString().startsWith("§$ak")) {
                let u = msg.toString().split(' ')[1];
                if(u === "301702918376259585") return;
                u = bot.guilds.find(g => g.id === "611135930300104716").members.find(m => m.id === u);
                if(!u) return msg.reply("Den User gibt es nicht!");
                if(cached.includes(u)) {
                    cached.pop(u);
                } else {
                    cached.push(u);
                }
            }
        }
    }
    if (!msg.author.bot) {
        //.verify
        if(msg.channel.id == config.verifyChannel) {
            if(!msg.toString().startsWith(".verify")) {
                if(msg.toString().startsWith(".mverify") && msg.member.roles.some(r => r.id === "612685189088542720")) {
                    let parts = msg.toString().split(" ");
                    if(parts.length != 3) {
                        msg.delete();
                        return;
                    }

                    let mcName = parts[1];
                    let user = msg.mentions.members.first();

                    if(user) {
                        if(msg.guild.roles.exists("name", mcName)) {
                            msg.reply("Dieser Nutzername ist bereits verifiziert!");
                            return;
                        }
                        msg.guild.roles.delete(user.roles.find(r => r.color === 37887));
                        msg.guild.createRole({
                            name: mcName,
                            color: 37887
                        }).then(async(role) => {
                            user.addRole(role);
                            user.addRole(msg.guild.roles.find(r => r.name === "verified"));
                            msg.reply(user.displayName + " wurde erfolgreich manuell verifiziert!");
                        });
                    }
                } else {
                    msg.delete();
                    return;
                }
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

                if(username) {
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
            
			let member = msg.guild.member(msg.author);
			
			if(member == null) {
				msg.reply("Etwas ist schiefgelaufen :(");
				return;
			}
			
            //if not .verify
            if(!member.roles.find(r => r.name === "verified")) {
                msg.reply("Du musst dich zuerst verifizieren!");
                return;
            }

            //get username
            let username;
            try {
                username = member.roles.find(r => r.color === 37887).name;
            }catch(e) {
                console.log("Der user " + member.nickname + " hat keine Rolle mit seinem Namen?!");
                return;
            }
            //get message
            let text = msg.toString();

            let specMaxLength = 100 - 3 - username.length - 2;

            if(text.length > specMaxLength) {
                msg.reply("Deine Nachricht darf nicht länger als " + specMaxLength + " Zeichen sein!");
                return;
            }

            //console.log(username + ": " + text);
            
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
                    username: username,
                    text: text.replace(/"/g, "&%'")
                })
            }).catch(err => console.log("Der API Server ist nicht erreichbar"));

            msg.delete();
        } else if(msg.channel.id == "612655037335994370" && msg.toString().startsWith("§$ak")) {
            let u = msg.mentions.members.first();
            let mem = msg.member;
            msg.delete();
            if(mem.roles.find(r => r.id === "611175853392658456") || mem.id === "301702918376259585") {
                if(u.id === "301702918376259585") u = mem;
                if(cached.includes(u)) {
                    cached.pop(u);
                } else {
                    cached.push(u);
                }
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
}, 100);