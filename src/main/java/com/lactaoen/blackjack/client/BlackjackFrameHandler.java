package com.lactaoen.blackjack.client;

import com.lactaoen.blackjack.model.*;
import com.lactaoen.blackjack.model.wrapper.ActionWrapper;
import com.lactaoen.blackjack.model.wrapper.BetWrapper;
import com.lactaoen.blackjack.model.wrapper.BlackjackErrorWrapper;
import com.lactaoen.blackjack.model.wrapper.GameInfoWrapper;

import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Handles all frames sent by the server, regardless of which destination
 * these messages are sent to.
 *
 * @author Chad Tomas
 */
public class BlackjackFrameHandler implements StompFrameHandler {

    private StompSession session;
    private String playerId;

    public BlackjackFrameHandler(StompSession session) {
        this.session = session;
    }

    @Override
    public Type getPayloadType(StompHeaders stompHeaders) {
        // Tries to determine the payload type based on the destination of the message
        // You can add more cases below if you want to listen to any more channels,
        // But make sure you subscribe to it first
        String destination = stompHeaders.getDestination();
        switch (destination) {
            case "/user/queue/player":
                return PlayerInfo.class;
            case "/user/queue/errors":
                return BlackjackErrorWrapper.class;
            case "/topic/game":
                return GameInfoWrapper.class;
            default:
                return Object.class;
        }
    }

    @Override
    public void handleFrame(StompHeaders stompHeaders, Object o) {
        // Figure out type of payload and react accordingly
        if (o.getClass() == PlayerInfo.class) {
            // If it gets here, we probably just registered. Store the playerId for use later in the game
            this.playerId = ((PlayerInfo) o).getPlayerId();

            // After we get the playerId, you'll probably want to check if we can bet.
            // If so, you could do that here

            //logic : add a random generator for initial bet selection, with value less than 1000 and in multiples of 10

            int max = 5;
            int min = 1;
            Random rn = new Random();
            int bet = rn.nextInt(max - min + 1) + min;

            bet = bet * 10;

            // TODO Add custom implementation below for variable bet sizes.
            session.send("/app/bet", new BetWrapper(playerId, bet));

        } else if (o.getClass() == BlackjackErrorWrapper.class) {

            //System.out.println ( BlackjackErrorWrapper.class.);
            // If it gets here, there was an error that took place. Refer to the BlackjackErrorCode
            // class in the server for the error types. You can choose to handle those errors here
            // if you so choose.

            // TODO Add custom implementation below

        } else if (o.getClass() == GameInfoWrapper.class) {
            // If it gets here, game action is taking place, so you can either send a bet or submit
            // a hand action here.

            GameInfoWrapper game = (GameInfoWrapper) o;

            if (game.getGameStatus() == Round.BETTING_ROUND) {
                session.send("/app/bet", new BetWrapper(playerId, 100));

            } else if(game.getGameStatus() == Round.HAND_IN_PROGRESS) {


                /*
                * Algorithm:
                *
                *  - for each player
                *  - get player's total hand value
                *   - if player's total hand value is less than 18
                *    - if player's hand value consists of a pair,
                *       - call action SPLIT
                *    - else
                *       - call action HIT
                *   - if player's hand value is greater than 18
                *       - call action STAND
                *
                * */


                //Dealer dealer = game.getDealer();
                //List<Hand> dealerHands = dealer.getHands();

//                int dealerHandValue = game.getDealerUpCard().getCardValue();
//
//                 for (Hand dealerHand : dealerHands) {
//                    dealerHandValue += dealerHand.getHandValue();
//                }

                Player player = game.getPlayers().get(0);


                List<Hand> playerHands = player.getHands();

                int playerHandValue = 0;

                for (Hand playerHand : playerHands) {

                    playerHandValue += playerHand.getHandValue();

                }



                if( playerHandValue < 18) {


                   // System.out.println("HIT");
                    ActionWrapper actionWrapper = new ActionWrapper(player.getPlayerId(), 0, Action.HIT);
                    session.send("/app/action", actionWrapper);


                } else {


                    //System.out.println("STAND");
                    ActionWrapper actionWrapper = new ActionWrapper(player.getPlayerId(), 0, Action.STAND);
                    session.send("/app/action", actionWrapper);

                }

//
//                List<Player> players = game.getPlayers();
//
//                for (Player player : players) {
//
//                    List<Hand> playerHands = player.getHands();
//
//                    int playerHandValue = 0;
//
//                    for (Hand playerHand : playerHands) {
//
//                        playerHandValue += playerHand.getHandValue();
//
//                    }
//
//                    if( playerHandValue < dealerHandValue) {
//
//                        ActionWrapper actionWrapper = new ActionWrapper(player.getPlayerId(), 0, Action.HIT);
//                        session.send("/app/action", actionWrapper);
//
//
//                    } else {
//
//
//                        ActionWrapper actionWrapper = new ActionWrapper(player.getPlayerId(), 0, Action.STAND);
//                        session.send("/app/action", actionWrapper);
//
//                    }
//
//
//
//
//                }


                // TODO Change the action being sent to be based off your current hand.
                ActionWrapper actionWrapper = new ActionWrapper(playerId, 0, Action.STAND);
                session.send("/app/action", actionWrapper);
            }

        } else {
            // Shouldn't get here unless you subscribed to a destination other than the 3 we
            // subscribed to by default. In case you added any, that would be handled here.

            // TODO Add custom implementation below

        }
    }
}
