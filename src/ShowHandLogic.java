import java.util.ArrayList;
import java.util.Collections;
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
		
		
		for (int i=0;i<inventories.length;i++) {
			inventories[i] = initialInventory;
			
		}

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
		
		for (int i=0;i<bets.length;i++) {
			bets[i] = 0;
			this.guiController.displayBets(i, bets[i]);
		}
		
		for (int i=0;i<inventories.length;i++) {
			this.guiController.displayInventories(i, inventories[i]);
		}
		
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
		this.guiController.displayCards(player, this.playersCards[player]);
		return card;
	}

	@Override
	public void bet(int player, int amount) {
		// TODO Auto-generated method stub
		bets[player] = bets[player]+amount;
		inventories[player] -= amount;
		this.guiController.displayBets(player, bets[player]);
		this.guiController.displayInventories(player, inventories[player]);
		
	}

	@Override
	public void giveUp(int player) {
		// TODO Auto-generated method stub
		this.playersDone[player]=true;
		if (player!=0) {
			
			this.inventories[0] += this.bets[player];
			this.bets[player]=0;
			
			this.done(player);
		} else {
			for (int i=1;i<noOfPlayers;i++) {
				inventories[i]+=bets[i]*2;
				inventories[0]-=bets[i];
				bets[i]=0;
			}
			
			for (int i=0;i<noOfPlayers;i++) {
				this.guiController.displayInventories(i, inventories[i]);
			}
			this.guiController.roundDone();
			for (int i=0;i<inventories.length;i++) {
				if (inventories[i] <= 0) {
					this.guiController.gameOver(i);
				}
			}
		}
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
				nextPlayer = (i+player)%noOfPlayers;
				break;
			}
		}
		
		if (allDone) {
			completeRound();
		} else {
			if (nextPlayer == 0) {
				while (!this.playersDone[nextPlayer])
					ShowHandStrategy.getInstance().requireDraw(this.playersCards[nextPlayer]);
			}
		}
		
	}

	private void completeRound() {
		// TODO Auto-generated method stub
		for (int i=0;i<this.playersCards.length;i++) {
			List<Card> cards = this.playersCards[i];
			this.guiController.displayCards(i, cards);
		}

		int hostPoints = getMaxPoints(this.playersCards[0]);
		inventories[0]+=bets[0];
		
		for (int i=1;i<noOfPlayers;i++) {
			int points = getMaxPoints(this.playersCards[i]);
			if (hostPoints<points) {
				this.inventories[i]+=this.bets[i]*2;
				this.inventories[0]-=this.bets[i];
			} else {
				this.inventories[0]+=this.bets[i];
			}
		}
		for (int i=0;i<noOfPlayers;i++) {
			this.guiController.displayInventories(i, inventories[i]);
		}
		this.guiController.roundDone();
		for (int i=0;i<inventories.length;i++) {
			if (inventories[i] <= 0) {
				this.guiController.gameOver(i);
			}
		}
		
	}

	private int getMaxPoints(List<Card> cards) {
		// TODO Auto-generated method stub
		List<Integer> possiblePoints = getPossiblePoints(cards);
		if (possiblePoints.isEmpty()) return 0;
		return possiblePoints.get(0);
	}

	public GuiController getGuiController() {
		// TODO Auto-generated method stub
		return this.guiController;
	}
	
	public List<Integer> getPossiblePoints(List<Card> cards) {
		// TODO Auto-generated method stub
		List<Integer> pointsList = new ArrayList<>();
		int A_s = 0;
		int points = 0;
		for (Card card:cards) {
			if (card.value == 1) {
				A_s++;
			} else if (card.value<10) {
				points+=card.value;
			} else {
				points+=10;
			}
		}
		
		for (int i=0;i<=A_s;i++) {
			int p = points+i+(A_s-i)*11;
			if (p<=21)
				pointsList.add(p);
		}
		Collections.sort(pointsList,Collections.reverseOrder());
		return pointsList;
	}
}
