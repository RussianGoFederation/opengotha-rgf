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

package ru.gofederation.gotha.model;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rank implements Comparable<Rank> {
    private static final Pattern rankPattern = Pattern.compile("(\\d{1,2})([KD])");
    private static final Map<Integer, Rank> cache = new HashMap<>();
    public static final int MIN_RANK = -30;
    public static final int MAX_RANK = 8;

    /**
     * -30[30k]..8[9d]
     */
    private final int rank;

    private Rank(int rank) {
        assert (MIN_RANK <= rank && rank <= MAX_RANK);
        this.rank = rank;
    }

    public static Rank fromRating(RatingOrigin origin, int rating) {
        return fromInt(Rating.ratingToRank(origin, rating));
    }

    public static Rank fromString(String value) {
        Matcher m = rankPattern.matcher(value);
        if (!m.find() || m.groupCount() != 2) {
            throw new IllegalArgumentException(value + " does not match rank pattern");
        }
        int rank = MIN_RANK;
        if ("k".equals(m.group(2).toLowerCase())) {
            rank = 0 - Integer.parseInt(m.group(1));
        } else if ("d".equals(m.group(2).toLowerCase())) {
            rank = Integer.parseInt(m.group(1)) - 1;
        }
        return fromInt(rank);
    }

    public static Rank fromInt(int value) {
        if (!cache.containsKey(value)) {
            Rank rank = new Rank(value);
            cache.put(value, rank);
            return rank;
        } else {
            return cache.get(value);
        }
    }

    public int getValue() {
        return rank;
    }

    @Override
    public int compareTo(Rank other) {
        return this.rank - other.rank;
    }

    @Override
    public final boolean equals(Object other) {
        if (!(other instanceof Rank)) return false;
        return this.rank == ((Rank) other).rank;
    }

    @Override
    public final int hashCode() {
        return rank;
    }

    @Override
    public String toString() {
        if (rank >=0) return "" + (rank +1) + "D";
        else return "" + (-rank) + "K";
    }
}
