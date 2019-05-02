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

import static ru.gofederation.gotha.model.RatingOrigin.RGF;

public final class Rating {
    private static final int MIN_RGF_RATING = 0;
    private static final int MAX_RGF_RATING = 2999;

    public static int clampRating(RatingOrigin origin, int rating) {
        if (origin == RGF) {
            return Math.max(Math.min(rating, MAX_RGF_RATING), MIN_RGF_RATING);
        }

        throw new IllegalArgumentException("Not implemented");
    }

    public static int ratingToRank(RatingOrigin origin, int rating) {
        if (origin == RGF) {
            if (rating < MIN_RGF_RATING || rating > MAX_RGF_RATING) {
                throw new IllegalArgumentException();
            }

            if (rating < 600) {
                return rating / 60 - 30;
            }

            if (rating < 2100) {
                return rating / 75 - 28;
            }

            return rating / 100 - 21;
        }

        throw new IllegalArgumentException("Not implemented");
    }

    public static int rankToRating(RatingOrigin origin, int rank) {
        // TODO test limits

        if (origin == RGF) {
            if (rank <= -20) {
                return (rank + 30) * 60;
            }

            if (rank <= 0) {
                return (rank + 20) * 75 + 600;
            }

            return rank * 100 + 2100;
        }

        throw new IllegalArgumentException("Not implemented");
    }
}
