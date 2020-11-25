using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ChatBannBypass.Beans
{
    class Verify
    {
        public string username { get; set; }
        public string token { get; set; }

        public Verify(string username, string token)
        {
            this.username = username;
            this.token = token;
        }
    }
}
