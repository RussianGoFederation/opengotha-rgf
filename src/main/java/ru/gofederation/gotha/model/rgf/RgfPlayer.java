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

import java.util.Date;

import info.vannier.gotha.GeneralParameterSet;
import info.vannier.gotha.PlacementCriterion;
import info.vannier.gotha.PlayerException;
import info.vannier.gotha.ScoredPlayer;
import ru.gofederation.gotha.model.PlayerRegistrationStatus;
import ru.gofederation.gotha.model.RatingOrigin;
import ru.gofederation.gotha.util.GsonDateAdapter;

public final class RgfPlayer {
    @SerializedName("id")
    public int id;
    @SerializedName("playerId")
    public int playerId;
    @SerializedName("firstName")
    public String firstName;
    @SerializedName("lastName")
    public String lastName;
    @SerializedName("patronymic")
    public String patronymic;
    @SerializedName("dateOfBirth")
    @JsonAdapter(value = GsonDateAdapter.class)
    public Date dateOfBirth;
    @SerializedName("mm0")
    public int mm0;
    @SerializedName("mmF")
    public int mmf;
    @SerializedName("sos")
    public int sos4;
    @SerializedName("sodos")
    public int sodos4;
    @SerializedName("rating")
    public int rating;
    @SerializedName("place")
    public int place;

    public RgfPlayer(ScoredPlayer player, GeneralParameterSet gps, int place) {
        playerId = player.getRgfId() > 0 ? player.getRgfId() : -1;
        firstName = player.getFirstName();
        lastName = player.getName();
        patronymic = player.getPatronymic();
        dateOfBirth = player.getDateOfBirth();
        rating = player.getRating();
        mm0 = player.smms(gps);
        this.place = place;

        int finalRound = gps.getNumberOfRounds() - 1;
        mmf = getCoef(player, finalRound, PlacementCriterion.MMS);
        sos4 = getCoef(player, finalRound, PlacementCriterion.SOSM);
        sodos4 = getCoef(player, finalRound, PlacementCriterion.SODOSM);
    }

    private static int getCoef(ScoredPlayer player, int round, PlacementCriterion crit) {
        return 4 * player.getCritValue(crit, round) / crit.getCoef();
    }

    public info.vannier.gotha.Player toGothaPlayer() throws PlayerException {
        return new info.vannier.gotha.Player.Builder()
            .setRgfId(playerId)
            .setName(lastName)
            .setFirstName(firstName)
            .setRating(rating, RatingOrigin.RGF)
            .setRank(info.vannier.gotha.Player.rankFromRating(RatingOrigin.RGF, rating))
            .setRegistrationStatus(PlayerRegistrationStatus.FINAL)
            .build();
    }
}
