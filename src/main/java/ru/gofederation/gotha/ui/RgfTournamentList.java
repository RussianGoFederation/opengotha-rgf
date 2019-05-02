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

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import info.vannier.gotha.Gotha;
import ru.gofederation.gotha.model.rgf.RgfTournament;
import ru.gofederation.gotha.model.rgf.RgfTournamentState;
import ru.gofederation.gotha.util.GothaLocale;

final class RgfTournamentList extends JPanel {
    private static final String PROGRESS = "progress";
    private static final String LIST = "list";

    private final GothaLocale locale;

    private final JTable tournamentsTable;
    private final JProgressBar progressBar;
    private final CardLayout layout;

    RgfTournamentList(TournamentPickListener tournamentPickListener) {
        this.locale = GothaLocale.getCurrentLocale();

        TableCellRenderer tableCellRenderer = new DefaultTableCellRenderer() {
            DateFormat dateFormat = locale.getDateFormat();

            public Component getTableCellRendererComponent(JTable table,
                                                           Object value, boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                if (value instanceof Date) {
                    value = dateFormat.format(value);
                } else if (value instanceof RgfTournamentState) {
                    value = locale.getString(((RgfTournamentState) value).getL10nKey());
                }
                return super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);
            }
        };

        layout = new CardLayout();
        setLayout(layout);

        tournamentsTable = new JTable();
        tournamentsTable.setDefaultRenderer(Date.class, tableCellRenderer);
        tournamentsTable.setDefaultRenderer(RgfTournamentState.class, tableCellRenderer);
        tournamentsTable.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                Point point = e.getPoint();
                int row = tournamentsTable.rowAtPoint(point);
                if (e.getClickCount() == 2 && tournamentsTable.getSelectedRow() != -1) {
                    TableModel model = (TableModel) tournamentsTable.getModel();
                    RgfTournament tournament = model.getTournament(tournamentsTable.convertRowIndexToModel(row));
                    tournamentPickListener.onTournamentPicked(tournament);
                }
            }
        });

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        JPanel tablePanel = new JPanel(new MigLayout("insets 0", "[grow,fill]", "[grow,fill]"));
        tablePanel.add(new JScrollPane(tournamentsTable));

        JPanel progressPanel = new JPanel(new MigLayout("flowy", "push[]push", "push[]rel[]push"));
        progressPanel.add(new JLabel(locale.getString("tournament.rgf.download_in_progress")));
        progressPanel.add(progressBar);

        add(progressPanel, PROGRESS);
        add(tablePanel, LIST);

        new TableModel().update();
    }

    private void onListDownloaded(TableModel model) {
        tournamentsTable.setModel(model);
        tournamentsTable.getColumnModel().getColumn(TableModel.NAME_COLUMN).setPreferredWidth(400);
        tournamentsTable.getColumnModel().getColumn(TableModel.LOCATION_COLUMN).setPreferredWidth(150);
        tournamentsTable.setAutoCreateRowSorter(true);
        // Twice to sort descending
        tournamentsTable.getRowSorter().toggleSortOrder(TableModel.END_DATE_COLUMN);
        tournamentsTable.getRowSorter().toggleSortOrder(TableModel.END_DATE_COLUMN);
        layout.show(this, LIST);
    }

    private final class TableModel extends AbstractTableModel {
        private static final int NAME_COLUMN = 0;
        private static final int START_DATE_COLUMN = 1;
        private static final int END_DATE_COLUMN = 2;
        private static final int LOCATION_COLUMN = 3;
        private static final int STATE_COLUMN = 4;
        private static final int APPLICATIONS_COLUMN = 5;
        private static final int PARTICIPANTS_COLUMN = 6;

        private volatile List<RgfTournament> tournaments = new ArrayList<>();
        private Thread updateThread = null;

        public void update() {
            if (null != updateThread) updateThread.interrupt();
            updateThread = new Thread(() -> {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    Gotha.download(progressBar, false, "https://gofederation.ru/api/v3.0/tournaments", baos);
                    byte[] b = baos.toByteArray();
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(b);
                         Reader reader = new InputStreamReader(bais)) {
                        ru.gofederation.gotha.model.rgf.RgfTournamentList list =
                            new Gson().fromJson(reader, ru.gofederation.gotha.model.rgf.RgfTournamentList.class);
                        tournaments =  list.getTournaments().stream()
                            .filter(t -> t.applicationsCount > 0)
                            .collect(Collectors.toList());
                        EventQueue.invokeLater(() -> onListDownloaded(this));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            updateThread.start();
        }

        RgfTournament getTournament(int row) {
            return tournaments.get(row);
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case NAME_COLUMN:         return locale.getString("tournament.name");
                case START_DATE_COLUMN:   return locale.getString("tournament.begin_date");
                case END_DATE_COLUMN:     return locale.getString("tournament.end_date");
                case LOCATION_COLUMN:     return locale.getString("tournament.location");
                case STATE_COLUMN:        return locale.getString("tournament.rgf.state");
                case APPLICATIONS_COLUMN: return locale.getString("tournament.rgf.applications");
                case PARTICIPANTS_COLUMN: return locale.getString("tournament.rgf.participants");
                default:                return null;
            }
        }

        @Override
        public int getRowCount() {
            return tournaments.size();
        }

        @Override
        public int getColumnCount() {
            return 7;
        }

        @Override
        public Object getValueAt(int row, int column) {
            switch (column) {
                case NAME_COLUMN:         return tournaments.get(row).name;
                case START_DATE_COLUMN:   return tournaments.get(row).startDate;
                case END_DATE_COLUMN:     return tournaments.get(row).endDate;
                case LOCATION_COLUMN:     return tournaments.get(row).location;
                case STATE_COLUMN:        return tournaments.get(row).state;
                case APPLICATIONS_COLUMN: return tournaments.get(row).applicationsCount;
                case PARTICIPANTS_COLUMN: return tournaments.get(row).participantsCount;
                default:                return null;
            }
        }

        @Override
        public Class getColumnClass(int column) {
            switch (column) {
                case START_DATE_COLUMN:
                case END_DATE_COLUMN:
                    return Date.class;

                case STATE_COLUMN:
                    return RgfTournamentState.class;

                default:
                    return String.class;
            }
        }
    }

    public interface TournamentPickListener {
        void onTournamentPicked(RgfTournament tournament);
    }
}
