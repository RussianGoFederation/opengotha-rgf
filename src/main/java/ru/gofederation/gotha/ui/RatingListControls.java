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

import java.awt.CardLayout;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;

import info.vannier.gotha.Gotha;
import ru.gofederation.gotha.model.RatingListType;
import ru.gofederation.gotha.util.GothaLocale;
import ru.gofederation.gotha.util.GothaPreferences;

import static ru.gofederation.gotha.model.RatingListType.AGA;
import static ru.gofederation.gotha.model.RatingListType.EGF;
import static ru.gofederation.gotha.model.RatingListType.FFG;
import static ru.gofederation.gotha.model.RatingListType.RGF;
import static ru.gofederation.gotha.model.RatingListType.UND;

public final class RatingListControls extends JPanel {
    private static final String BUTTON = "button";
    private static final String PROGRESS = "progress";

    private final JCheckBox useRatingListCheckbox;
    private final JButton updateButton;
    private final JProgressBar updateProgress;
    private final JPanel updatePanel;
    private final CardLayout updateLayout;
    private final Map<JRadioButton, RatingListType> ratingOrigins = new HashMap<>();
    private final Collection<Listener> listeners = new LinkedList<>();

    private GothaLocale locale;
    private final GothaPreferences preferences;

    public RatingListControls() {
        preferences = GothaPreferences.instance();

        useRatingListCheckbox = new JCheckBox();
        JRadioButton rdbAGA = new JRadioButton();
        JRadioButton rdbEGF = new JRadioButton();
        JRadioButton rdbFFG = new JRadioButton();
        JRadioButton rdbRGF = new JRadioButton();
        updateButton = new JButton();
        updateProgress = new JProgressBar();

        rdbEGF.setSelected(true);

        ButtonGroup grpRatingList = new ButtonGroup();
        grpRatingList.add(rdbAGA);
        grpRatingList.add(rdbEGF);
        grpRatingList.add(rdbFFG);
        grpRatingList.add(rdbRGF);

        updatePanel = new JPanel();
        updateLayout = new CardLayout();
        updatePanel.setLayout(updateLayout);
        updatePanel.add(updateButton, BUTTON);
        updatePanel.add(updateProgress, PROGRESS);

        setLayout(new MigLayout("insets 0", "[fill]unrel[]rel[]rel[]", "[]unrel[]rel[]rel[]"));
        add(useRatingListCheckbox, "span, wrap");
        add(rdbEGF, "gapbefore indent");
        add(rdbFFG, "wrap");
        add(rdbAGA, "gapbefore indent");
        add(rdbRGF, "wrap");
        add(updatePanel, "sg update, span");

        useRatingListCheckbox.addActionListener((event) -> setRatingListEnabled(useRatingListCheckbox.isSelected()));
        useRatingListCheckbox.addActionListener((event) -> onTypeChange());
        updateButton.addActionListener((event) -> downloadRatingList());
        addRatingSource(rdbAGA, AGA);
        addRatingSource(rdbEGF, EGF);
        addRatingSource(rdbFFG, FFG);
        addRatingSource(rdbRGF, RGF);

        updateProgress.setStringPainted(true);

        loadPreferences();
        updateLocale(GothaLocale.getCurrentLocale());

        setRatingListEnabled(useRatingListCheckbox.isSelected());
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    private void notifyListeners(RatingListType rlType) {
        for (Listener listener : listeners) {
            listener.onRatingListSelected(rlType);
        }
    }

    private void addRatingSource(JRadioButton button, RatingListType type) {
        ratingOrigins.put(button, type);
        button.addActionListener((event) -> onTypeChange());
    }

    private void updateLocale(GothaLocale locale) {
        this.locale = locale;
        useRatingListCheckbox.setText(locale.getString("rating_list.use"));
        for (JRadioButton rb : ratingOrigins.keySet()) {
            String type = locale.getString(ratingOrigins.get(rb).getL10nKey());
            rb.setText(type);
            if (rb.isSelected())
                updateButton.setText(locale.format("rating_list.btn_update_from", type));
        }
    }

    private void setRatingListEnabled(boolean enabled) {
        updateButton.setVisible(enabled);
        for (JRadioButton rb : ratingOrigins.keySet())
            rb.setVisible(enabled);
    }

    private void loadPreferences() {
        String rlName = preferences.getString(GothaPreferences.DEFAULT_RATING_LIST, UND.name());
        RatingListType rlType = RatingListType.fromName(rlName);
        useRatingListCheckbox.setSelected(UND != rlType);
        for (JRadioButton button : ratingOrigins.keySet()) {
            RatingListType type = ratingOrigins.get(button);
            if (rlType == type) {
                button.setSelected(true);
            }
        }
    }

    private void onTypeChange() {
        RatingListType rlType = getSelectedRatingListType();

        if (UND != rlType) {
            updateButton.setText(locale.format("rating_list.btn_update_from", locale.format(rlType.getL10nKey())));
        }

        notifyListeners(rlType);

        preferences.putString(GothaPreferences.DEFAULT_RATING_LIST, rlType.name()).sync();
    }

    public RatingListType getSelectedRatingListType() {
        if (!useRatingListCheckbox.isSelected()) return UND;

        for (JRadioButton rb : ratingOrigins.keySet()) {
            if (rb.isSelected()) return ratingOrigins.get(rb);
        }

        return UND;
    }

    private void downloadRatingList() {
        if (!Gotha.isRatingListsDownloadEnabled()){
            JOptionPane.showMessageDialog(this, locale.getString("rating_list.disabled"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        RatingListType rlType = getSelectedRatingListType();
        if (rlType == UND) {
            return;
        }

        String strDefaultURL;
        File fDefaultFile;
        String strPrompt;

        strDefaultURL = rlType.getUrl();
        fDefaultFile = new File(Gotha.getRunningDirectory(), rlType.getFilename());
        strPrompt = locale.format("rating_list.btn_update_from", locale.getString(rlType.getL10nKey()));

        try {
            String url = JOptionPane.showInputDialog(strPrompt, strDefaultURL);
            if (url == null) return;
            updateProgress.setValue(0);
            updateLayout.show(updatePanel, PROGRESS);
            updatePanel.paintImmediately(0, 0, updatePanel.getWidth(), updatePanel.getHeight());
            Gotha.download(updateProgress, url, fDefaultFile);
        } catch (MalformedURLException ex) {
            JOptionPane.showMessageDialog(this, locale.getString("rating_list.error.malformed_url"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, locale.getString("rating_list.error.unreachable_file"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
        } finally {
            updateLayout.show(updatePanel, BUTTON);
        }

        notifyListeners(rlType);
    }

    public interface Listener {
        void onRatingListSelected(RatingListType rlType);
    }
}
