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

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import info.vannier.gotha.GeneralParameterSet;
import info.vannier.gotha.PlacementParameterSet;
import info.vannier.gotha.Player;
import info.vannier.gotha.PlayerException;
import info.vannier.gotha.ScoredPlayer;
import info.vannier.gotha.Tournament;
import info.vannier.gotha.TournamentException;
import info.vannier.gotha.TournamentInterface;
import info.vannier.gotha.TournamentParameterSet;
import ru.gofederation.gotha.model.Rating;
import ru.gofederation.gotha.model.RatingOrigin;
import ru.gofederation.gotha.util.GsonDateAdapter;

public final class RgfTournament {
    public static final int IMPORT_MODE_APPLICATIONS = 0;
    public static final int IMPORT_MODE_WALLIST = 1;

    @SerializedName("Id")
    public int id;
    @SerializedName("Name")
    public String name;
    @SerializedName("Location")
    public String location;
    @SerializedName("StartDate")
    @JsonAdapter(value = GsonDateAdapter.class)
    public Date startDate;
    @SerializedName("EndDate")
    @JsonAdapter(value = GsonDateAdapter.class)
    public Date endDate;
    @SerializedName("System")
    public String system;
    @SerializedName("RoundCount")
    public int roundCount;
    @SerializedName("Komi")
    public float komi;
    @SerializedName("State")
    public RgfTournamentState state;
    @SerializedName("Category")
    public String category;
    @SerializedName("Timing")
    public Timing timing;
    @SerializedName("Applications")
    public Set<RgfTournamentPlayerApplication> applications;
    @SerializedName("Players")
    public Set<RgfPlayer> players;
    @SerializedName("Games")
    public Set<RgfGame> games;
    @SerializedName("ApplicationsCount")
    public int applicationsCount;
    @SerializedName("ParticipantsCount")
    public int participantsCount;

    public RgfTournament(TournamentInterface tournament) throws RemoteException {
        TournamentParameterSet tps = tournament.getTournamentParameterSet();
        GeneralParameterSet gps = tps.getGeneralParameterSet();
        PlacementParameterSet pps = tps.getPlacementParameterSet();

        id = gps.hasRgfId() ? gps.getRgfId() : 0;
        name = gps.getName();
        location = gps.getLocation();
        startDate = gps.getBeginDate();
        endDate = gps.getEndDate();
        // TODO system = ...
        roundCount = gps.getNumberOfRounds();

        players = new HashSet<>();
        Map<Player, RgfPlayer> playersMap = new HashMap<>();
        int id = 1;
        List<ScoredPlayer> scoredPlayers = tournament.orderedScoredPlayersList(gps.getNumberOfRounds() - 1, pps);
        for (int i = 0; i < scoredPlayers.size(); i++) {
            ScoredPlayer player = scoredPlayers.get(i);
            RgfPlayer rgfPlayer = new RgfPlayer(player, gps, i + 1);
            rgfPlayer.id = id++;
            players.add(rgfPlayer);
            playersMap.put(tournament.getPlayerByKeyString(player.getKeyString()), rgfPlayer);
        }

        games = new HashSet<>();
        for (info.vannier.gotha.Game game : tournament.gamesList()) {
            RgfGame rgfGame = new RgfGame(game, playersMap, this);
            games.add(rgfGame);
        }
    }

    public RgfTournamentImportReport toGothaTournament(int importMode) throws PlayerException, RemoteException {
        Tournament tournament = new Tournament();
        TournamentParameterSet tps = tournament.getTournamentParameterSet();
        tps.initForMM(); // TODO
        GeneralParameterSet gps = tps.getGeneralParameterSet();
        gps.setName(name);
        gps.setShortName(name); // TODO actually shorten the name
        gps.setBeginDate(startDate);
        gps.setEndDate(endDate);
        gps.setRgfId(id);

        RgfTournamentImportReport report = new RgfTournamentImportReport();

        if (importMode == IMPORT_MODE_APPLICATIONS) {
            if (null != applications) {
                for (RgfTournamentPlayerApplication application : applications) {
                    info.vannier.gotha.Player.Builder builder = new info.vannier.gotha.Player.Builder()
                        .setFirstName(application.firstName)
                        .setName(application.lastName);
                    if (application.rgfId > 0)
                        builder.setRgfId(application.rgfId);
                        if (null != application.rating)
                            try {
                                int rating = Rating.clampRating(RatingOrigin.RGF, Integer.parseInt(application.rating));
                                builder.setRating(rating, RatingOrigin.RGF)
                                       .setRank(Rating.ratingToRank(RatingOrigin.RGF, rating));
                            } catch (NumberFormatException e) {
                                // Noop is OK
                            }
                    try {
                        tournament.addPlayer(builder.build());
                        report.players += 1;
                    } catch (TournamentException e) {
                        report.reportBuilder.append(e.getMessage()).append("\n");
                        report.hadError = true;
                    }
                }
            }
        } else if (importMode == IMPORT_MODE_WALLIST) {
            if (null != players) {
                Map<Integer, info.vannier.gotha.Player> playersMap = new HashMap<>();
                for (RgfPlayer player : players) {
                    info.vannier.gotha.Player gothaPlayer = player.toGothaPlayer();
                    playersMap.put(player.id, gothaPlayer);
                    try {
                        tournament.addPlayer(gothaPlayer);
                        report.players += 1;
                    } catch (TournamentException e) {
                        report.reportBuilder.append(e.getMessage()).append("\n");
                        report.hadError = true;
                    }
                }

                if (null != games) {
                    for (RgfGame game : games) {
                        info.vannier.gotha.Game gothaGame = game.toGothaGame(playersMap);
                        try {
                            tournament.addGame(gothaGame);
                            report.games += 1;
                        } catch (TournamentException e) {
                            report.reportBuilder.append(e.getMessage()).append("\n");
                            report.hadError = true;
                        }
                    }
                }
            }
        }

        report.tournament = tournament;

        return report;
    }

    public static final class Timing {
        @SerializedName("Base")
        public int base;
        @SerializedName("BoyomiType")
        public String boyomiType;
    }
}