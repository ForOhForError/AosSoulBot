package forohfor.twitchchatbot;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

/**
 * An abstract definition of a chatbot to be used on Twitch.tv. Runs as a thread.
 * @author ForOhForError
 */

public abstract class ChatBot extends Thread{

	/** Address of Twitch's chat server */
	public static final String TWITCH_SERVER = "irc.twitch.tv";

	/** Port of Twitch's chat server */
	public static final int TWITCH_PORT = 6667;

	/** The username used when parseMessage() is called with a channel notification. */
	public static final String NOTIFICATION_USERNAME="JTV";

	/** This bot's owner. Optional. */
	protected String botOwner;

	/** This bot's name. getName() and setName() refer to the thread's name, not the bot's. */
	protected String nick;

	/** The twitch channel this bot should join  */
	protected String channel;

	/** The oAuth token used in place of a password */
	protected String oAuth;

	/** Debug mode. Prints all received data to System.err */
	protected boolean debug = false;

	/** The number of times this bot has sent a message in the last 30 seconds */
	protected int queue = 1000;

	/** The time in seconds between each queue reset */
	private int queueResetTime = 30;
	/** The number of messages allowed per queue reset interval
	 *  before the the bot will not send more */
	private int spamCutoff = 20;

	/** Connection to the chat server */
	private Socket irc;

	/** Reads data */
	private BufferedReader reader;

	/** Sends data */
	private BufferedWriter writer;

	/** True if kill() has been called */
	private boolean stopped = false;

	/** Thread responsible for resetting the spam queue */
	private Thread queueReset = null;

	/** Generic whitelist, for convenience */
	protected ArrayList<String> whitelist = new ArrayList<String>();

	/** Generic blacklist, for convenience */
	protected ArrayList<String> blacklist = new ArrayList<String>();

	protected boolean connected = false;

	/**
	 * Constructs a new chatbot.
	 * @param botOwner the username of the owner of this bot
	 * @param nick the bot's username
	 * @param channel the twitch channel this bot should join
	 * @param oAuth the oAuth token for the bot. Obtain this token
	 * from http://www.twitchapps.com/tmi/
	 */
	public ChatBot(String botOwner, String nick, String channel, String oAuth) {
		super();
		this.botOwner = botOwner.toLowerCase();
		this.nick = nick.toLowerCase();
		this.channel = channel.toLowerCase();
		this.oAuth = oAuth;
	}

	public ChatBot(){
		super();
		this.botOwner = "";
		this.nick = "";
		this.channel = "";
		this.oAuth = "";
	}
	
	/**
	 * Constructs a new chatbot.
	 * @param nick the bot's username
	 * @param channel the twitch channel this bot should join
	 * @param oAuth the oAuth token for the bot. Obtain this token
	 * from http://www.twitchapps.com/tmi/
	 */
	public ChatBot(String nick, String channel, String oAuth) {
		super();
		this.botOwner = null;
		this.nick = nick;
		this.channel = channel;
		this.oAuth = oAuth;
	}

	protected boolean send(String msg){
		try{
			writer.write(msg + "\r\n");
			flush();

			if(debug){
				System.err.println("\t< "+msg);
			}

			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	protected boolean flush(){
		try{
			writer.flush();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void run(){
		queueReset = new Thread(){
			public void run(){
				while( !stopped ){
					queue = 0;
					try {
						sleep(queueResetTime*1000);
					} catch (InterruptedException e) {}
				}
			}
		};

		queueReset.start();

		try {
			irc = new Socket(InetAddress.getByName(TWITCH_SERVER), TWITCH_PORT);
			reader = new BufferedReader(new InputStreamReader(irc.getInputStream()));
			writer = new BufferedWriter(new OutputStreamWriter(irc.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		send("PASS "+oAuth);
		send("USER "+nick+" 0 * :"+botOwner);
		send("NICK "+nick);

		try{
			while( !stopped ){
				String data = reader.readLine();
				if(!connected){
					if (data.indexOf("004") >= 0) {
						connected = true;						
						send("JOIN #"+channel);
						System.err.println("Connecting to channel "+channel+".");
						init();
					}else if (data.indexOf("433") >= 0) {
						System.err.println("Nickname is already in use.");
						kill();
					}
				}else{
					processData(data);
				}
			}
		} catch (IOException e) {}
	}

	private void readPrivmsg(String msg){
		String user = msg.substring(1,msg.indexOf("!"));
		int len = 29 + ( 3 * user.length() ) + channel.length();
		String message = msg.substring(len);
		parseMessage(user, message);
	}

	/**
	 * Processes incoming data. Passes chat messages to parseMessage(), prints other incoming 
	 * data to System.err if debug mode is enabled, and responds to server pings.
	 * @param data
	 */
	private void processData(String data) throws IOException{
		if(debug){
			System.err.println("\t> "+data);
		}
		if(data.startsWith("PING")){
			send("PONG :tmi.twitch.tv");
		}else if(data.indexOf("PRIVMSG") >= 0){
			readPrivmsg(data);
		}
	}


	/**
	 * Sends a chat message
	 * @param msg the string to send as a chat message
	 * @return true if the message is sent successfully, and false if the message cannot be sent
	 * or if the bot has sent up to its limit of messages since the last time the spam queue
	 * refreshed.
	 */
	protected boolean message(String msg){
		if( !this.isAlive() ){
			return false;
		}
		queue = queue+1;
		if( queue < spamCutoff ){
			try{
				msg = new String ( msg.getBytes(), "ISO-8859-1" );
				send("PRIVMSG #"+channel+" :"+msg);
				return true;
			}catch (IOException e){
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Halts this bot's execution.
	 */
	public void kill(){
		stopped = true;
		interrupt();
		queueReset.interrupt();
		try {
			irc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Abstract method called each time a message is recieved by the bot.
	 * @param user the user who sent the message
	 * @param message the message's contents
	 */
	public abstract void parseMessage(String user, String message);

	/**
	 * Abstract method called after this bot connects to the given channel.
	 */
	public abstract void init();

	/**
	 * Adds the given user to the whitelist
	 * @param username a twitch username
	 */
	public void addToWhitelist(String username){
		whitelist.add(username.toLowerCase());
	}

	/**
	 * Adds the given user to the blacklist
	 * @param username a twitch username
	 */
	public void addToBlacklist(String username){
		blacklist.add(username.toLowerCase());
	}


	private BufferedReader getChatInfo(){
		try {
			URL chatInfo = new URL("http://tmi.twitch.tv/group/user/"+channel+"/chatters");
			URLConnection chatInfoCon = chatInfo.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					chatInfoCon.getInputStream()));
			return in;
		} catch (Exception e) {e.printStackTrace();}
		return null;
	}

	/**
	 * Returns a list of online mods
	 */
	protected ArrayList<String> getMods(){
		ArrayList<String> mods = new ArrayList<String>();
		boolean parse = false;
		try{
			BufferedReader chatInfo = getChatInfo();
			while(true){
				String line = chatInfo.readLine().trim();
				if(line==null){
					break;
				}
				if(line.startsWith("\"moderators\": [")){
					if(line.contains("]")){
						break;
					}
					parse = true;
				}else if(parse){
					if(line.contains("]")){
						break;
					}
					if(line.contains(",")){
						mods.add(line.substring(1, line.length()-2));
					}else{
						mods.add(line.substring(1, line.length()-1));
					}
				}
			}
			return mods;
		}catch (Exception e) {}
		return null;
	}

	/**
	 * Returns a list of online chatters
	 */
	protected ArrayList<String> getChatters(){
		ArrayList<String> chatters = new ArrayList<String>();
		boolean parse = false;
		try{
			BufferedReader chatInfo = getChatInfo();
			while(true){
				String line = chatInfo.readLine().trim();
				if(line==null){
					break;
				}
				if(line.startsWith("\"chatters\": {")){
					if(line.contains("}")){
						break;
					}
					parse = true;
				}else if(parse){
					if(line.contains("}")){
						break;
					}
					if(!line.contains("[")){
						if(!line.contains("]")){
							if(line.contains(",")){
								chatters.add(line.substring(1, line.length()-2));
							}else{
								chatters.add(line.substring(1, line.length()-1));
							}
						}
					}
				}
			}
			return chatters;
		}catch (Exception e) {}
		return null;
	}

	/**
	 * Loads a list of strings from a file, with each element being loader from a single line
	 * of text.
	 */
	public static ArrayList<String> loadListFromFile(File file){
		Scanner s;
		s = null;
		try {
			s = new Scanner(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if(s==null){
			return null;
		}
		ArrayList<String> list = new ArrayList<String>();
		while (s.hasNext()){
		    list.add(s.nextLine());
		}
		s.close();
		return list;
	}

	/**
	 * Saves a list to a file, with each element being on its own line.
	 */
	public void saveListToFile(Collection<String> list, File file){
		try {
			BufferedWriter save = 
					new BufferedWriter( new OutputStreamWriter( new FileOutputStream(file)));
			for(String item:list){
				save.write(item+System.lineSeparator());
			}
			save.flush();
			save.close();
		} catch (Exception e){

		}

	}
}
