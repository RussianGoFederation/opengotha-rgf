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

import info.vannier.gotha.GeneralParameterSet;
import info.vannier.gotha.TournamentException;
import info.vannier.gotha.TournamentInterface;
import net.miginfocom.swing.MigLayout;
import ru.gofederation.gotha.model.Player;
import ru.gofederation.gotha.util.GothaLocale;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SmmsByHand extends JPanel {
    private final TournamentInterface tournament;

    private final PlayerList playerList;
    private final JButton btnSetSelectedZero;
    private final JButton btnSetSelectedDefault;
    private final JButton btnIncrement;
    private final JButton btnDecrement;
    private final JButton btnRise;

    private GothaLocale locale;

    public SmmsByHand(TournamentInterface tournament) {
        this.tournament = tournament;
        locale = GothaLocale.getCurrentLocale();

        setLayout(new MigLayout(null, "[]unrel[500lp::, fill, sgx btn]"));

        playerList = new PlayerList();
        playerList.setTournament(tournament);
        playerList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        playerList.getSelectionModel().addListSelectionListener(selectionListener);
        playerList.setSortKeys(Arrays.asList(
            new RowSorter.SortKey(playerList.getColumnIndex(PlayerList.Column.SMMS), SortOrder.DESCENDING),
            new RowSorter.SortKey(playerList.getColumnIndex(PlayerList.Column.RATING), SortOrder.DESCENDING)
        ));
        add(playerList, "spany, push, grow");

        btnRise = new JButton();
        btnRise.addActionListener(event -> playerList.preserveSelection(() -> {
            try {
                if (!selectionValidToRise()) return;

                int[] selectedRows = playerList.getSelectedRows();
                GeneralParameterSet gps = tournament.getTournamentParameterSet().getGeneralParameterSet();
                for (int i = 0; i <= selectedRows[selectedRows.length - 1]; i++) {
                    Player player = playerList.getPlayer(i);
                    Player.Builder builder = player.toBuilder();
                    builder.setSmmsByHand(player.smms(gps) + 1);
                    tournament.modifyPlayer(player, builder.build());
                }
            } catch (TournamentException | RemoteException e) {
                // TODO
            }
            setTournamentUpdateTime();
        }));
        add(btnRise, "gaptop 0:40lp:, sgx btn, h min*1.5, wrap");

        JLabel riseHelp = new JLabel(locale.getString("tournament.setup_smms_by_hand.btn_rise_help"));
        add(riseHelp, "hmax 50lp, wmax 500lp, wrap unrel:unrel*2");

        JButton btnSetAllZero = new JButton(locale.getString("tournament.setup_smms_by_hand.btn_set_all_zero"));
        btnSetAllZero.addActionListener(e -> setZero(allPlayers()));
        add(btnSetAllZero, "sgx btn, wrap");

        btnSetSelectedZero = new JButton();
        btnSetSelectedZero.addActionListener(e -> setZero(selectedPlayers()));
        add(btnSetSelectedZero, "sgx btn, wrap unrel:unrel*2");

        JButton btnSetAllDefault = new JButton(locale.getString("tournament.setup_smms_by_hand.btn_set_all_default"));
        btnSetAllDefault.addActionListener(e -> setDefault(allPlayers()));
        add(btnSetAllDefault, "sgx btn, wrap");

        btnSetSelectedDefault = new JButton();
        btnSetSelectedDefault.addActionListener(e -> setDefault(selectedPlayers()));
        add(btnSetSelectedDefault, "sgx btn, wrap unrel:unrel*2");

        btnIncrement = new JButton();
        btnIncrement.addActionListener(e -> adjustSmms(playerList.getSelectedPlayers(), 1));
        add(btnIncrement, "sgx btn, wrap");

        btnDecrement = new JButton();
        btnDecrement.addActionListener(e -> adjustSmms(playerList.getSelectedPlayers(), -1));
        add(btnDecrement, "sgx btn, wrap :push");

        selectionListener.valueChanged(null);
    }

    private void setTournamentUpdateTime() {
        try {
            tournament.setLastTournamentModificationTime(tournament.getCurrentTournamentTime());
        } catch (RemoteException e) {
            // TODO
        }
        playerList.onTournamentUpdated();
    }

    private final ListSelectionListener selectionListener = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent event) {
            int c = playerList.getSelectedRowCount();
            btnSetSelectedZero.setEnabled(c > 0);
            btnSetSelectedZero.setText(locale.format("tournament.setup_smms_by_hand.btn_set_selected_zero", c));
            btnSetSelectedDefault.setEnabled(c > 0);
            btnSetSelectedDefault.setText(locale.format("tournament.setup_smms_by_hand.btn_set_selected_default", c));
            btnIncrement.setEnabled(c > 0);
            btnIncrement.setText(locale.format("tournament.setup_smms_by_hand.btn_increment", c));
            btnDecrement.setEnabled(c > 0);
            btnDecrement.setText(locale.format("tournament.setup_smms_by_hand.btn_decrement", c));

            try {
                if (selectionValidToRise()) {
                    btnRise.setEnabled(true);
                    btnRise.setText(locale.format("tournament.setup_smms_by_hand.btn_rise", playerList.getSelectedRows().length));
                } else {
                    btnRise.setEnabled(false);
                    btnRise.setText(locale.format("tournament.setup_smms_by_hand.btn_rise", 0));
                }
            } catch (RemoteException e) {
                // TODO
            }
        }
    };

    private boolean selectionValidToRise() throws RemoteException {
        int[] selectedRows = playerList.getSelectedRows();

        if (selectedRows.length < 1) return false;

        GeneralParameterSet gps = tournament.getTournamentParameterSet().getGeneralParameterSet();

        for (int i = selectedRows[0] - 1; i >= 0; i--) {
            if (playerList.getPlayer(i).smms(gps) <= 0) {
                return false;
            }
        }

        int n = selectedRows[0];
        for (int i : selectedRows) {
            if (i != n++) return false;
            if (playerList.getPlayer(i).smms(gps) != 0) return false;
        }

        return true;
    }

    private void setZero(List<Player> players) {
        playerList.preserveSelection(() -> {
            for (Player player : players) {
                try {
                    tournament.modifyPlayer(player, pb -> pb.setSmmsByHand(0));
                } catch (TournamentException | RemoteException e) { }
            }
            setTournamentUpdateTime();
        });
    }

    private void setDefault(List<Player> players) {
        playerList.preserveSelection(() -> {
            for (Player player : players) {
                try {
                    tournament.modifyPlayer(player, pb -> pb.setSmmsByHand(-1));
                } catch (TournamentException | RemoteException e) { }
            }
            setTournamentUpdateTime();
        });
    }

    private void adjustSmms(List<Player> players, int delta) {
        playerList.preserveSelection(() -> {
            try {
                GeneralParameterSet gps = tournament.getTournamentParameterSet().getGeneralParameterSet();
                try {
                    for (Player player : players) {
                        tournament.modifyPlayer(player, pb -> {
                            int smms = player.smms(gps) + delta;
                            if (smms < 0) smms = 0;
                            pb.setSmmsByHand(smms);
                        });
                    }
                } catch (TournamentException | RemoteException e) { }
                setTournamentUpdateTime();
            } catch (RemoteException e) {
                // TODO
            }
        });
    }

    private List<Player> allPlayers() {
        try {
            return tournament.playersList();
        } catch (RemoteException e) {
            // TODO
        }
        return new ArrayList<>();
    }

    private List<Player> selectedPlayers() {
        return playerList.getSelectedPlayers();
    }
}
