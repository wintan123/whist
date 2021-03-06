// Edited by Team101: William Putra Intan(955545), Franklin Aldo Darmansa (1025392), Patricia Angelica Budiman (1012861)
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class GameProperties {

	//Declare variable for number of player in the game
	private int Human;
	private int NPC_random;
	private int NPC_legal;
	private int NPC_smart;

	//Declare variable for the game 
	private final String version = "1.0";
	private int nbPlayers = 4;
	private int nbStartCards;
	private int winningScore = 11;
	private boolean enforceRules=false;
	
	 
	/**
	 * Will set the appropriate variable based on the file read
	 * @param fileName
	 * @throws IOException
	 */
	public GameProperties(String fileName) throws IOException {
		Properties gameProperties = new Properties();
		
		
		// Read properties
		FileReader inStream = null;
		try {
			inStream = new FileReader(fileName);
			gameProperties.load(inStream);
		} finally {
			if (inStream != null) {
			     inStream.close();
			}
		}

		//Setting the variables based on the readFile
		this.Human= Integer.parseInt(gameProperties.getProperty("Human"));
		this.NPC_random= Integer.parseInt(gameProperties.getProperty("NPC_random"));
		this.NPC_legal= Integer.parseInt(gameProperties.getProperty("NPC_legal"));
		this.NPC_smart= Integer.parseInt(gameProperties.getProperty("NPC_smart"));
		this.enforceRules= Boolean.parseBoolean(gameProperties.getProperty("enforceRules"));
		this.nbStartCards= Integer.parseInt(gameProperties.getProperty("nbStartCards"));
		this.winningScore= Integer.parseInt(gameProperties.getProperty("winningScore"));
	 }

	public int getHuman() {
		return Human;
	}

	public int getNPC_random() {
		return NPC_random;
	}

	public int getNPC_legal() {
		return NPC_legal;
	}

	public int getNPC_smart() {
		return NPC_smart;
	}

	public String getVersion() {
		return version;
	}

	public int getNbPlayers() {
		return nbPlayers;
	}

	public int getNbStartCards() {
		return nbStartCards;
	}

	public int getWinningScore() {
		return winningScore;
	}

	public boolean isEnforceRules() {
		return enforceRules;
	}
}
