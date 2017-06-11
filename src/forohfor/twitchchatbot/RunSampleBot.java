package forohfor.twitchchatbot;

public class RunSampleBot {
	public static void main(String[] args) throws Exception{
		//GET OAUTH AT http://www.twitchapps.com/tmi
		ChatBot bot = new SampleBot("bot_name","channel_to_join","oauth_goes_here");
		bot.start();
	}
}
