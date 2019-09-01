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

package ru.gofederation.gotha.ui;

import net.miginfocom.swing.MigLayout;

import java.awt.Font;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import info.vannier.gotha.AutoCompletion;
import info.vannier.gotha.RatedPlayer;
import info.vannier.gotha.RatingList;
import ru.gofederation.gotha.util.GothaLocale;

public class RatingListSearch extends JPanel {
    private final GothaLocale locale = GothaLocale.getCurrentLocale();
    private final JLabel ratingListStats;
    private final JComboBox<String> searchBox;
    private Listener listener = null;

    private RatingList ratingList;

    public RatingListSearch() {
        setLayout(new MigLayout("flowy, insets 0", "shrink 50"));

        ratingListStats = new JLabel();
        ratingListStats.setFont(ratingListStats.getFont().deriveFont(Font.ITALIC));
        add(ratingListStats);

        searchBox = new JComboBox<>();
        searchBox.setEditable(true);
        searchBox.addItemListener(this::searchBoxItemStateChanged);
        new AutoCompletion(searchBox);
        add(searchBox, "wmin 30lp");

        setRatingList(null);
    }

    public void setRatingList(RatingList ratingList) {
        this.ratingList = ratingList;

        if (null == ratingList) {
            ratingListStats.setText(locale.getString("rating_list.not_loaded"));
            searchBox.removeAllItems();
            searchBox.setEnabled(false);
        } else {
            ratingListStats.setText(locale.format("rating_list.stats",
                locale.getString(ratingList.getRatingListType().getL10nKey()),
                ratingList.getStrPublicationDate(),
                ratingList.getALRatedPlayers().size()));
            searchBox.removeAllItems();
            searchBox.addItem("");
            for (RatedPlayer rP : ratingList.getALRatedPlayers()) {
                searchBox.addItem(this.ratingList.getRatedPlayerString(rP));
            }
            searchBox.setEnabled(true);
        }
    }

    private void searchBoxItemStateChanged(java.awt.event.ItemEvent evt) {
        if (null != listener) {
            int index = searchBox.getSelectedIndex();
            if (index <= 0) {
                listener.onPlayerSelected(null);
            } else {
                RatedPlayer player = ratingList.getALRatedPlayers().get(index - 1);
                listener.onPlayerSelected(player);
            }
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onPlayerSelected(RatedPlayer player);
    }
}
