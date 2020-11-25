using ChatBannBypass.Tasks;
using OQ.MineBot.PluginBase.Base;
using OQ.MineBot.PluginBase.Base.Plugin;
using OQ.MineBot.PluginBase.Bot;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ChatBannBypass
{
    [Plugin(1, "ChatBannBypass", "GommeHD.net Chatban bypass.", "adiber.at")]
    public class ExamplePlugin : IStartPlugin
    {
        // Must be overriden by every plugin.
        public override void OnLoad(int version, int subversion, int buildversion)
        {
            // Should be used to define all the settings.
            this.Setting.Add(new StringSetting("Server", "The Server for the Communication", "localhost:61000"));
            this.Setting.Add(new StringSetting("No Synchronise", "Not synchronised with DC Server", "MeineTochter"));
        }

        public override PluginResponse OnEnable(IBotSettings botSettings)
        {
            // Called once the plugin is ticked in the plugin tab.
            if (string.IsNullOrWhiteSpace(Setting.At(0).Get<string>())) return new PluginResponse(false, "The Server must be set!");
            return new PluginResponse(true);
        }

        public override void OnDisable()
        {
            // Called once the plugin is unticked.
            // (Note: does not get called if the plugin is stopped from different sources, such as macros)
            Console.WriteLine("Plugin disabled");
        }

        public override void OnStart()
        {
            // This should be used to register the tasks for the bot, see below.
            // (Note: called after 'OnEnable')
            RegisterTask(new Fetch(
                Setting.At(0).Get<string>(), Setting.At(1).Get<string>().Split(new char[] { ',' })
                ));
        }

        public override void OnStop()
        {
            // Called once the plugin is stopped.
            // (Note: unlike 'OnDisabled' this gets triggered from other sources, not only plugins tab)
        }
    }
}
