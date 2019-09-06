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

import java.awt.FontMetrics;
import java.awt.Graphics;
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

    private CellPrinter resultCellPrinter = null;

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

    @Override
    protected CellPrinter getCellPrinter(int column) {
        if (column == resultCol) {
            if (null == resultCellPrinter) {
                resultCellPrinter = new ResultCellPrinter(fontMetrics);
            }
            return resultCellPrinter;
        } else {
            return super.getCellPrinter(column);
        }
    }

    private static class ResultCellPrinter implements CellPrinter {
        private static final String POSSIBLE_FIRST_CHARACTERS = " 01½";
        private final int firstCharacterWidth;

        ResultCellPrinter(FontMetrics fm) {
            int w = 0;
            for (int i = 0; i < POSSIBLE_FIRST_CHARACTERS.length(); i++) {
                w = Math.max(w, fm.stringWidth(POSSIBLE_FIRST_CHARACTERS.substring(i, i+1)));
            }
            firstCharacterWidth = w;
        }

        @Override
        public void printCell(Graphics g, int x, int y, String s) {
            g.drawString(s.substring(1), x + firstCharacterWidth, y);
            s = s.substring(0, 1);
            int w = g.getFontMetrics().stringWidth(s);
            g.drawString(s, x + firstCharacterWidth - w, y);
        }
    }
}
