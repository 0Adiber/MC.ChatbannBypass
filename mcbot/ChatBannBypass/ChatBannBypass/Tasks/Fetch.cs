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
using System.Web.Script.Serialization;

namespace ChatBannBypass.Tasks
{
    class Fetch : ITask
    {
        private static JavaScriptSerializer js;
        #region PluginStandard
        private readonly string server;
        private readonly string[] not_sync;

        public Fetch(string server, string[] not_sync)
        {
            this.server = server.Contains("http://")?server:"http://"+server;
            this.not_sync = not_sync;
            js = new JavaScriptSerializer();
        }

        public override bool Exec()
        {
            return true;
        }

        public override async Task Start()
        {
            Context.Events.onChat += OnChat;
            Context.Events.onTick += OnTick;
        } 
        
        public override async Task Stop()
        {
            Context.Events.onChat -= OnChat;
            Context.Events.onTick -= OnTick;
        }

        #endregion

        private void OnChat(IBotContext context, IChat message, byte position)
        {
            string msg = message.GetText();
            if (msg.StartsWith("[Clans]"))
            {
                if (!msg.Contains(":")) return;
                msg = msg.Replace("[Clans]", "");
                string[] parts = msg.Trim().Split(new char[] { ':' }, 2);
                string username = parts[0].Trim();

                parts[1] = parts[1].Trim();

                if (!parts[1].StartsWith(".auth"))
                {
                    if (not_sync.Contains(username))
                    {
                        return;
                    }

                    string text = parts[1];

                    var req = (HttpWebRequest)WebRequest.Create(server + "/message/MC");
                    req.Method = "POST";
                    req.UserAgent = "ChatBannBypass Bot";
                    req.ContentType = "application/json";
                    req.Credentials = CredentialCache.DefaultCredentials;
                    ServicePointManager.ServerCertificateValidationCallback += (sender, cert, chain, sslPolicyErrors) => true;

                    using (var streamWriter = new StreamWriter(req.GetRequestStream()))
                    {
                        var obj = new Beans.Message(username, text);

                        streamWriter.Write(js.Serialize(obj));
                    }

                    HttpWebResponse res;
                    try
                    {
                        res = (HttpWebResponse)req.GetResponse();
                    }
                    catch (Exception)
                    {
                        Console.WriteLine("Server cannot be reached!");
                        return;
                    }
                    res.Close();
                    return;
                }

                //parts[1] = ".auth <Token>"
                if(parts[1].Split(new char[] { ' ' }).Length < 2)
                {
                    context.Functions.Chat("/cc Ungültiger Befehl! - (.auth <token>)");
                    return;
                }
                string token = parts[1].Split(new char[] { ' ' })[1].Trim();

                var request = (HttpWebRequest)WebRequest.Create(server + "/verify");
                request.Method = "POST";
                request.UserAgent = "ChatBannBypass Bot";
                request.ContentType = "application/json";
                request.Credentials = CredentialCache.DefaultCredentials;
                ServicePointManager.ServerCertificateValidationCallback += (sender, cert, chain, sslPolicyErrors) => true;

                using (var streamWriter = new StreamWriter(request.GetRequestStream()))
                {
                    var obj = new Beans.Verify(username, token);
                    streamWriter.Write(js.Serialize(obj));
                }

                HttpWebResponse response;
                try
                {
                    response = (HttpWebResponse)request.GetResponse();
                }
                catch (WebException e)
                {
                    response = (HttpWebResponse)e.Response;
                    if (response != null && response.StatusCode == HttpStatusCode.BadRequest)
                    {
                        var err = js.Deserialize<Beans.Error>(new StreamReader(response.GetResponseStream()).ReadToEnd());
                        if (err == null)
                            return;
                        context.Functions.Chat("/cc " + err.message);
                    }
                    response.Close();
                }
                response.Close();
            }
        }

        private void OnTick(IBotContext context)
        {
            var request = (HttpWebRequest)WebRequest.Create(server + "/message/DC");
            request.Method = "GET";
            request.UserAgent = "ChatBannBypass Bot";
            request.Credentials = CredentialCache.DefaultCredentials;
            ServicePointManager.ServerCertificateValidationCallback += (sender, cert, chain, sslPolicyErrors) => true;

            WebResponse response;
            try
            {
                response = request.GetResponse();
            }
            catch (Exception)
            {
                Console.WriteLine("Server cannot be reached!");
                return;
            }
            string responseFromServer = "";
            using (Stream dataStream = response.GetResponseStream())
            {
                StreamReader reader = new StreamReader(dataStream);
                responseFromServer = reader.ReadToEnd();
            }

            response.Close();

            var msg = js.Deserialize<Beans.Message>(responseFromServer);

            if(msg == null)
            {
                return;
            }

            string username = msg.sender;
            string text = msg.msg;

            username = username.Trim('"');
            text = text.Trim('"').Replace("&%'", "\"");

            context.Functions.Chat("/cc " + username + ": " + text);
        }

    }
}
