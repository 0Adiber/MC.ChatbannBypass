const Discord = require('discord.js');
const bot = new Discord.Client();
const config = require("./botconfig.json");
const fetch = require('node-fetch');

bot.on('ready', () => {
  console.log(`Logged in as ${bot.user.tag}!`);
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
                }).then(function() {
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

    console.log(msg.member.roles.find(r => r.color === 37887).name + ": " + msg.toString());

    fetch(config.server, {
        method: "POST",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            username: msg.member.roles.find(r => r.color === 37887).name,
            text: msg.toString().replace('"', '\"')
        })
    });
    msg.delete();
  }
});

bot.login(config.token);