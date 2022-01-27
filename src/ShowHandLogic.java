import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShowHandLogic implements Service {
	static private ShowHandLogic instance=null;
	private GuiController guiController =null;
	// private AIController aiController = null;
	final int initialInventory = 500;
	final int noOfPlayers = 2;
	
	
	List<Card> drawCards = new ArrayList<Card>();
	List<Card>[] playersCards = new List[noOfPlayers];
	int[] bets = new int[noOfPlayers];
	int[] inventories = new int[noOfPlayers];
	
	boolean[] playersDone = new boolean[noOfPlayers];
	
	public ShowHandLogic() {
		for (int i=0;i<2;i++) {
			playersCards[i] = new ArrayList<Card>();
		}
	}
	
	static public ShowHandLogic getInstance(GuiController controller) {
		getInstance().guiController = controller;
		return instance;
	}
	
	static public ShowHandLogic getInstance() {
		if (instance == null) {
			instance = new ShowHandLogic();
			ShowHandStrategy.getInstance(instance);
		}
		return instance;
	}

	@Override
	public void newGame() {
		// TODO Auto-generated method stub
		for (int i=0;i<bets.length;i++) {
			bets[i] = 0;
		}
		
		for (int i=0;i<inventories.length;i++) {
			inventories[i] = initialInventory;
		}
		newRound();
	}

	@Override
	public void newRound() {
		// TODO Auto-generated method stub
		this.drawCards.clear();
		for (List<Card> cards:this.playersCards) {
			cards.clear();
		}
		
		char[] suites = {'s','h','d','c'};
		for (int i=1;i<=13;i++) {
			for (char suite:suites ) {
				Card card = new Card(i,suite);
				drawCards.add(card);
			}
		}
		
		for (int i=0;i<playersDone.length;i++) {
			playersDone[i]=false;
		}
		shuffleCards();
		
		
	}

	private void shuffleCards() {
		// TODO Auto-generated method stub
		List<Card> cards = new ArrayList<>();
		Random  rand = new Random();
		int loops = this.drawCards.size();
		for (int i=0;i<loops;i++) {
			int idx = rand.nextInt(this.drawCards.size());
			cards.add(drawCards.get(idx));
			drawCards.remove(idx);
		}
		
		this.drawCards = cards;
	}

	@Override
	public Card draw(int player) {
		// TODO Auto-generated method stub
		Card card = this.drawCards.get(0);
		this.playersCards[player].add(card);
		this.drawCards.remove(0);
		this.guiController.displayCards(player, (Card[])this.playersCards[player].toArray());
		return card;
	}

	@Override
	public void bet(int amount, int player) {
		// TODO Auto-generated method stub
		bets[player] = bets[player]+amount;
	}

	@Override
	public void giveUp(int player) {
		// TODO Auto-generated method stub
		this.inventories[player] -= this.bets[player];
		this.inventories[0] += this.bets[player];
		this.bets[player]=0;
		
	}

	@Override
	public void done(int player) {
		// TODO Auto-generated method stub
		this.playersDone[player] = true;
		boolean allDone = true;
		int nextPlayer = 0;
		for (int i=0;i<this.playersDone.length;i++) {
			if (this.playersDone[(i+player)%noOfPlayers]==false) {
				allDone = false;
				break;
			}
		}
		
		if (allDone) {
			completeRound();
		} else {
			if (nextPlayer == 0) {
				ShowHandStrategy.getInstance().requireDraw(this.playersCards[nextPlayer]);
			}
		}
		
	}

	public GuiController getGuiController() {
		// TODO Auto-generated method stub
		return this.guiController;
	}
}
