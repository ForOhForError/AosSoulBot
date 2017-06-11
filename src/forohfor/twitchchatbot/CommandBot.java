package forohfor.twitchchatbot;

import java.util.ArrayList;

public class CommandBot extends ChatBot{

	ArrayList<Command> commands = new ArrayList<Command>();
	
	public CommandBot(String nick, String channel, String oAuth) {
		super(nick, channel, oAuth);
	}

	public void parseMessage(String user, String message) {
		System.out.println(message);
		for(Command com:commands){
			if(message.startsWith("!"+com.getCommandString())){
				int len = 1+com.getCommandString().length();
				String newMsg = message.substring(len);
				com.parseCommand(user, newMsg);
				break;
			}
		}
	}

	public void init(){
		
	}

	public void registerCommand(Command com){
		com.load(this);
		commands.add(com);
	}
	
	public static void main(String[] args) throws Exception{
		//GET OAUTH AT http://www.twitchapps.com/tmi
		//ChatBot bot = new SampleBot("bot_name","channel_to_join","oauth_goes_here");
		CommandBot bot = new CommandBot("403_bot","ForOhForError","oauth:b46io770cmdibpilbj1o48rjdlfbsn");
		
		Command test = new Command(){

			int[] exin = {ARG_STRING,ARG_INT};
			
			@Override
			int[] getExpectedInput() {
				// TODO Auto-generated method stub
				return exin;
			}

			@Override
			String getCommandString() {
				return "test";
			}

			@Override
			public void init() {
			}

			@Override
			public void runCommand(String user, CommandArgument[] args) {
				System.out.println(args[0].stringValue()+(2*args[1].intValue()));
			}
			
		};
		
		bot.registerCommand(test);
		
		bot.start();
	}
}
