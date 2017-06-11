package forohfor.twitchchatbot;

public abstract class Command {

	protected static final int ARG_STRING = 0;
	protected static final int ARG_INT = 1;
	protected static final int ARG_DOUBLE = 2;
	
	protected boolean autofillMissing = false;
	protected ChatBot bot;
	
	abstract int[] getExpectedInput();
	abstract String getCommandString();
	
	public void load(ChatBot bot){
		this.bot = bot;
		init();
	}

	public abstract void init();

	public boolean parseCommand(String user, String message){
		try{
			String[] inputs = message.split(" ");
			int[] expectedInput = getExpectedInput();
			
			CommandArgument[] args = new CommandArgument[expectedInput.length];
			
			for(int i=0;i<expectedInput.length;i++){
				try{
					int type = expectedInput[i];
					if(type == ARG_STRING){
						args[i] = new CommandArgument(inputs[i]);
					}else if(type == ARG_INT){
						args[i] = new CommandArgument(Integer.parseInt(inputs[i]));
					}else if(type == ARG_DOUBLE){
						args[i] = new CommandArgument(Double.parseDouble(inputs[i]));
					}
				}catch(Exception e){
					if(autofillMissing){
						int type = expectedInput[i];
						if(type == ARG_STRING){
							args[i] = new CommandArgument("");
						}else if(type == ARG_INT){
							args[i] = new CommandArgument(0);
						}else if(type == ARG_DOUBLE){
							args[i] = new CommandArgument(0.0);
						}
					}else{
						return false;
					}
				}
			}
			runCommand(user,args);
			return true;
		}catch(Exception e){
			return false;
		}
	}
	
	public abstract void runCommand(String user, CommandArgument[] args);
}
