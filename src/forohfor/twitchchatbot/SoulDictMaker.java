package forohfor.twitchchatbot;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Scanner;

public class SoulDictMaker {
	private static final double TARGET_BASE = 7;
	private static final double SOUL_RING_BONUS = 8;
	public static void main(String[] a) throws Exception{
		int luck = 0;
		Scanner scan = new Scanner(new File("souldict.dat"));
		
		System.out.println("Enemy Name          Base %      % With Ring\n-------------------------------------------");
		
		while(scan.hasNext()){
			DecimalFormat formatter = new DecimalFormat("0.00");
			String line = scan.nextLine();
			
			String name = line.split(":")[0];
			
			String r = line.split(":")[1];
			
			int rarity = Integer.parseInt(r);
			
			if(rarity != 0){
				long bottom = 32l + (8l*rarity) - (luck/16l);
				double normalDrop = (TARGET_BASE/bottom)*100;
				double souleaterDrop = ((TARGET_BASE+SOUL_RING_BONUS)/bottom)*100;
				
				String normal = formatter.format(normalDrop)+"%";
				String soulEat = formatter.format(souleaterDrop)+"%";
				
				System.out.printf("%-17s %8s %16s\n",capitalizeString(name),normal,soulEat);
			}
		}
		
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
}
