using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ChatBannBypass.Beans
{
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
