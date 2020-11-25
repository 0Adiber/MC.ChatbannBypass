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
}
