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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RatingTest {
    private static Stream<Arguments> rgfRatingToRankData() {
        Stream.Builder<Arguments> builder = Stream.builder();

        int r = -30;

        for (int i = 0; i < 600; i += 60) {
            builder.accept(Arguments.of(i, i + 59, r++));
        }

        for (int i = 600; i < 2100; i += 75) {
            builder.accept(Arguments.of(i, i + 74, r++));
        }

        for (int i = 2100; i < 3000; i += 100) {
            builder.accept(Arguments.of(i, i + 99, r++));
        }

        return builder.build();
    }

    @DisplayName("Should convert RGF rating to rank")
    @ParameterizedTest
    @MethodSource("rgfRatingToRankData")
    void testRgfRatingToRankConversion(int minRating, int maxRating, int rank) {
        for (int rating = minRating; rating <= maxRating; rating++) {
            assertEquals(rank, Rating.ratingToRank(RatingOrigin.RGF, rating));
        }
    }

    @DisplayName("Should convert rank to RGF rating")
    @ParameterizedTest
    @MethodSource("rgfRatingToRankData")
    void testRankToRgfRatingConversion(int minRating, int maxRating, int rank) {
        for (int rating = minRating; rating <= maxRating; rating++) {
            assertEquals(minRating, Rating.rankToRating(RatingOrigin.RGF, rank));
        }
    }

    private static Stream<Arguments> clampRatingData() {
        return Stream.of(
            Arguments.of(RatingOrigin.RGF, -10, 0),
            Arguments.of(RatingOrigin.RGF, 0, 0),
            Arguments.of(RatingOrigin.RGF, 3000, 2999),
            Arguments.of(RatingOrigin.RGF, 2999, 2999)
        );
    }

    @ParameterizedTest
    @MethodSource("clampRatingData")
    void testClampRating(RatingOrigin origin, int in, int out) {
        assertEquals(out, Rating.clampRating(origin, in));
    }
}
