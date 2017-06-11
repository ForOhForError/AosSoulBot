package forohfor.twitchchatbot;

/**
 * Simple example bot. Prints received messages to System.out.
 * @author ForOhForError
 */

public class PrinterBot extends ChatBot{

	public PrinterBot(String nick, String channel, String oAuth) {
		super(nick, channel, oAuth);
	}

	public void parseMessage(String user, String message) {
		System.out.println(user+": "+message);
	}

	public void init(){
		
	}

}
