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

import info.vannier.gotha.DPParameterSet;
import info.vannier.gotha.GeneralParameterSet;
import info.vannier.gotha.PlacementParameterSet;
import info.vannier.gotha.ScoredPlayer;
import info.vannier.gotha.TournamentInterface;
import info.vannier.gotha.TournamentParameterSet;
import ru.gofederation.gotha.model.PlacementCriterion;
import ru.gofederation.gotha.util.GothaLocale;
import ru.gofederation.gotha.util.ScoreDisplayKt;

import java.awt.print.Printable;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StandingsPrinter extends TablePrinter implements Printable {
    private final List<ru.gofederation.gotha.model.ScoredPlayer> playerList;
    private final GothaLocale locale;
    private final GeneralParameterSet gps;
    private final int round;
    private final String[][] games;
    private final String[] places;
    private final List<PlacementCriterion> placementCriteria;

    private final int numberCol;
    private final int placeCol;
    private final int nameCol;
    private final int rankCol;
    private final int countryCol;
    private final int clubCol;
    private final int nbwCol;
    private final int firstRoundCol;
    private final int lastRoundCol;
    private final int firstPlaCritCol;
    private final int lastPlaCritCol;
    private final int columnCount;

    public StandingsPrinter(TournamentInterface tournament, TournamentParameterSet tps, int round) throws RemoteException {
        PlacementParameterSet pps = tps.getPlacementParameterSet();
        DPParameterSet dps = tps.getDPParameterSet();

        this.locale = GothaLocale.getCurrentLocale();
        this.gps = tps.getGeneralParameterSet();
        this.round = round;
        this.playerList = tournament.orderedScoredPlayersList(round, pps);
        this.games = ScoredPlayer.halfGamesStrings(this.playerList, round, tournament, dps.getGameFormat() == DPParameterSet.DP_GAME_FORMAT_FULL);
        this.places = ScoredPlayer.catPositionStrings(this.playerList, round, tps);
        this.placementCriteria = Arrays.stream(pps.getPlaCriteria())
            .filter(placementCriterion -> placementCriterion != PlacementCriterion.NUL)
            .collect(Collectors.toList());

        int n = 0;
        if (dps.isDisplayNumCol()) this.numberCol = n++;
        else                       this.numberCol = -1;
        if (dps.isDisplayPlCol())  this.placeCol = n++;
        else                       this.placeCol = -1;
        this.nameCol = n++;
        this.rankCol = n++;
        if (dps.isDisplayCoCol())  this.countryCol = n++;
        else                       this.countryCol = -1;
        if (dps.isDisplayClCol())  this.clubCol = n++;
        else                       this.clubCol = -1;
        this.nbwCol = n++;
        this.firstRoundCol = n;
        this.lastRoundCol = (n += round); n++;
        this.firstPlaCritCol = n;
        this.lastPlaCritCol = (n += this.placementCriteria.size());
        this.columnCount = n;
    }

    @Override
    int getColumnCount() {
        return columnCount;
    }

    @Override
    int getRowCount() {
        return playerList.size();
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
        s = locale.format("printing.standings.header", round + 1);
        w = fontMetrics.stringWidth(s);
        graphics.drawString(s, (int) (pageFormat.getImageableWidth() / 2 - w / 2), fontMetrics.getHeight() * 2);
    }

    @Override
    String getHeader(int column) {
        if (column == numberCol) {
            return locale.getString("nr");
        } else if (column == nameCol) {
            return locale.getString("player.participant");
        } else if (column == countryCol) {
            return locale.getString("player.country");
        } else if (column == clubCol) {
            return locale.getString("player.club");
        } else if (column == rankCol) {
            return locale.getString("player.rank_s");
        } else if (column == placeCol) {
            return locale.getString("player.pl").trim();
        } else if (column == nbwCol) {
            return locale.getString("player.nbw");
        } else if (column >= firstRoundCol && column <= lastRoundCol) {
            return "R" + (column - firstRoundCol + 1);
        } else if (column >= firstPlaCritCol && column <= lastPlaCritCol) {
            return placementCriteria.get(column - firstPlaCritCol).getShortName();
        }

        return "";
    }

    @Override
    String getCell(int row, int column) {
        if (column == numberCol) {
            return Integer.toString(row + 1);
        } else if (column == placeCol) {
            return places[row];
        } else if (column == nameCol) {
            return playerList.get(row).fullName();
        } else if (column == countryCol) {
            return playerList.get(row).getCountry();
        } else if (column == clubCol) {
            return playerList.get(row).getClub();
        } else if (column == rankCol) {
            return playerList.get(row).getRank().toString();
        } else if (column == nbwCol) {
            return ScoreDisplayKt.formatScore(playerList.get(row), PlacementCriterion.NBW, round);
        } else if (column >= firstRoundCol && column <= lastRoundCol) {
            return games[column - firstRoundCol][row].trim();
        } else if (column >= firstPlaCritCol && column <= lastPlaCritCol) {
            return ScoreDisplayKt.formatScore(playerList.get(row), placementCriteria.get(column - firstPlaCritCol), round);
        } else {
            return "";
        }
    }
}
