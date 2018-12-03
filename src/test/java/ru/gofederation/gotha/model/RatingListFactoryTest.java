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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.stream.Stream;

import info.vannier.gotha.RatedPlayer;
import info.vannier.gotha.RatingList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.gofederation.gotha.model.RatingOrigin.AGA;
import static ru.gofederation.gotha.model.RatingOrigin.EGF;
import static ru.gofederation.gotha.model.RatingOrigin.FFG;

@DisplayName("Test rating list loading")
public class RatingListFactoryTest {
    private static Stream<Arguments> data() {
        return Stream.of(
            Arguments.of(
                "ru/gofederation/gotha/model/aga_rl.txt", RatingListType.AGA, new RatedPlayer[]{
                    new RatedPlayer("", "", "", "23168", "3/5/2018", "Anderson", "Glenn", "US", "SEAG", -1367, "", AGA),
                }),
            Arguments.of(
                "ru/gofederation/gotha/model/egf_rl.txt", RatingListType.EGF, new RatedPlayer[] {
                    new RatedPlayer("13633719", "", "", "", "", "Aagren", "Thomas", "SE", "Upp", 1484, "6k", EGF),
                    new RatedPlayer("15813842", "", "", "", "", "Aahman", "Sebastian", "SE", "Vaxj", 1129, "10k", EGF),
                }
            ), Arguments.of(
                "ru/gofederation/gotha/model/ffg_rl.txt", RatingListType.FFG, new RatedPlayer[] {
                    new RatedPlayer("", "-------", "e", "", "", "AAIJ", "René", "NL", "xxxx", 243, "", FFG),
                    new RatedPlayer("", "-------", "e", "", "", "AAKERBLOM", "Charlie", "SE", "xxxx", 433, "", FFG),
                    new RatedPlayer("", "9728205", "-", "", "", "ABADIA", "Mickaël", "FR", "94MJ", -1400, "", FFG),
                }
            )
        );
    }

    @DisplayName("Should load rating list")
    @ParameterizedTest
    @MethodSource("data")
    void testRatingListLoader(String filename, RatingListType ratingListType, RatedPlayer[] players) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(filename).getFile());

        try (FileInputStream in = new FileInputStream(file)) {
            RatingList rl = RatingListFactory.instance().load(ratingListType, in);
            assertEquals(10, rl.getALRatedPlayers().size());
            assertEquals(ratingListType, rl.getRatingListType());

            for (int i = 0; i < players.length; i++) {
                assertEquals(players[i], rl.getALRatedPlayers().get(i));
            }
        }
    }
}
