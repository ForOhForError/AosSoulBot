package forohfor.twitchchatbot;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Simple example bot. 
 */

public class AosSoulBot extends ChatBot{

	ArrayList<String> soulDict;
	DecimalFormat formatter = new DecimalFormat("0.0");

	private final double TARGET_BASE = 7;
	private final double SOUL_RING_BONUS = 8;

	public AosSoulBot() {
		File config = new File("config.txt");
		ArrayList<String> options = loadListFromFile(config);

		this.nick = options.get(0).split("#")[0];
		this.botOwner = options.get(1).split("#")[0];
		this.oAuth = options.get(2).split("#")[0];
		this.channel = options.get(3).split("#")[0];
	}

	public void parseMessage(String user, String message) {
		if(message.startsWith("!soul ")){
			if(message.split(" ").length>1){
				String soul = "";
				long luck = 0;

				//parse soul name and optional luck input
				String[] args = message.split(" ");
				int len = args.length;
				for(int i=0;i<len;i++){
					if(i != 0){
						if(i == len-1){
							if(isNumeric(args[i])){
								luck = Integer.parseInt(args[i]);
							}else{
								soul += args[i];
							}
						}else{
							soul += args[i]+" ";
						}
					}
				}
				soul = soul.toLowerCase().trim();
				long rarity = getRarity(soul);
				soul = capitalizeString(soul);

				if(rarity == -999){
					message("Could not find an enemy with that name");
					return;
				}

				if(rarity == 0){
					message(soul+" is a boss, or an enemy with no soul drop.");
				}

				long bottom = 32l + (8l*rarity) - (luck/16l);
				double normalDrop = (TARGET_BASE/bottom)*100;
				double souleaterDrop = ((TARGET_BASE+SOUL_RING_BONUS)/bottom)*100;

				if(normalDrop < 0 || souleaterDrop < 0){
					message("That's too much luck, sir Kappa");
					return;
				}

				String normal = formatter.format(normalDrop)+"%";
				String soulEat = formatter.format(souleaterDrop)+"%";

				String response = soul+": "+normal+" drop chance normally, or "+soulEat
						+" with the Soul Eater Ring (LCK = "+luck+")";

				message(response);
			}
		}else if(message.startsWith("!soulbot")){
			message("commands: !soul [enemy name] [luck stat]");
		}
	}

	public void init(){
		System.out.println("Connected!");
		message("Soulbot connected. Command syntax: !soul [enemy name] [luck stat]");
		soulDict = loadListFromFile(new File("soulDict.dat"));
	}

	public int getRarity(String soulName){
		for(String line:soulDict){
			if(line.startsWith(soulName)){
				return Integer.parseInt(line.split(":")[1].trim());
			}
		}
		return -999;
	}

	public static String capitalizeString(String string) {
		char[] chars = string.toLowerCase().toCharArray();
		boolean found = false;
		for (int i = 0; i < chars.length; i++) {
			if (!found && Character.isLetter(chars[i])) {
				chars[i] = Character.toUpperCase(chars[i]);
				found = true;
			} else if (Character.isWhitespace(chars[i]) || chars[i]=='.' || chars[i]=='\'') { // You can add other chars here
				found = false;
			}
		}
		return String.valueOf(chars);
	}

	public static boolean isNumeric(String str)  
	{  
		try  
		{  
			Double.parseDouble(str);  
		}  
		catch(NumberFormatException nfe)  
		{  
			return false;  
		}  
		return true;  
	}

}
