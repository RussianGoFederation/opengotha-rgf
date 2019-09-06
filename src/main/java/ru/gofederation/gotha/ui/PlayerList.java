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

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import info.vannier.gotha.Player;
import info.vannier.gotha.TournamentInterface;
import ru.gofederation.gotha.model.PlayerRegistrationStatus;
import ru.gofederation.gotha.util.GothaLocale;
import ru.gofederation.gotha.util.TableColumnConfig;

public final class PlayerList extends JPanel {
    private final JTable table;
    private JPopupMenu popupMenu = null;

    private PlayerDoubleClickListener playerDoubleClickListener;

    public PlayerList() {
        setLayout(new MigLayout("insets 0, fill"));

        table = new JTable();
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, "grow");

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event){
                if (null != playerDoubleClickListener && event.getClickCount() >= 2) {
                    playerDoubleClickListener.onPlayerDoubleClicked(getSelectedPlayer());
                }
            }
        });
    }

    private JPopupMenu initPopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        menu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
                EventQueue.invokeLater(() -> {
                    int row = table.rowAtPoint(SwingUtilities.convertPoint(menu, new Point(0, 0), table));
                    if (row >= 0) {
                        table.setRowSelectionInterval(row, row);
                    }
                });
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {}

            @Override
            public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {}
        });
        table.setComponentPopupMenu(menu);
        return menu;
    }

    public JMenuItem addContextMenuItem(String text, Consumer<Player> callback) {
        if (null == popupMenu) popupMenu = initPopupMenu();
        JMenuItem menuItem = new JMenuItem(text);
        popupMenu.add(menuItem);
        menuItem.addActionListener(actionEvent -> callback.accept(getSelectedPlayer()));
        return menuItem;
    }

    public int getColumnIndex(Column column) {
        return ((TableModel) table.getModel()).getColumnIndex(column);
    }

    public void setSelectionMode(int selectionMode) {
        table.setSelectionMode(selectionMode);
    }

    public ListSelectionModel getSelectionModel() {
        return table.getSelectionModel();
    }

    public int getSelectedRowCount() {
        return table.getSelectedRowCount();
    }

    public int[] getSelectedRows() {
        return table.getSelectedRows();
    }

    public Player getPlayer(int row) throws RemoteException {
        return ((TableModel) table.getModel()).getPlayer(table.convertRowIndexToModel(row));
    }

    public List<Player> getSelectedPlayers() {
        List<Player> players = new ArrayList<>();
        int[] selectedRows = table.getSelectedRows();
        TableModel model = (TableModel) table.getModel();
        for (int row : selectedRows) {
            try {
                players.add(model.getPlayer(table.convertRowIndexToModel(row)));
            } catch (RemoteException e) {
                // TODO
            }
        }
        return players;
    }

    public void preserveSelection(Runnable r) {
        int[] rows = table.getSelectedRows();
        for (int i = 0; i < rows.length; i++)
            rows[i] = table.convertRowIndexToModel(rows[i]);

        r.run();

        for (int i = 0; i < rows.length; i++) {
            rows[i] = table.convertRowIndexToView(rows[i]);
            table.addRowSelectionInterval(rows[i], rows[i]);
        }
    }

    private Player getSelectedPlayer() {
        try {
            TableModel model = (TableModel) table.getModel();
            int row = table.getRowSorter().convertRowIndexToModel(table.getSelectedRow());
            return model.getPlayer(row);
        } catch (RemoteException e) {
            // TODO
            return null;
        }
    }

    public void setPlayerDoubleClickListener(PlayerDoubleClickListener playerDoubleClickListener) {
        this.playerDoubleClickListener = playerDoubleClickListener;
    }

    public void setModel(AbstractTableModel model) {
        table.setModel(model);
    }

    public void setTournament(TournamentInterface tournament) {
        TableModel model = new TableModel(tournament);
        table.setModel(model);
        TableColumnModel columnModel = table.getColumnModel();
        for (int i = 0; i < model.getColumnCount(); i++) {
            TableColumn tableColumn = columnModel.getColumn(i);
            Column column = model.getColumn(i);
            TableColumnConfig.configure(tableColumn, column);

            if (column == Column.LAST_NAME)
                table.getRowSorter().toggleSortOrder(i);
        }

        ((TableRowSorter) table.getRowSorter()).setComparator(getColumnIndex(Column.RANK), rankComparator);
        ((TableRowSorter) table.getRowSorter()).setComparator(getColumnIndex(Column.GRADE), rankComparator);

        table.getRowSorter().addRowSorterListener(rowSorterEvent -> {
            List<? extends RowSorter.SortKey> origKeys =  table.getRowSorter().getSortKeys();
            List<RowSorter.SortKey> sortKeys = new LinkedList<>();
            if (origKeys.size() > 0) {
                RowSorter.SortKey key = origKeys.get(0);
                sortKeys.add(key);
                sortKeys.add(new RowSorter.SortKey(getColumnIndex(Column.LAST_NAME), key.getSortOrder()));
                sortKeys.add(new RowSorter.SortKey(getColumnIndex(Column.FIRST_NAME), key.getSortOrder()));
            }
            rowSorterEvent.getSource().setSortKeys(sortKeys);
        });
    }

    public void setSortKeys(List<? extends RowSorter.SortKey> keys) {
        table.getRowSorter().setSortKeys(keys);
    }

    public void onTournamentUpdated() {
        ((AbstractTableModel) table.getModel()).fireTableDataChanged();
    }

    public static final class TableModel extends AbstractTableModel {
        private final List<Column> displayedColumns = new ArrayList<>();
        private final TournamentInterface tournament;
        private final GothaLocale locale = GothaLocale.getCurrentLocale();

        TableModel(TournamentInterface tournament) {
            this.tournament = tournament;
            updateDisplayedColumns();
        }

        private void updateDisplayedColumns() {
            displayedColumns.clear();
            displayedColumns.addAll(Arrays.asList(Column.values()));
        }

        private Player getPlayer(int row) throws RemoteException {
            return tournament.playersList().get(row);
        }

        Column getColumn(int col) {
            return displayedColumns.get(col);
        }

        int getColumnIndex(Column column) {
            return displayedColumns.indexOf(column);
        }

        @Override
        public int getRowCount() {
            try {
                return tournament.numberOfPlayers();
            } catch (RemoteException e) {
                return 0;
            }
        }

        @Override
        public int getColumnCount() {
            return displayedColumns.size();
        }

        @Override
        public String getColumnName(int col) {
            Column column = getColumn(col);
            switch (column) {
                case REGISTRATION: return locale.getString("R");
                case LAST_NAME:    return locale.getString("player.last_name");
                case FIRST_NAME:   return locale.getString("player.first_name");
                case RANK:         return locale.getString("player.rank");
                case COUNTRY:      return locale.getString("player.country");
                case CLUB:         return locale.getString("player.club");
                case RATING:       return locale.getString("player.rating");
                case GRADE:        return locale.getString("player.grade");
                case SMMS:         return locale.getString("player.smms");
                default:           return null;
            }
        }

        @Override
        public Object getValueAt(int row, int col) {
            try {
                Player player = getPlayer(row);
                Column column = getColumn(col);
                switch (column) {
                    case REGISTRATION: return player.getRegisteringStatus() == PlayerRegistrationStatus.FINAL ? "F" : "P";
                    case LAST_NAME:    return player.getName();
                    case FIRST_NAME:   return player.getFirstName();
                    case RANK:         return Player.convertIntToKD(player.getRank());
                    case COUNTRY:      return player.getCountry();
                    case CLUB:         return player.getClub();
                    case RATING:       return player.getRating();
                    case GRADE:        return player.getStrGrade();
                    case SMMS:         return player.smms(tournament.getTournamentParameterSet().getGeneralParameterSet());
                    default:           return null;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                return "ERROR";
            }
        }

        @Override
        public Class getColumnClass(int column) {
            switch (getColumn(column)) {
                case SMMS:
                case RATING:
                    return Integer.class;

                default:
                    return String.class;
            }
        }
    }

    private static class SmmsCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            TableModel model = (TableModel) table.getModel();
            try {
                if (model.getColumn(column) == Column.SMMS &&
                    model.getPlayer(table.getRowSorter().convertRowIndexToModel(row)).isSmmsByHand()) {
                    cell.setFont(cell.getFont().deriveFont(Font.BOLD));
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return cell;
        }
    }

    public enum Column implements TableColumnConfig {
        REGISTRATION(10, SwingConstants.CENTER),
        LAST_NAME(110),
        FIRST_NAME(80),
        COUNTRY(30),
        CLUB(40),
        SMMS(40, SwingConstants.RIGHT),
        RANK(30, SwingConstants.RIGHT),
        RATING(40, SwingConstants.RIGHT),
        GRADE(25, SwingConstants.RIGHT);

        private final int prefWidth;
        private final int horizontalAlignment;

        Column(int prefWidth) {
            this(prefWidth, SwingUtilities.LEFT);
        }

        Column(int prefWidth, int horizontalAlignment) {
            this.prefWidth = prefWidth;
            this.horizontalAlignment = horizontalAlignment;
        }

        @Override
        public int prefWidth() {
            return prefWidth;
        }

        @Override
        public int horizontalAlignment() {
            return horizontalAlignment;
        }

        @Override
        public TableCellRenderer tableCellRenderer() {
            if (this == SMMS) {
                return new SmmsCellRenderer();
            } else {
                return TableColumnConfig.super.tableCellRenderer();
            }
        }
    }

    public interface PlayerDoubleClickListener {
        void onPlayerDoubleClicked(Player player);
    }

    private static final Comparator<String> rankComparator = new Comparator<String>() {
        private final Pattern rankPattern = Pattern.compile("(\\d{1,2})([KD])");

        @Override
        public int compare(String a, String b) {
            Matcher am = rankPattern.matcher(a);
            Matcher bm = rankPattern.matcher(b);
            if (am.find() && bm.find()) {
                int c = am.group(2).compareTo(bm.group(2));
                if (c != 0) return c;
                return (Integer.parseInt(am.group(1)) - Integer.parseInt(bm.group(1))) * ("D".equals(am.group(2)) ? -1 : 1);
            } else {
                throw new IllegalStateException();
            }
        }
    };
}
