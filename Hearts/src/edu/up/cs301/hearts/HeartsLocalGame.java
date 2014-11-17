package edu.up.cs301.hearts;

import java.util.ArrayList;
import java.util.Collections;

import edu.up.cs301.card.Card;
import edu.up.cs301.card.Suit;
import edu.up.cs301.game.Game;
import edu.up.cs301.game.GamePlayer;
import edu.up.cs301.game.LocalGame;
import edu.up.cs301.game.actionMsg.GameAction;
import edu.up.cs301.game.infoMsg.NotYourTurnInfo;

public class HeartsLocalGame extends LocalGame implements Game {

	HeartsState state;
	Card[] deck;
	int turnIdx;

	public HeartsLocalGame() {
		super();
		int count = 0;
		deck = new Card[52];
		String suit = null;
		String rank;
		String newCard;
		for (int i = 0; i < 4; i++) {
			switch (i) {
			case 0:
				suit = "H";
				break;

			case 1:
				suit = "S";
				break;

			case 2:
				suit = "D";
				break;

			case 3:
				suit = "C";
				break;
			}
			for (int j = 2; j < 15; j++) {
				switch (j) {
				case 10:
					rank = "T";
					break;
				case 11:
					rank = "J";
					break;
				case 12:
					rank = "Q";
					break;
				case 13:
					rank = "K";
					break;
				case 14:
					rank = "A";
					break;
				default:
					rank = "" + j;
					break;
				}
				newCard = rank + suit;
				deck[count] = Card.fromString(newCard);
				count++;
			}
		}
		Card[][] deal = createNewDeal();
		state = new HeartsState(deal, new int[4], new int[4], new Card[4], false);
	}

	@Override
	protected void sendUpdatedStateTo(GamePlayer p) {
		if (p == null) {
			return;
		}
		int playerIdx = 0;
		for (int i = 0; i < players.length; i++) {
			if (players[i] == p) {
				playerIdx = i;
			}
		}
		p.sendInfo(new HeartsState(state,playerIdx));
	}

	@Override
	protected boolean canMove(int playerIdx) {
		if (playerIdx == turnIdx) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected String checkIfGameOver() {
		for (int i = 0; i < state.getNumPlayers(); i++) {
			if (state.getOverallScore(i) >= 100) {
				return this.playerNames[i] + " is teh winrar";
			}
		}
		return null;
	}

	@Override
	protected boolean makeMove(GameAction action) {
		// TODO Auto-generated method stub
		GamePlayer p;
		boolean tf;
		if (action instanceof HeartsPlayAction) {
			HeartsPlayAction act = (HeartsPlayAction) action;
			p = action.getPlayer();
			for (int i = 0; i < players.length; i++) {
				if (players[i].equals(p)) {
					if (canMove(i) == true) {
						Card[] trick = state.getCurrentTrick();
						Suit ledSuit = null;
						for (int j = 0; j < trick.length; j++) {
							if (trick[j] == null) {
								if (isValidPlay(act.getPlayedCard(),i,ledSuit)) {
									tf = state.addCardToTrick(act.PlayedCard);
									if(tf == true){
										sendUpdatedStateTo(p);
										return true;
									}
									else{
										return false;
									}
								}
								break;
							}
							else if (j == 0) {
								ledSuit = act.getPlayedCard().getSuit();
							}
						}
						setTurnIdx(-1);
						return true;
					}
					break;
				}
			}
			p.sendInfo(new NotYourTurnInfo());
		}
		
		return false;
	}
	
	private Card[][] createNewDeal() {
		Card[][] deal = new Card[4][13];
		//essentially, pick a random order to attempt dealing cards in
		ArrayList<Integer> order = new ArrayList<Integer>();
		order.add(0); order.add(1); order.add(2); order.add(3);
		for (int card = 0; card < deck.length; card++) {
			Collections.shuffle(order);
			for (int player: order) {
				int i = 0;
				while (i < 13 && deal[player][i] != null) {
					i++;
				}
				if (i < 13) {
					deal[player][i] = deck[card];
					break;
				}
				//otherwise, this player's hand is full, try the next one
			}
		}
		return deal;
	}

	public void setTurnIdx(int i){
		if(i == -1){
			turnIdx++;
			turnIdx = turnIdx % 4;
		}
		else if(i < 4){
			turnIdx = i;
		}
	}
	
	private boolean isValidPlay(Card c, int idx, Suit ledSuit) {
		ArrayList<Card> playersHand = state.getPlayerHand(idx);
		if (playersHand.contains(c)) {
			for (Card test: playersHand) {
				if (test.getSuit().equals(ledSuit)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
