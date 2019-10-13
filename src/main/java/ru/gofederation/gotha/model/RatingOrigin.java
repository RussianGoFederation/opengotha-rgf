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

public enum RatingOrigin {
    UNDEF(-900, 2949),
    /** Rating coming from European Go DataBase */
    EGF(-900, 2949),
    /** Rating coming from FFG Rating list */
    FFG(-900, 2949),
    /** Rating coming from American Go Association */
    AGA(-900, 2949),
    /** Rating coming from Russian Go Federation */
    RGF(0, 3000),
    /** Rating specified by the organiser or imported from vBar-separated file */
    MAN(-900, 2949),
    /** Rating computed from rank */
    INI(-900, 2949);

    RatingOrigin(int minRating, int maxRating) {
        this.minRating = minRating;
        this.maxRating = maxRating;
    }

    private final int minRating;
    private final int maxRating;

    public int getMinRating() {
        return minRating;
    }

    public int getMaxRating() {
        return maxRating;
    }

    public static RatingOrigin fromString(String origin) {
        for (RatingOrigin ratingOrigin : values()) {
            if (ratingOrigin.name().equals(origin)) return ratingOrigin;
        }

        return UNDEF;
    }

    @Override
    public String toString() {
        if (this == UNDEF) return "";
        else return name();
    }
}
