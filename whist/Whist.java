// Whist.java

import ch.aplu.jcardgame.*;
import ch.aplu.jgamegrid.*;

import java.awt.Color;
import java.awt.Font;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.io.FileReader;
import java.io.IOException;

@SuppressWarnings("serial")
public class Whist extends CardGame {
	private ArrayList<Observer> observers= new ArrayList();
	
		
	//  public enum Suit
	//  {
	//    SPADES, HEARTS, DIAMONDS, CLUBS
	//  }
	
	//  public enum Rank
	//  {
	//    // Reverse order of rank importance (see rankGreater() below)
	//	// Order of cards is tied to card images
	//	ACE, KING, QUEEN, JACK, TEN, NINE, EIGHT, SEVEN, SIX, FIVE, FOUR, THREE, TWO
	//  }
	//  
	  final String trumpImage[] = {"bigspade.gif","bigheart.gif","bigdiamond.gif","bigclub.gif"};
	
	  static final Random random = new Random(30006);
	  
	  // return random Enum value
	  public static <T extends Enum<?>> T randomEnum(Class<T> clazz){
	      int x = random.nextInt(clazz.getEnumConstants().length);
	      return clazz.getEnumConstants()[x];
	  }
	  
	  // return random Card from Hand
	  public static Card randomCard(Hand hand){
	      int x = random.nextInt(hand.getNumberOfCards());
	      return hand.get(x);
	  }
	 
	  // return random Card from ArrayList
	  public static Card randomCard(ArrayList<Card> list){
	      int x = random.nextInt(list.size());
	      return list.get(x);
	  }
	  
	  public boolean rankGreater(Card card1, Card card2) {
		  return card1.getRankId() < card2.getRankId(); // Warning: Reverse rank order of cards (see comment on enum)
	  }
	  
	  private static GameProperties gameProperties;
	  
	//private static int Human;
	//private static int NPC_random;
	//private static int NPC_legal;
	//private static int NPC_smart;
	//private static boolean enforceRules=false;
	//  private final String version = "1.0";
	//  public final int nbPlayers = 4;
	//  public static int nbStartCards;
	//  public static int winningScore = 11;
	  
	  
	  
	  int nbPlayers = gameProperties.getNbPlayers();
	  
	  
	  public Player[] players= new Player[nbPlayers];

	  
	  private final int handWidth = 400;
	  private final int trickWidth = 40;
	  private final Deck deck = new Deck(Suit.values(), Rank.values(), "cover");
	  private final Location[] handLocations = {
				  new Location(350, 625),
				  new Location(75, 350),
				  new Location(350, 75),
				  new Location(625, 350)
		  };	
	  private final Location[] scoreLocations = {
				  new Location(575, 675),
				  new Location(25, 575),
				  new Location(575, 25),
				  new Location(650, 575)
		  };
	  private Actor[] scoreActors = {null, null, null, null };
	  private final Location trickLocation = new Location(350, 350);
	  private final Location textLocation = new Location(350, 450);
	  private final int thinkingTime = 2000;
	  private Hand[] hands;
	  private Location hideLocation = new Location(-500, - 500);
	  private Location trumpsActorLocation = new Location(50, 50);
	  
	
	
	  public void setStatus(String string) { setStatusText(string); }
	  
	private int[] scores = new int[nbPlayers];
	
	Font bigFont = new Font("Serif", Font.BOLD, 36);
	
	private void notifyObservers(Card card, Suit lead, Suit trump, Card winningCard) {
		for(Observer observer:observers) {
			observer.addCurrentCard(card);
			observer.tableInfo(winningCard, lead, trump);
		}
	}
	
	private void initScore() {
		int Human = gameProperties.getHuman();
		int NPC_random = gameProperties.getNPC_random();
		int NPC_legal = gameProperties.getNPC_legal();
		int NPC_smart = gameProperties.getNPC_smart();
	
		 for (int i = 0; i < nbPlayers; i++) {
			 scores[i] = 0;
			 if(Human>0 && i==0) {
				 players[i] = new Player("human", random);
			 }else if (NPC_random > 0) {
				 players[i] = new Player("random", random);
				 NPC_random-=1;
			 }else if (NPC_smart > 0) {
				 players[i] = new Player("smart", random);
				 NPC_smart-=1;
				 System.out.println("SMART: "+i);
			 }else if (NPC_legal > 0) {
				 players[i] = new Player("legal", random);
				 NPC_legal-=1;
			 }
			 observers.add(players[i]);
			 
			 scoreActors[i] = new TextActor("0", Color.WHITE, bgColor, bigFont);
			 addActor(scoreActors[i], scoreLocations[i]);
		 }
	  }
	
	private void updateScore(int player) {
		removeActor(scoreActors[player]);
		scoreActors[player] = new TextActor(String.valueOf(scores[player]), Color.WHITE, bgColor, bigFont);
		addActor(scoreActors[player], scoreLocations[player]);
	}
	
	private Card selected;
	
	private void initRound() {
		int nbStartCards = gameProperties.getNbStartCards();
		int Human = gameProperties.getHuman();

		 hands = deck.dealingOut(nbPlayers, nbStartCards); // Last element of hands is leftover cards; these are ignored
		 for (int i = 0; i < nbPlayers; i++) {
				   hands[i].sort(Hand.SortType.SUITPRIORITY, true);
				   players[i].setHand(hands[i]);
			 }

		 if(Human>0) {
			 // Set up human player for interaction
			CardListener cardListener = new CardAdapter()  // Human Player plays card
				    {
				      public void leftDoubleClicked(Card card) { selected = card; hands[0].setTouchEnabled(false); }
				    };
			hands[0].addCardListener(cardListener);
		 }
		 
		 // graphics
	    RowLayout[] layouts = new RowLayout[nbPlayers];
	    for (int i = 0; i < nbPlayers; i++) {
	      layouts[i] = new RowLayout(handLocations[i], handWidth);
	      layouts[i].setRotationAngle(90 * i);
	      // layouts[i].setStepDelay(10);
	      hands[i].setView(this, layouts[i]);
	      hands[i].setTargetArea(new TargetArea(trickLocation));
	      hands[i].draw();
	    }
//	    for (int i = 1; i < nbPlayers; i++)  // This code can be used to visually hide the cards in a hand (make them face down)
//	      hands[i].setVerso(true);
	    // End graphics
	 }
	
	private Optional<Integer> playRound() {  // Returns winner, if any
		// Select and display trump suit
		final Suit trumps = randomEnum(Suit.class);
		final Actor trumpsActor = new Actor("sprites/"+trumpImage[trumps.ordinal()]);
		int nbStartCards = gameProperties.getNbStartCards();
		int Human = gameProperties.getHuman();
		boolean enforceRules = gameProperties.isEnforceRules();
		int winningScore = gameProperties.getWinningScore();
		
		
		
		
		addActor(trumpsActor, trumpsActorLocation);
		// End trump suit
		Hand trick;
		int winner;
		Card winningCard;
		Suit lead;
		
		
	
		int nextPlayer = random.nextInt(nbPlayers); // randomly select player to lead for this round
		for (int i = 0; i < nbStartCards; i++) {
			trick = new Hand(deck);
		
	    	selected = null;

	        if (Human > 0 && 0 == nextPlayer) {  // Select lead depending on player type
	    		hands[0].setTouchEnabled(true);
	    		setStatus("Player 0 double-click on card to lead.");
	    		while (null == selected) delay(100);
	    		
	        } else {
	    		setStatusText("Player " + nextPlayer + " thinking...");
	            delay(thinkingTime);
	            selected = players[nextPlayer].getCard();

	        }

	        
	        
	       
	        // Lead with selected card
		        trick.setView(this, new RowLayout(trickLocation, (trick.getNumberOfCards()+2)*trickWidth));
				trick.draw();
				selected.setVerso(false);
				// No restrictions on the card being lead
				lead = (Suit) selected.getSuit();

				selected.transfer(trick, true); // transfer to trick (includes graphic effect)
				winner = nextPlayer;
				winningCard = selected;
				
				notifyObservers(selected, lead, trumps, winningCard);
			// End Lead
			for (int j = 1; j < nbPlayers; j++) {
				if (++nextPlayer >= nbPlayers) nextPlayer = 0;  // From last back to first
				selected = null;

		        if (0 == nextPlayer && Human > 0) {
		    		hands[0].setTouchEnabled(true);
		    		setStatus("Player 0 double-click on card to follow.");
		    		while (null == selected) delay(100);
		        } else {
			        setStatusText("Player " + nextPlayer + " thinking...");
			        delay(thinkingTime);
			        selected = players[nextPlayer].getCard();
		        }
		        notifyObservers(selected, lead, trumps, winningCard);
	//	        gameInfo.getCurrentlyPlayed().setView(this, null);
		        
		        // Follow with selected card
			        trick.setView(this, new RowLayout(trickLocation, (trick.getNumberOfCards()+2)*trickWidth));
					trick.draw();
					selected.setVerso(false);  // In case it is upside down
					// Check: Following card must follow suit if possible
						if (selected.getSuit() != lead && hands[nextPlayer].getNumberOfCardsWithSuit(lead) > 0) {
							 // Rule violation
							 String violation = "Follow rule broken by player " + nextPlayer + " attempting to play " + selected;
							 System.out.println(violation);
							 if (enforceRules) 
								 try {
									 throw(new BrokeRuleException(violation));
									} catch (BrokeRuleException e) {
										e.printStackTrace();
										System.out.println("A cheating player spoiled the game!");
										System.exit(0);
									}  
						 }
					// End Check
					 selected.transfer(trick, true); // transfer to trick (includes graphic effect)
					 System.out.println("winning: suit = " + winningCard.getSuit() + ", rank = " + winningCard.getRankId());
					 System.out.println(" played: suit = " +    selected.getSuit() + ", rank = " +    selected.getRankId());
					 if ( // beat current winner with higher card
						 (selected.getSuit() == winningCard.getSuit() && rankGreater(selected, winningCard)) ||
						  // trumped when non-trump was winning
						 (selected.getSuit() == trumps && winningCard.getSuit() != trumps)) {
						 System.out.println("NEW WINNER");
	//					 System.out.println("next");
						 winner = nextPlayer;
						 winningCard = selected;
						 notifyObservers(selected, lead, trumps, winningCard);
					 }
				// End Follow
			}
	
			delay(600);
			trick.setView(this, new RowLayout(hideLocation, 0));
			trick.draw();		
			nextPlayer = winner;
			setStatusText("Player " + nextPlayer + " wins trick.");
			scores[nextPlayer]++;
			updateScore(nextPlayer);
			if (winningScore == scores[nextPlayer]) return Optional.of(nextPlayer);
		}
		removeActor(trumpsActor);
		return Optional.empty();
	}
	
	  public Whist()
	  {
	    super(700, 700, 30);
		String version = gameProperties.getVersion();
	    setTitle("Whist (V" + version + ") Constructed for UofM SWEN30006 with JGameGrid (www.aplu.ch)");
	    setStatusText("Initializing...");
	    initScore();
	    Optional<Integer> winner;
	    do { 
	      initRound();
	      winner = playRound();
	    } while (!winner.isPresent());
	    addActor(new Actor("sprites/gameover.gif"), textLocation);
	    setStatusText("Game over. Winner is player: " + winner.get());
	    refresh();
	  }
	
	  @SuppressWarnings("resource")
	public static void main(String[] args) throws IOException
	  {
	  
		
		System.out.println("Choose properties (original/legal/smart): ");
		Scanner myObj = new Scanner(System.in); 
		String property = myObj.nextLine();
	  	String fileName ="whist/"+property+".properties";
	  	
	  	gameProperties = new GameProperties(fileName);
	
	    new Whist();
	  }

}
