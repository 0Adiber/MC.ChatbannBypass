using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ChatBannBypass.Beans
{
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
    }
}
