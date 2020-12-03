using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;
using System.Web.Script.Serialization;

namespace Test
{
    class Program
    {
        static void Main(string[] args)
        {
            /*
            var request = (HttpWebRequest)WebRequest.Create("http://localhost:61000/message/DC");
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
                Console.ReadKey();
                return;
            }
            string responseFromServer = "";
            using (Stream dataStream = response.GetResponseStream())
            {
                StreamReader reader = new StreamReader(dataStream);
                responseFromServer = reader.ReadToEnd();
            }

            response.Close();

            var msg = new JavaScriptSerializer().Deserialize<Message>(responseFromServer);

            if(msg == null)
            {
                Console.WriteLine("No Message");
                Console.ReadKey();
                return;
            }

            string username = msg.sender;
            string text = msg.msg;

            username = username.Trim('"');
            text = text.Trim('"').Replace("&%'", "\"");

            Console.WriteLine(username + text);
            Console.ReadKey();
        }*/
        
            JavaScriptSerializer js = new JavaScriptSerializer();
            string text = Console.ReadLine();

            var req = (HttpWebRequest)WebRequest.Create("http://localhost:61000/message/MC");
            req.Method = "POST";
            req.UserAgent = "ChatBannBypass Bot";
            req.ContentType = "application/json";
            req.Credentials = CredentialCache.DefaultCredentials;
            ServicePointManager.ServerCertificateValidationCallback += (sender, cert, chain, sslPolicyErrors) => true;

            using (var streamWriter = new StreamWriter(req.GetRequestStream()))
            {
                var obj = new Message("Adiber", text);

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

            /*
            string token = Console.ReadLine();

            var request = (HttpWebRequest)WebRequest.Create("http://localhost:61000" + "/verify");
            request.Method = "POST";
            request.UserAgent = "ChatBannBypass Bot";
            request.ContentType = "application/json";
            request.Credentials = CredentialCache.DefaultCredentials;
            ServicePointManager.ServerCertificateValidationCallback += (sender, cert, chain, sslPolicyErrors) => true;

            using (var streamWriter = new StreamWriter(request.GetRequestStream()))
            {
                var obj = new Verify("Adiber", token);
                streamWriter.Write(js.Serialize(obj));
            }

            HttpWebResponse response;
            try
            {
                response = (HttpWebResponse)request.GetResponse();
            }
            catch (WebException e)
            {
                response = (HttpWebResponse) e.Response;
                if (response != null && response.StatusCode == HttpStatusCode.BadRequest)
                {
                    var err = js.Deserialize<Error>(new StreamReader(response.GetResponseStream()).ReadToEnd());
                    if (err == null)
                        return;
                    Console.WriteLine(err.message);
                }
                response.Close();
            }
            
            Console.ReadKey();*/
        }
    }

    class Message
    {
        public string sender { get; set; }
        public string msg { get; set; }

        public Message(string sender, string msg)
        {
            this.sender = sender;
            this.msg = msg;
        }

        public Message()
        {

        }
    }

    class Verify
    {
        public string username { get; set; }
        public string token { get; set; }

        public Verify(string username, string token)
        {
            this.username = username;
            this.token = token;
        }

        public Verify()
        {

        }
    }

    class Error
    {
        public string status { get; set; }
        public string timestamp { get; set; }
        public string message { get; set; }

        public Error(string status, string timestamp, string message)
        {
            this.status = status;
            this.timestamp = timestamp;
            this.message = message;
        }

        public Error()
        {

        }
    }
}
