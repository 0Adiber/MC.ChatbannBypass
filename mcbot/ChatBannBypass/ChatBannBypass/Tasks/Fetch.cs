using OQ.MineBot.PluginBase;
using OQ.MineBot.PluginBase.Base.Plugin.Tasks;
using OQ.MineBot.PluginBase.Classes.Base;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;

namespace ChatBannBypass.Tasks
{
    class Fetch : ITask
    {
        private readonly string server;
        private readonly string[] not_sync;

        public Fetch(string server, string[] not_sync)
        {
            this.server = server.Contains("http://")?server:"http://"+server;
            this.not_sync = not_sync;
        }

        public override bool Exec()
        {
            return true;
        }
        public override void Start() { player.events.onChat += OnChat; player.events.onTick += OnTick; StartUp(); }
        public override void Stop() { player.events.onChat -= OnChat; player.events.onTick -= OnTick; StopCon(); }

        private void StartUp()
        {
            var request = (HttpWebRequest)WebRequest.Create(server + "/start");
            request.Method = "POST";
            request.UserAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36";
            request.ContentType = "application/json";
            request.Credentials = CredentialCache.DefaultCredentials;
            ServicePointManager.ServerCertificateValidationCallback += (sender, cert, chain, sslPolicyErrors) => true;

            var response = (HttpWebResponse)request.GetResponse();
            response.Close();
        }

        private void StopCon()
        {
            var request = (HttpWebRequest)WebRequest.Create(server + "/stop");
            request.Method = "POST";
            request.UserAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36";
            request.ContentType = "application/json";
            request.Credentials = CredentialCache.DefaultCredentials;
            ServicePointManager.ServerCertificateValidationCallback += (sender, cert, chain, sslPolicyErrors) => true;

            var response = (HttpWebResponse)request.GetResponse();
            response.Close();
        }

        private void OnChat(IPlayer player, IChat message, byte position)
        {
            string msg = message.GetText();
            if (msg.StartsWith("[Clans]"))
            {
                if (!msg.Contains(":")) return;
                msg = msg.Replace("[Clans]", "");
                string[] parts = msg.Trim().Split(new char[] { ':' }, 2);
                if (!parts[1].Trim().Equals(".auth"))
                {
                    string username = parts[0].Trim();

                    if (not_sync.Contains(username))
                    {
                        return;
                    }

                    string text = parts[1].Replace("\"", "&%'");

                    var req = (HttpWebRequest)WebRequest.Create(server + "/chat");
                    req.Method = "POST";
                    req.UserAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36";
                    req.ContentType = "application/json";
                    req.Credentials = CredentialCache.DefaultCredentials;
                    ServicePointManager.ServerCertificateValidationCallback += (sender, cert, chain, sslPolicyErrors) => true;

                    using (var streamWriter = new StreamWriter(req.GetRequestStream()))
                    {
                        string json = "{\"username\":\"" + username + "\"," +
                                      "\"text\":\"" + text + "\"}";

                        streamWriter.Write(json);
                    }

                    var res = (HttpWebResponse)req.GetResponse();
                    res.Close();
                    return;
                }

                string token = RandomString(11, false);

                var request = (HttpWebRequest)WebRequest.Create(server + "/auth");
                request.Method = "POST";
                request.UserAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36";
                request.ContentType = "application/json";
                request.Credentials = CredentialCache.DefaultCredentials;
                ServicePointManager.ServerCertificateValidationCallback += (sender, cert, chain, sslPolicyErrors) => true;

                using (var streamWriter = new StreamWriter(request.GetRequestStream()))
                {
                    string json = "{\"token\":\""+ token + "\"," +
                                  "\"username\":\""+ parts[0].Trim() +"\"}";

                    streamWriter.Write(json);
                }

                var response = (HttpWebResponse)request.GetResponse();
                response.Close();
                player.functions.Chat("/cc " + parts[0].Trim() + " Dein Token ist: " + token);
            }
        }

        private void OnTick(IPlayer player)
        {
            var request = (HttpWebRequest)WebRequest.Create(server);
            request.Method = "GET";
            request.UserAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36";
            request.Credentials = CredentialCache.DefaultCredentials;
            ServicePointManager.ServerCertificateValidationCallback += (sender, cert, chain, sslPolicyErrors) => true;

            WebResponse response;
            try
            {
                response = request.GetResponse();
            }
            catch (Exception)
            {
                return;
            }
            string responseFromServer = "";
            using (Stream dataStream = response.GetResponseStream())
            {
                StreamReader reader = new StreamReader(dataStream);
                responseFromServer = reader.ReadToEnd();
            }

            response.Close();

            responseFromServer = responseFromServer.TrimEnd('}').TrimStart('{');

            string[] parts = responseFromServer.Split(new char[] { ',' }, 2);

            string username = parts[0].Split(new char[] { ':' })[1];
            string text = parts[1].Split(new char[] { ':' })[1];

            username = username.Trim('"');
            text = text.Trim('"').Replace("&%'", "\"");


            player.functions.Chat("/cc " + username + ": " + text);
        }

        public string RandomString(int size, bool lowerCase)
        {
            StringBuilder builder = new StringBuilder();
            Random random = new Random();
            char ch;
            for (int i = 0; i < size; i++)
            {
                ch = Convert.ToChar(Convert.ToInt32(Math.Floor(26 * random.NextDouble() + 65)));
                builder.Append(ch);
            }
            if (lowerCase)
                return builder.ToString().ToLower();
            return builder.ToString();
        }

    }
}
