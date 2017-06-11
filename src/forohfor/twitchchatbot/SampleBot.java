package forohfor.twitchchatbot;

/**
 * Simple example bot. 
 */

public class SampleBot extends ChatBot{

	public SampleBot(String nick, String channel, String oAuth) {
		super(nick, channel, oAuth);
	}

	public void parseMessage(String user, String message) {
		if(message.startsWith("!fiveKappas")){
			for(int x=0;x<5;x++){
				message("Kappa");
			}
		}
		
		if(message.startsWith("!say")){
			if(message.split(" ").length>1){
				message(message.split(" ")[1]);
			}
		}
	}

	public void init(){
		
	}

}
