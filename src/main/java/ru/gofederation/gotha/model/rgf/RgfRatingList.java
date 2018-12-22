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

import java.util.Set;

public final class RgfRatingList {
    @SerializedName("Data")
    Data data;

    public Set<Player> getPlayers() {
        if (null != data) return data.players;
        else return null;
    }

    public static final class Data {
        @SerializedName("Players")
        Set<Player> players;
    }

    public static final class Player {
        @SerializedName("Id")
        public int id;
        @SerializedName("FirstName")
        public String firstName;
        @SerializedName("LastName")
        public String lastName;
        @SerializedName("FirstNameLat")
        public String firstNameLat;
        @SerializedName("LastNameLat")
        public String lastNameLat;
        @SerializedName("TownName")
        public String townName;
        @SerializedName("Rating")
        public int rawRating;
        @SerializedName("RatingDrift")
        public int ratingDrift;
        @SerializedName("LastGame")
        public String lastGame;
    }
}
