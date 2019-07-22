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

import com.google.gson.Gson;

import net.miginfocom.swing.MigLayout;

import java.awt.Dimension;
import java.awt.Frame;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import info.vannier.gotha.Gotha;
import info.vannier.gotha.JFrGotha;
import info.vannier.gotha.PlayerException;
import ru.gofederation.gotha.model.rgf.RgfTournament;
import ru.gofederation.gotha.model.rgf.RgfTournamentImportReport;
import ru.gofederation.gotha.util.GothaLocale;
import ru.gofederation.gotha.util.GothaPreferences;

import static ru.gofederation.gotha.model.rgf.Rgf.API_BASE_PATH;

public final class RgfTournamentImportDialog extends JDialog implements RgfTournamentList.TournamentPickListener {
    private final GothaLocale locale;
    private final TournamentOpener tournamentOpener;

    private final JRadioButton importApplications;

    public RgfTournamentImportDialog(Frame owner, String title, boolean modal, TournamentOpener tournamentOpener) {
        super(owner, title, modal);

        this.locale = GothaLocale.getCurrentLocale();
        this.tournamentOpener = tournamentOpener;

        setLayout(new MigLayout("insets dialog", "[grow,fill]", "[][grow,fill]"));

        JPanel importOptionsPanel = new JPanel(new MigLayout("flowy, insets panel"));
        importOptionsPanel.setBorder(BorderFactory.createTitledBorder(locale.getString("tournament.rgf.import.options")));
        ButtonGroup importMode = new ButtonGroup();
        importApplications = new JRadioButton(locale.getString("tournament.rgf.import.applications"));
        importApplications.setSelected(true);
        importMode.add(importApplications);
        importOptionsPanel.add(importApplications);
        JRadioButton importParticipants = new JRadioButton(locale.getString("tournament.rgf.import.participants"));
        importParticipants.setEnabled(false);
        importMode.add(importParticipants);
        importOptionsPanel.add(importParticipants);

        add(importOptionsPanel, "wrap");

        RgfTournamentList tournamentList = new RgfTournamentList(this);

        add(tournamentList);

        pack();

        GothaPreferences preferences = GothaPreferences.instance();
        preferences.persistWindowState(this, new Dimension(JFrGotha.BIG_FRAME_WIDTH, JFrGotha.BIG_FRAME_HEIGHT));
    }

    private void loadTournament(int id) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            int importMode = importApplications.isSelected()
                ? RgfTournament.IMPORT_MODE_APPLICATIONS : RgfTournament.IMPORT_MODE_WALLIST;
            String url = API_BASE_PATH + "tournaments/" + Integer.toString(id);
            if (importMode == RgfTournament.IMPORT_MODE_APPLICATIONS) url += "?include=player_applications";
            Gotha.download(null, false, url, baos);
            byte[] b = baos.toByteArray();
            try (ByteArrayInputStream bais = new ByteArrayInputStream(b);
                 Reader reader = new InputStreamReader(bais)) {

                RgfTournament tournament = new Gson().fromJson(reader, ru.gofederation.gotha.model.rgf.RgfTournamentDetails.class)
                    .getTournament();

                RgfTournamentImportReport report = tournament.toGothaTournament(importMode);

                JOptionPane reportPane = new JOptionPane();
                if (report.hadError) {
                    reportPane.setMessageType(JOptionPane.WARNING_MESSAGE);
                    reportPane.setMessage(locale.format("tournament.rgf.import.report.with_errors", report.players, report.games) +
                        "\n\n" +
                        report.reportBuilder.toString());
                } else {
                    reportPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
                    reportPane.setMessage(locale.format("tournament.rgf.import.report.no_errors", report.players, report.games));
                }
                reportPane.createDialog(this, locale.getString("tournament.rgf.import.report.window_title")).setVisible(true);

                tournamentOpener.openTournament(report.tournament);
            }
            dispose();
        } catch (IOException | PlayerException e) {
            e.printStackTrace();
            // TODO
        }
    }

    @Override
    public void onTournamentPicked(RgfTournament tournament) {
        loadTournament(tournament.id);
    }
}
