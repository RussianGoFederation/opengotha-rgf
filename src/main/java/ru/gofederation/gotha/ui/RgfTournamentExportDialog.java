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

import java.awt.Frame;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.rmi.RemoteException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import info.vannier.gotha.GeneralParameterSet;
import info.vannier.gotha.TournamentInterface;
import ru.gofederation.gotha.model.rgf.RgfTournament;
import ru.gofederation.gotha.model.rgf.RgfTournamentDetails;
import ru.gofederation.gotha.model.rgf.RgfTournamentState;
import ru.gofederation.gotha.util.GothaLocale;

import static ru.gofederation.gotha.model.rgf.Rgf.API_BASE_PATH;

public final class RgfTournamentExportDialog extends JDialog {
    private final GothaLocale locale = GothaLocale.getCurrentLocale();
    private final JCheckBox finishTournament;

    public RgfTournamentExportDialog(Frame owner, String title, boolean modal, TournamentInterface tournament) {
        super(owner, title, modal);

         GeneralParameterSet gps = null;
        try {
            gps = tournament.getTournamentParameterSet().getGeneralParameterSet();
        } catch (RemoteException e) {
            // TODO
        }

        setLayout(new MigLayout("insets dialog, flowy", "[fill, grow]", "[]unrel:push[]"));

        JPanel optionsPanel = new JPanel(new MigLayout("flowy"));
        optionsPanel.setBorder(BorderFactory.createTitledBorder(locale.getString("tournament.rgf.publish.options")));

        ButtonGroup exportMode = new ButtonGroup();

        JRadioButton exportUpdate = new JRadioButton(locale.getString("tournament.rgf.publish.mode_update"));
        exportUpdate.setEnabled(gps.hasRgfId());
        exportUpdate.setSelected(gps.hasRgfId());
        exportMode.add(exportUpdate);
        optionsPanel.add(exportUpdate);

        JRadioButton exportNew = new JRadioButton(locale.getString("tournament.rgf.publish.mode_new"));
        exportNew.setEnabled(!gps.hasRgfId());
        exportNew.setSelected(!gps.hasRgfId());
        exportMode.add(exportNew);
        optionsPanel.add(exportNew);

        finishTournament = new JCheckBox(locale.getString("tournament.rgf.publish.finish"));
        optionsPanel.add(finishTournament, "gaptop unrel");

        add(optionsPanel);

        JButton ok = new JButton(locale.getString("btn.ok"));
        ok.addActionListener(actionEvent -> exportTournament(tournament));
        add(ok, "flowx, split, tag ok");

        JButton cancel = new JButton(locale.getString("btn.cancel"));
        cancel.addActionListener(actionEvent -> dispose());
        add(cancel, "tag cancel");

        pack();
    }

    private void exportTournament(TournamentInterface tournament) {
        String authentication = new RgbAuthentication().getAuthenticationHeader(this);
        if (null == authentication) {
            return;
        }

        HttpURLConnection conn = null;
        boolean hadError = false;
        try {
            RgfTournament rgfTournament = new RgfTournament(tournament);
            rgfTournament.state = finishTournament.isSelected() ?
                RgfTournamentState.MODERATION : RgfTournamentState.CONDUCTING;
            Gson gson = new Gson();
            String json = gson.toJson(new RgfTournamentDetails(rgfTournament));
            byte[] data = json.getBytes(Charset.forName("UTF-8"));

            URL url;
            if (rgfTournament.id > 0) {
                url = new URL(API_BASE_PATH + "tournaments/" + rgfTournament.id);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
            } else {
                url = new URL(API_BASE_PATH + "tournaments");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
            }
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(data.length));
            conn.setRequestProperty("Authorization", authentication);
            conn.setDoOutput(true);
            try (OutputStream out = conn.getOutputStream()) {
                out.write(data);
            }

            if (conn.getResponseCode() == 403) {
                JOptionPane.showMessageDialog(
                    this,
                    locale.getString("tournament.rgf.publish.authentication_required_message"),
                    locale.getString("tournament.rgf.publish.authentication_failed"),
                    JOptionPane.WARNING_MESSAGE);
            }

            try (InputStream in = conn.getInputStream()) {
                try (Reader reader = new InputStreamReader(in)) {
                    RgfTournament postedTournament = new Gson().fromJson(reader, ru.gofederation.gotha.model.rgf.RgfTournamentDetails.class)
                        .getTournament();

                    if (null != postedTournament) {
                        tournament.getTournamentParameterSet().getGeneralParameterSet().setRgfId(postedTournament.id);
                        tournament.setLastTournamentModificationTime(tournament.getCurrentTournamentTime());
                    }
                }
            }
        } catch (IOException e) {
            hadError = true;
            JOptionPane.showMessageDialog(
                this,
                locale.format("tournament.rgf.publish.error", e.getLocalizedMessage()),
                locale.getString("alert.error"),
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            if (null != conn) conn.disconnect();
            if (!hadError) {
                JOptionPane.showMessageDialog(
                    this,
                    locale.getString("tournament.rgf.publish.success"),
                    locale.getString("alert.message"),
                    JOptionPane.INFORMATION_MESSAGE);
            }
            dispose();
        }
    }
}
