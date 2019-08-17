const Discord = require('discord.js');
const bot = new Discord.Client();
const config = require("./botconfig.json");
const fetch = require('node-fetch');

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
                return;
            }
            let username = res.username;
            let text = res.text.replace(/&%'/g, '"');

            bot.channels.get(config.channel).fetchWebhooks().then(hooks => hooks.find(r => r.name === username))
            .then(async(hook) => {
                if(!hook) {
                    let uuid = await getUUID(username);
                    hook = bot.channels.get(config.channel).createWebhook(username, "https://mc-heads.net/avatar/"+ uuid).then(h => h.send(text));
                    return;
                }
                hook.send(text);
            })
        })
        .catch(err => {});
    }, 50);
});

bot.on('message', msg => {
  if (!msg.author.bot) {
    //.verify
    if(msg.toString().startsWith(".verify")) {

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
        .then(res => username = res)
        .then(function() {
            console.log(username)

            if(username != "" && username != null) {
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
                    if(bot.channels.get(config.channel).fetchWebhooks().exists("name", username)) {
                        let uuid = await getUUID(username);
                        hook = bot.channels.get(config.channel).createWebhook(username, "https://mc-heads.net/avatar/"+uuid);
                    }
                    msg.reply("Erfolgreich verifiziert!");
                });
            } else {
                msg.reply("Da ist was schiefgelaufen, vielleicht falscher token?")
            }
        })
        .catch(err => console.error(err));

        return;
    }
    //if not .verify
    if(!msg.member.roles.find(r => r.name === "verified")) {
        msg.reply("Du musst dich zuerst verifizieren mit '.verify <token>', den token bekommst du in mc mit '.auth'");
        return;
    }

    if(msg.toString.length > 90) {
        msg.reply("Die Nachricht darf nicht länger als 90 Zeichen sein!");
        return;
    }

    let username = msg.member.roles.find(r => r.color === 37887).name;
    let text = msg.toString();

    console.log(username + ": " + msg.toString());

    msg.delete();

    bot.channels.get(config.channel).fetchWebhooks().then(hooks => hooks.find(r => r.name === username))
            .then(async(hook) => {
                if(!hook) {
                    let uuid = await getUUID(username);
                    hook = bot.channels.get(config.channel).createWebhook(username, "https://mc-heads.net/avatar/"+ uuid).then(h => h.send(text));
                    return;
                }
                hook.send(text);
            })

    fetch(config.server, {
        method: "POST",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            username: msg.member.roles.find(r => r.color === 37887).name,
            text: msg.toString().replace(/"/g, "&%'")
        })
    }).catch(err => msg.reply("Der API Server ist nicht erreichbar"));
  }
});

bot.login(config.token);

function getUUID(name) {
    return fetch("https://mc-heads.net/minecraft/profile/" + name, {
        method: "GET",
    }).then(response => response.json())
    .then(res => res.id);
}