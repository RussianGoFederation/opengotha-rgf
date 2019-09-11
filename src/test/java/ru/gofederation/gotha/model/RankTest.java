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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Test Rank class")
public class RankTest {
    private static Stream<Arguments> data() {
        AtomicInteger c = new AtomicInteger(8);
        return Stream.generate(() -> {
            int i = c.getAndDecrement();
            if (i >= 0) {
                return Arguments.of(i, "" + (i + 1) + "D");
            } else {
                return Arguments.of(i, "" + (i * -1) + "K");
            }

        }).limit(39);
    }

    @ParameterizedTest
    @MethodSource("data")
    void testRank(int i, String s) {
        Rank rankI = Rank.fromInt(i);
        Rank rankS = Rank.fromString(s);

        assertEquals(rankI, rankS);
        assertEquals(i, rankI.getValue());
        assertEquals(i, rankS.getValue());
        assertEquals(s, rankI.toString());
        assertEquals(s, rankS.toString());
    }
}
