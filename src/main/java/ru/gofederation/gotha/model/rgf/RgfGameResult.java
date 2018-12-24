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

import info.vannier.gotha.Game;

public enum RgfGameResult {
    PLAYER_1_WIN(Game.RESULT_BLACKWINS),
    PLAYER_2_WIN(Game.RESULT_WHITEWINS),
    PLAYER_1_WIN_BYREF(Game.RESULT_BLACKWINS_BYDEF),
    PLAYER_2_WIN_BYREF(Game.RESULT_WHITEWINS_BYDEF),
    NOT_PLAYED(Game.RESULT_UNKNOWN),
    TIE(Game.RESULT_EQUAL),
    BOTH_LOST(Game.RESULT_BOTHLOSE),
    UNKNOWN(Game.RESULT_UNKNOWN),
    TIE_BYREF(Game.RESULT_EQUAL_BYDEF);

    final int gothaCode;

    RgfGameResult(int gothaCode) {
        this.gothaCode = gothaCode;
    }

    public int getGothaCode() {
        return gothaCode;
    }
}
