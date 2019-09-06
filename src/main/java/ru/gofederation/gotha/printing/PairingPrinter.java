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

package ru.gofederation.gotha.printing;

import java.rmi.RemoteException;
import java.util.List;

import info.vannier.gotha.Game;
import info.vannier.gotha.GeneralParameterSet;
import info.vannier.gotha.TournamentInterface;

public class PairingPrinter extends TablePrinter {
    private final int boardCol = 0;
    private final int blackCol = 1;
    private final int whiteCol = 2;
    private final int handicapCol = 3;
    private final int resultCol = 4;

    private final GeneralParameterSet gps;
    private final List<Game> games;
    private final int round;

    private PairingPrinter(TournamentInterface tournament, int round) throws RemoteException {
        this.gps = tournament.getTournamentParameterSet().getGeneralParameterSet();
        this.games = tournament.gamesList(round);
        this.round = round;
    }

    public static void print(TournamentInterface tournament, int round) throws RemoteException {
        new PairingPrinter(tournament, round).print();
    }

    @Override
    int getColumnCount() {
        return 5;
    }

    @Override
    int getRowCount() {
        return games.size();
    }

    @Override
    String getHeader(int column) {
        switch (column) {
            case boardCol: return locale.getString("game.board");
            case blackCol: return locale.getString("game.black");
            case whiteCol: return locale.getString("game.white");
            case handicapCol: return locale.getString("game.handicap");
            case resultCol: return locale.getString("game.result");
            default: return "";
        }
    }

    @Override
    String getCell(int row, int column) {
        switch (column) {
            case boardCol: return Integer.toString(row + 1);
            case blackCol: return games.get(row).getBlackPlayer().fullName();
            case whiteCol: return games.get(row).getWhitePlayer().fullName();
            case handicapCol: return Integer.toString(games.get(row).getHandicap());
            case resultCol: return games.get(row).resultAsString(whiteCol < blackCol);
            default: return "";
        }
    }

    @Override
    protected int headerHeight() {
        return (int) (fontMetrics.getHeight() * 2.5);
    }

    @Override
    protected void printHeader() {
        String s = gps.getName();
        int w = fontMetrics.stringWidth(s);
        graphics.drawString(s, (int) (pageFormat.getImageableWidth() / 2 - w / 2), fontMetrics.getHeight());
        s = locale.format("printing.games.header", round + 1);
        w = fontMetrics.stringWidth(s);
        graphics.drawString(s, (int) (pageFormat.getImageableWidth() / 2 - w / 2), fontMetrics.getHeight() * 2);
    }
}
