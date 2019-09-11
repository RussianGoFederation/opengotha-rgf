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

package ru.gofederation.gotha.ui.component;

import javax.swing.JComboBox;

import info.vannier.gotha.AutoCompletion;
import info.vannier.gotha.RatedPlayer;
import info.vannier.gotha.RatingList;

public class RatingListComboBox extends JComboBox<RatedPlayer> {
    public RatingListComboBox() {
        setRatingList(null);
        AutoCompletion.enable(this);
    }

    public void setRatingList(RatingList ratingList) {
        this.removeAllItems();
        if (null == ratingList) {
            this.setEnabled(false);
            return;
        }

        for (RatedPlayer rp : ratingList.getALRatedPlayers()) {
            this.addItem(rp);
        }
        this.setEnabled(true);
    }
}
