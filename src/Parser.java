
import java.util.StringTokenizer;

public class Parser{
	String line, prefix = "", command = "", params = "", middle = "", trailing = "", servername = "", nick = "", user = "", host = "";
	
	/**
	 * Initialize the parser
	 * @param line: Line to parse
	 */
	public Parser(String line){
		this.line = line;
		initTags();
	}
	
	/**
	 * Initialize tags
	 */
	private void initTags(){
		StringTokenizer st = new StringTokenizer(line, " \r\n");
		int totalTokens = st.countTokens();
		if(line.startsWith(":") && totalTokens >= 3){
			String temp = st.nextToken();
			int index = temp.indexOf(":");
			if(index != -1)
				prefix = temp.substring(index + 1);
				temp = st.nextToken();
				command = temp;
				temp = st.nextToken("\n");
				params = temp;
		}
		else if(!line.startsWith(":") && totalTokens >= 2){
			String temp = st.nextToken();
			command = temp;
			params = st.nextToken("\n");
		}
	}
	
	/**
	 * Gets the prefix
	 * @return: Returns the prefix
	 */
	public String getPrefix(){
		return prefix;
	}
	
	/**
	 * Gets the command
	 * @return: Returns the command
	 */
	public String getCommand(){
		return command;
	}
	
	/**
	 * Gets the parameter
	 * @return: Returns the parameter
	 */
	public String getParams(){
		return params;
	}
	
	/**
	 * Gets the server
	 * @return: Returns the server
	 */
	public String getServer(){
		if(!prefix.equals("")){
			int index = prefix.indexOf("!");
			if(index != -1){
				String temp = prefix.substring(0, index);
				servername = temp;
			}
		}
		return servername;
	}
	
	/**
	 * Gets the nickname
	 * @return: Returns the nickname
	 */
	public String getNick(){
		if(!prefix.equals("")){
			int index = prefix.indexOf("!");
			if(index != -1){
				String temp = prefix.substring(0, index);
				nick = temp;
			}
		}
		return nick;
	}
	
	/**
	 * Gets the user
	 * @return: Returns the user
	 */
	public String getUser(){
		if(!prefix.equals("")){
			int exMark = prefix.indexOf("!");
			int adMark = prefix.indexOf("@");
			if(exMark != -1 && adMark != -1 && (adMark > exMark))
				user = prefix.substring(exMark + 1, adMark);
		}
		return user;
	}
	
	/**
	 * Gets the host
	 * @return: Returns the host
	 */
	public String getHost(){
		if(!prefix.equals(""))
		{
			int adMark = prefix.indexOf("@");
			if(adMark != -1 && adMark >= 0)
				host = prefix.substring(adMark + 1);
		}		
		return host;
	}
	
	/**
	 * Gets the trailing
	 * @return: Returns the trailing
	 */
	public String getTrailing(){
		if (!params.equals("")){
			int index = params.indexOf(":");
			if(index != -1 && index >= 0)
				trailing = params.substring(index + 1);
		}
		return trailing;
	}
	
	/**
	 * Gets the middle
	 * @return: Returns the middle
	 */
	public String getMiddle(){
		if(!params.equals("")){
			int index = params.indexOf(":");
			if(index != -1 && index >= 0){
				if(params.startsWith(" ") && index - 1 >= 1)
					middle = params.substring(1, index - 1);
				else
					middle = params.substring(0, index - 1);
			}
		}
		return middle;
	}
        
        /**
         * toString
         * @return; returns String representation
         */
        public String toString()
        {
          return ("command:"+command+"| host:"+host+"| middle:"+middle+"| nick:" + nick+"| params:"+params+"| prefix:"+prefix+ "| server:"+servername+"| trailing:"+trailing+"| user:"+user);                       

        }
}