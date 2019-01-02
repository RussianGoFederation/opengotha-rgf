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

package info.vannier.gotha;

import ru.gofederation.gotha.util.GothaLocale;

enum TeamPlacementCriterion implements java.io.Serializable {
    /** No tie break */
    NUL           (0,   "NULL", "NULL",  1),
    /** Team points */
    TEAMPOINTS    (1,   "TP",   "TEAMP", 1),
    /** Sum of Opponents Scores (Team points) */
    SOST          (11,  "SOST", "SOST",  1),
    /** Board Wins */
    BOARDWINS     (12,  "BDW",  "BDW",   2),
    /** Board Wins. 9 Upper boards */
    BOARDWINS_9UB (109, "B9U",  "BDW9U", 2),
    /** Board Wins. 8 Upper boards */
    BOARDWINS_8UB (108, "B8U",  "BDW8U", 2),
    /** Board Wins. 7 Upper boards */
    BOARDWINS_7UB (107, "B7U",  "BDW7U", 2),
    /** Board Wins. 6 Upper boards */
    BOARDWINS_6UB (106, "B6U",  "BDW6U", 2),
    /** Board Wins. 5 Upper boards */
    BOARDWINS_5UB (105, "B5U",  "BDW5U", 2),
    /** Board Wins. 4 Upper boards */
    BOARDWINS_4UB (104, "B4U",  "BDW4U", 2),
    /** Board Wins. 3 Upper boards */
    BOARDWINS_3UB (103, "B3U",  "BDW3U", 2),
    /** Board Wins. 2 Upper boards */
    BOARDWINS_2UB (102, "B2U",  "BDW2U", 2),
    /** Board Wins. 1 Upper board */
    BOARDWINS_1UB (101, "B1U",  "BDW1U", 2),
    /** Mean rating at first round */
    MEAN_RATING   (201, "MNR",  "MNR",   1);


    private final int uid;
    private final String shortName;
    private final String longName;
    private final String descriptionKey;
    private final int coef;

    TeamPlacementCriterion(int uid, String shortName, String longName, int coef) {
        this.uid = uid;
        this.shortName = shortName;
        this.longName = longName;
        this.descriptionKey = "tpps." + name();
        this.coef = coef;
    }

    public static TeamPlacementCriterion fromUid(int uid) {
        for (TeamPlacementCriterion criterion : values()) {
            if (criterion.getUid() == uid)
                return criterion;
        }

        return null;
    }

    public static TeamPlacementCriterion fromLongName(String longName) {
        for (TeamPlacementCriterion criterion : values()) {
            if (criterion.getLongName() == longName)
                return criterion;
        }

        return null;
    }

    public int getUid() {
        return uid;
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    public String getDescriptionKey() {
        return descriptionKey;
    }

    public String getDescription(GothaLocale locale) {
        return locale.getString(getDescriptionKey());
    }

    public int getCoef() {
        return coef;
    }
}
