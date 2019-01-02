/*
 * This file is part of OpenGotha.
 *
 * OpenGotha is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * OpenGotha is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenGotha. If not, see <http://www.gnu.org/licenses/>.
 */

package ru.gofederation.gotha.model.rgf;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

import info.vannier.gotha.Game;
import info.vannier.gotha.Player;

public final class RgfGame {
    @SerializedName("Player1")
    public int player1;
    @SerializedName("Player2")
    public int player2;
    @SerializedName("Color")
    public RgfGameColor color;
    @SerializedName("Result")
    public RgfGameResult result;
    @SerializedName("Round")
    public int round;
    @SerializedName("Board")
    public int board;
    @SerializedName("Komi")
    public float komi;
    @SerializedName("Handicap")
    public int handicap;

    public RgfGame(Game game, Map<Player, RgfPlayer> playersMap, RgfTournament tournament) {
        if (playersMap.containsKey(game.getBlackPlayer()))
            player1 = playersMap.get(game.getBlackPlayer()).id;
        if (playersMap.containsKey(game.getWhitePlayer()))
            player2 = playersMap.get(game.getWhitePlayer()).id;
        color = game.isKnownColor() ? RgfGameColor.PLAYER_1_BLACK : RgfGameColor.UNKNOWN;
        result = RgfGameResult.fromGothaCode(game.getResult());
        round = game.getRoundNumber();
        board = game.getTableNumber();
        komi = tournament.komi;
        handicap = game.getHandicap();
    }

    public Game toGothaGame(Map<Integer, Player> playersMap) {
        Game game = new Game();
        game.setBlackPlayer(playersMap.get(player1));
        game.setWhitePlayer(playersMap.get(player2));
        game.setHandicap(handicap);
        game.setKnownColor(RgfGameColor.PLAYER_1_BLACK == color);
        game.setResult(result.getGothaCode());
        game.setRoundNumber(round);
        game.setTableNumber(board);
        return game;
    }
}
