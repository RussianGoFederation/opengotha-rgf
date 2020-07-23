/*
 * JFrUpdateRatings.java
 */
package info.vannier.gotha;

import net.miginfocom.swing.MigLayout;
import ru.gofederation.gotha.model.Rating;
import ru.gofederation.gotha.model.RatingListFactory;
import ru.gofederation.gotha.model.RatingListType;
import ru.gofederation.gotha.presenter.PlayersQuickCheckTableModel;
import ru.gofederation.gotha.printing.PlayerListPrinter;
import ru.gofederation.gotha.ui.Dialog;
import ru.gofederation.gotha.ui.PrinterSettings;
import ru.gofederation.gotha.ui.RatingListControls;
import ru.gofederation.gotha.ui.component.RatingListComboBox;
import ru.gofederation.gotha.util.GothaLocale;

import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import static ru.gofederation.gotha.model.RatingOrigin.EGF;

/**
 *
 * @author Luc Vannier
 */
public class JFrUpdateRatings extends javax.swing.JFrame implements RatingListControls.Listener {
    private static final long REFRESH_DELAY = 2000;
    private long lastComponentsUpdateTime = 0;

    private static final int NAME_COL           = 0;
    private static final int FIRSTNAME_COL      = NAME_COL + 1;
    private static final int COUNTRY_COL        = FIRSTNAME_COL + 1;
    private static final int CLUB_COL           = COUNTRY_COL + 1;
    private static final int RANK_COL           = CLUB_COL + 1;
    public static  final int RATINGORIGIN_COL   = RANK_COL + 1;
    public static  final int RATING_COL         = RATINGORIGIN_COL + 1;
    public static  final int NEWRATING_COL      = RATING_COL + 1;
    public static  final int PLAYERID_COL       = NEWRATING_COL + 1;
    public static  final int STATUS_COL         = PLAYERID_COL + 1;
    public static  final int RATINGLIST_COL     = STATUS_COL + 1;

    private RatingListControls ratingListControls = new RatingListControls();
    private int playersSortType = PlayerComparator.NAME_ORDER;
    private ArrayList<Player> alSelectedPlayersToKeepSelected = new ArrayList<Player>();
    private PlayersURTableCellRenderer urtCellRenderer = new PlayersURTableCellRenderer();

    private TournamentInterface tournament;

    /** Rating List */
    private RatingList ratingList = new RatingList();

    int activeRow = 0;

	private GothaLocale locale;

    /** Creates new form JFrUpdateRatings */
    public JFrUpdateRatings(TournamentInterface tournament) throws RemoteException{
		this.locale = GothaLocale.getCurrentLocale();

        this.tournament = tournament;
        initComponents();
        onRatingListSelected(ratingListControls.getSelectedRatingListType());
        customInitComponents();
        setupRefreshTimer();
    }

    private volatile boolean running = true;
    javax.swing.Timer timer = null;
    private void setupRefreshTimer() {
        ActionListener taskPerformer;
        taskPerformer = evt -> {
            if (!running){
                timer.stop();
            }
            try {
                if (tournament.getLastTournamentModificationTime() > lastComponentsUpdateTime) {
                    updateAllViews();
                }
            } catch (RemoteException ex) {
                Logger.getLogger(JFrGamesResults.class.getName()).log(Level.SEVERE, null, ex);
            }
        };
        timer = new javax.swing.Timer((int) REFRESH_DELAY, taskPerformer);
        timer.start();
    }

    private void customInitComponents() throws RemoteException {
        initRatingListRDBControls();
        this.resetRatingListControls();

        initPnlPlayers();

        updateAllViews();

        getRootPane().setDefaultButton(this.btnUpdateSelRatings);
    }

        private void initRatingListRDBControls(){
        // Use the preferred rating list as in Preferences
        Preferences prefs = Preferences.userRoot().node(Gotha.strPreferences + "/playersmanager");
        String defRL = prefs.get("defaultratinglist", "" );
        RatingListType rlType;
        try{
            rlType = RatingListType.fromId(Integer.parseInt(defRL));
        }catch(Exception e){
            rlType = RatingListType.UND;
        }
        ratingListControls.setSelectedRatingListType(rlType);
    }

    private void initPnlPlayers() {
        JFrGotha.formatColumn(this.tblPlayers, NAME_COL, locale.getString("player.last_name"), 110, JLabel.LEFT, JLabel.LEFT);
        JFrGotha.formatColumn(this.tblPlayers, FIRSTNAME_COL, locale.getString("player.first_name"), 70, JLabel.LEFT, JLabel.LEFT);
        JFrGotha.formatColumn(this.tblPlayers, COUNTRY_COL, locale.getString("player.country"), 30, JLabel.LEFT, JLabel.LEFT);
        JFrGotha.formatColumn(this.tblPlayers, CLUB_COL, locale.getString("player.club"), 40, JLabel.LEFT, JLabel.LEFT);
        JFrGotha.formatColumn(this.tblPlayers, RANK_COL, locale.getString("player.rank"), 30, JLabel.RIGHT, JLabel.RIGHT);
        JFrGotha.formatColumn(this.tblPlayers, RATINGORIGIN_COL, locale.getString("player.rating_origin"), 30, JLabel.RIGHT, JLabel.RIGHT);
        JFrGotha.formatColumn(this.tblPlayers, RATING_COL, locale.getString("player.rating"), 40, JLabel.RIGHT, JLabel.RIGHT);

        this.updatePnlPlayers(alSelectedPlayersToKeepSelected, ratingListControls.getSelectedRatingListType());
    }

    private void updateSCPPlayersColTitles(RatingListType rlType) {
        JFrGotha.formatColumn(this.tblPlayers, RATINGLIST_COL, locale.format("rating_list.name", locale.getString(rlType.getL10nKey())), 220, JLabel.CENTER, JLabel.CENTER);

        switch (rlType){
            case EGF:
                JFrGotha.formatColumn(this.tblPlayers, NEWRATING_COL, locale.getString("rating_list.egf_rt"), 40, JLabel.RIGHT, JLabel.RIGHT);
                JFrGotha.formatColumn(this.tblPlayers, PLAYERID_COL, locale.getString("rating_list.egf_pin"),70, JLabel.LEFT, JLabel.LEFT);
                break;
            case FFG:
                JFrGotha.formatColumn(this.tblPlayers, NEWRATING_COL, locale.getString("rating_list.ffg_rt"), 40, JLabel.RIGHT, JLabel.RIGHT);
                JFrGotha.formatColumn(this.tblPlayers, PLAYERID_COL, locale.getString("rating_list.ffg_lic"),70, JLabel.LEFT, JLabel.LEFT);
                break;
            case AGA:
                JFrGotha.formatColumn(this.tblPlayers, NEWRATING_COL, locale.getString("rating_list.aga_rt"), 40, JLabel.RIGHT, JLabel.RIGHT);
                JFrGotha.formatColumn(this.tblPlayers, PLAYERID_COL, locale.getString("rating_list.aga_id"), 70, JLabel.LEFT, JLabel.LEFT);
                break;
            case RGF:
                JFrGotha.formatColumn(this.tblPlayers, NEWRATING_COL, locale.getString("rating_list.rgf_rt"), 40, JLabel.RIGHT, JLabel.RIGHT);
                JFrGotha.formatColumn(this.tblPlayers, PLAYERID_COL, locale.getString("rating_list.rgf_id"), 70, JLabel.LEFT, JLabel.LEFT);
                break;
            default:
                System.out.println("btnUpdateRatingListActionPerformed : Internal error");
                return;
        }

        TableColumnModel tcm = this.tblPlayers.getColumnModel();
        if (rlType == RatingListType.FFG){
            tcm.getColumn(STATUS_COL).setMaxWidth(16);
            tcm.getColumn(STATUS_COL).setMinWidth(16);
        }
        else{
            tcm.getColumn(STATUS_COL).setMinWidth(0);
            tcm.getColumn(STATUS_COL).setMaxWidth(0);
        }


    }

    private void initComponents() {
        btnHelp = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        pnlPlayersList = new javax.swing.JPanel();
        btnPrint = new javax.swing.JButton();
        scpPlayers = new javax.swing.JScrollPane();
        tblPlayers = new javax.swing.JTable();
        btnUpdateAllRatings = new javax.swing.JButton();
        btnUpdateSelRatings = new javax.swing.JButton();
        lblRatingList = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(locale.getString("player.update_ratings"));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new MigLayout("insets dialog, flowy"));

        pnlPlayersList.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("player.players")));
        pnlPlayersList.setLayout(new MigLayout("insets panel"));

        ratingListControls.addListener(this);
        ratingListControls.setForceUseRatingList(true);
        pnlPlayersList.add(ratingListControls);

        lblRatingList.setText("No rating list has been loaded yet");
        pnlPlayersList.add(lblRatingList, "ax right, ay bottom, wrap");

        tblPlayers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                locale.getString("player.last_name"),
                locale.getString("player.first_name"),
                locale.getString("player.country_s"),
                locale.getString("player.club"),
                locale.getString("player.rank_s"),
                locale.getString("player.rating"),
                locale.getString("player.rating_origin"),
                locale.getString("player.new_rating"),
                locale.getString("rating_list.egf_pin"),
                locale.getString("player.status"),
                locale.getString("rating_list")
            }
        ) {
            public Class getColumnClass(int columnIndex) {
                return columnIndex == RATINGLIST_COL ? RatedPlayer.class : String.class;
            }
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex == RATINGLIST_COL;
            }
        });
        tblPlayers.getColumnModel().getColumn(RATINGLIST_COL).setCellEditor(new TableCellEditor());
        tblPlayers.getModel().addTableModelListener(event -> {
            if (event.getType() == TableModelEvent.UPDATE && event.getColumn() == RATINGLIST_COL) {
                for (int row = event.getFirstRow(); row <= event.getLastRow(); row++) {
                    this.updateRLCellsWithRP(row, (RatedPlayer) tblPlayers.getModel().getValueAt(row, event.getColumn()));
                }
            }
        });
        scpPlayers.setViewportView(tblPlayers);

        pnlPlayersList.add(scpPlayers, "spanx 2, push, grow, wrap");

        btnUpdateAllRatings.setText(locale.getString("player.update_rating_all"));
        btnUpdateAllRatings.addActionListener(this::btnUpdateAllRatingsActionPerformed);
        pnlPlayersList.add(btnUpdateAllRatings, "spanx 2, growx, wrap");

        btnUpdateSelRatings.setText(locale.getString("player.update_rating_selected"));
        btnUpdateSelRatings.addActionListener(this::btnUpdateSelRatingsActionPerformed);
        pnlPlayersList.add(btnUpdateSelRatings, "spanx 2, growx, wrap");

        btnPrint.setText(locale.getString("btn.print"));
        btnPrint.addActionListener(this::btnPrintActionPerformed);
        pnlPlayersList.add(btnPrint, "spanx 2, growx, wrap");

        getContentPane().add(pnlPlayersList, "push, grow");

        btnHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/info/vannier/gotha/gothalogo16.jpg")));
        btnHelp.setText(locale.getString("btn.help"));
        btnHelp.addActionListener(this::btnHelpActionPerformed);
        getContentPane().add(btnHelp, "split 2, flowx,  tag help");

        btnClose.setText(locale.getString("btn.close"));
        btnClose.addActionListener(this::btnCloseActionPerformed);
        getContentPane().add(btnClose, "tag cancel");

        pack();
    }

    private void btnHelpActionPerformed(java.awt.event.ActionEvent evt) {
        Gotha.displayGothaHelp("Update ratings frame");
    }

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {
        cleanClose();
    }

    private void cleanClose(){
        running = false;
        dispose();
    }

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            ArrayList<Player> players = tournament.playersList();
            players.sort(Comparator.comparing(Player::fullName));
            TableModel model = new PlayersQuickCheckTableModel(players, tournament.getTournamentParameterSet().getGeneralParameterSet().getNumberOfRounds());
            PlayerListPrinter printer = new PlayerListPrinter(model);
            Dialog dialog = new Dialog(this, new PrinterSettings(printer), locale.getString("printing.print_setup"), true);
            dialog.setVisible(true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void updateRLCellsWithRP(int row, RatedPlayer rp){
        DefaultTableModel model = (DefaultTableModel)tblPlayers.getModel();

        String strRating = "????";
        if (rp!=null) strRating = "" + rp.getStdRating();

        RatingListType rlType = ratingListControls.getSelectedRatingListType();
        String strPlayerId = "";
        String strStatus = "";
        switch(rlType){
            case EGF:
                if (rp!=null) strPlayerId = rp.getEgfPin();
                break;
            case FFG:
                if (rp!=null) strPlayerId = rp.getFfgLicence();
                if (rp!=null) strStatus = rp.getFfgLicenceStatus();
                break;
            case AGA:
                if (rp!=null) strPlayerId = rp.getAgaId();
                break;
            case RGF:
                if (rp != null) strPlayerId = Integer.toString(rp.getRgfId());
                break;
            default:
                System.out.println("btnUpdateRatingListActionPerformed : Internal error");
                return;
        }

        model.setValueAt(strRating, row, JFrUpdateRatings.NEWRATING_COL);
        model.setValueAt(strPlayerId, row, JFrUpdateRatings.PLAYERID_COL);
        model.setValueAt(strStatus, row, JFrUpdateRatings.STATUS_COL);
    }

    private ArrayList<Player> selectedPlayersList(JTable tbl){
        ArrayList<Player> alSelectedPlayers = new ArrayList<Player>();

        // gather selected players into alSelectedPlayers
        for ( int iRow = 0; iRow < tbl.getModel().getRowCount(); iRow++){
            if (tbl.isRowSelected(iRow)){
                String name = (String)tbl.getModel().getValueAt(iRow, NAME_COL);
                String firstName = (String)tbl.getModel().getValueAt(iRow, FIRSTNAME_COL);
                Player p = null;
                try {
                    p = tournament.getPlayerByKeyString(name + firstName);
                } catch (RemoteException ex) {
                    Logger.getLogger(JFrPlayersQuickCheck.class.getName()).log(Level.SEVERE, null, ex);
                }
                alSelectedPlayers.add(p);
            }
        }
        return alSelectedPlayers;
    }

    private void btnUpdateAllRatingsActionPerformed(java.awt.event.ActionEvent evt) {
        ArrayList<Player> alP = null;
        try {
            alP = tournament.playersList();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrUpdateRatings.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (alP.isEmpty()) {
            JOptionPane.showMessageDialog(this, locale.getString("player.select_at_least_one"), locale.getString("alert.message"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        this.updateRatings(alP);

        this.tournamentChanged();
    }

    private void updateRatings(ArrayList<Player> alP){
        DefaultTableModel model = (DefaultTableModel)tblPlayers.getModel();

        for (Player p : alP){
            // Find the player's row
            int row = -1;
            for (int r = 0; r < tblPlayers.getRowCount(); r++){
                String name = (String)model.getValueAt(r, JFrUpdateRatings.NAME_COL);
                if (!name.equals(p.getName())) continue;
                String firstName = (String)model.getValueAt(r, JFrUpdateRatings.FIRSTNAME_COL);
                if (!firstName.equals(p.getFirstName())) continue;
                row = r;
                break;
            }
            String egfPin = (String)model.getValueAt(row, JFrUpdateRatings.PLAYERID_COL);
            if (egfPin.equals("")){
                continue;
            }
            try{
                String strNewRating = (String)model.getValueAt(row, JFrUpdateRatings.NEWRATING_COL);
                int newRating = Integer.parseInt(strNewRating);
                p.setEgfPin(egfPin);
                p.setRating(new Rating(EGF, newRating));
                tournament.modifyPlayer(p, p);
            }catch(Exception e){
                continue;
            }
        }
    }


    private void btnUpdateSelRatingsActionPerformed(java.awt.event.ActionEvent evt) {
        ArrayList<Player> alP = this.selectedPlayersList(this.tblPlayers);
        // Keep a track of selected Players
        alSelectedPlayersToKeepSelected = new ArrayList<Player>(alP);

        if (alP.isEmpty()) {
            JOptionPane.showMessageDialog(this, locale.getString("player.select_at_least_one"), locale.getString("alert.message"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        this.updateRatings(alP);

        this.tournamentChanged();
    }

    private void formWindowClosed(java.awt.event.WindowEvent evt) {
        this.ratingList = null;
        Runtime.getRuntime().gc();
    }

    @Override
    public void onRatingListSelected(RatingListType rlType) {
        urtCellRenderer.ratingList = ratingList;
        this.resetRatingListControls();
        ArrayList<Player> playersList = null;
        try {
            playersList = tournament.playersList();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrUpdateRatings.class.getName()).log(Level.SEVERE, null, ex);
        }
        updatePnlPlayers(playersList, rlType);
    }

    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        cleanClose();
    }

    private void resetRatingListControls() {
        RatingListType rlType = ratingListControls.getSelectedRatingListType();

        String strRLType = locale.getString(rlType.getL10nKey());

        this.useRatingList(rlType);

        this.btnUpdateAllRatings.setText(locale.format("player.update_rating_all", strRLType));
        this.btnUpdateSelRatings.setText(locale.format("player.update_rating_selected", strRLType));

        this.updateSCPPlayersColTitles(rlType);
    }

    // See also JFrPlayersManager.useRatingList, which should stay a clone
    private void useRatingList(RatingListType typeRatingList) {
        try {
            lblRatingList.setText(locale.format("rating_list.searching", locale.getString(typeRatingList.getL10nKey())));
            ratingList = RatingListFactory.instance().loadDefaultFile(typeRatingList);
        } catch (IOException e) {
            ratingList = new RatingList();
        }
        int nbPlayersInRL = ratingList.getALRatedPlayers().size();
        if (nbPlayersInRL == 0) {
            ratingList.setRatingListType(RatingListType.UND);
            lblRatingList.setText(locale.getString("rating_list.not_loaded"));
        } else {
            lblRatingList.setText(
                locale.format("rating_list.stats",
                              locale.getString(ratingList.getRatingListType().getL10nKey()),
                              ratingList.getStrPublicationDate(),
                              nbPlayersInRL));
        }
    }

    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnHelp;
    private javax.swing.JButton btnPrint;
    private javax.swing.JButton btnUpdateAllRatings;
    private javax.swing.JButton btnUpdateSelRatings;
    private javax.swing.JLabel lblRatingList;
    private javax.swing.JPanel pnlPlayersList;
    private javax.swing.JScrollPane scpPlayers;
    private javax.swing.JTable tblPlayers;

    private void tournamentChanged() {
        try {
            tournament.setLastTournamentModificationTime(tournament.getCurrentTournamentTime());
        } catch (RemoteException ex) {
            Logger.getLogger(JFrPlayersManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        updateAllViews();
    }

    private void updateAllViews() {
        try {
            if (!tournament.isOpen()) cleanClose();
            this.lastComponentsUpdateTime = tournament.getCurrentTournamentTime();
            setTitle(locale.format("player.update_ratings", tournament.getFullName()));
        } catch (RemoteException ex) {
            Logger.getLogger(JFrPlayersManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        updateComponents();
    }

    private void updateComponents(){
        ArrayList<Player> playersList = null;
        try {
            playersList = tournament.playersList();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrUpdateRatings.class.getName()).log(Level.SEVERE, null, ex);
        }
        updatePnlPlayers(playersList, ratingListControls.getSelectedRatingListType());
    }

    private void updatePnlPlayers(ArrayList<Player> playersList, RatingListType rlType){
        this.updateSCPPlayersColTitles(rlType);
        this.pnlPlayersList.setVisible(true);


        DefaultTableModel model = (DefaultTableModel)tblPlayers.getModel();

        ArrayList<Player> displayedPlayersList = new ArrayList<>(playersList);

        PlayerComparator playerComparator = new PlayerComparator(playersSortType);
        Collections.sort(displayedPlayersList, playerComparator);

        model.setRowCount(displayedPlayersList.size());

        for (Player p:displayedPlayersList){
            int line = displayedPlayersList.indexOf(p);
            model.setValueAt(p.getName(), line, JFrUpdateRatings.NAME_COL);
            model.setValueAt(p.getFirstName(), line, JFrUpdateRatings.FIRSTNAME_COL);
            model.setValueAt(p.getCountry(), line, JFrUpdateRatings.COUNTRY_COL);
            model.setValueAt(p.getClub(), line, JFrUpdateRatings.CLUB_COL);
            model.setValueAt(p.getRank().toString(), line, JFrUpdateRatings.RANK_COL);
            model.setValueAt(p.getRating(), line, JFrUpdateRatings.RATING_COL);
            model.setValueAt(p.getRatingOrigin(), line, JFrUpdateRatings.RATINGORIGIN_COL);

            //Find the player in rating list
            RatedPlayer rp = ratingList.getRatedPlayer(p);
            updateRLCellsWithRP(line, rp);
            model.setValueAt(rp, line, JFrUpdateRatings.RATINGLIST_COL);
        }

        for (int nCol = 0; nCol < this.tblPlayers.getColumnCount(); nCol++){
            TableColumn col = tblPlayers.getColumnModel().getColumn(nCol);
            col.setCellRenderer(/*new PlayersURTableCellRenderer()*/urtCellRenderer);
        }


        // Reselect players that may have been deselected by this update
        for (Player p:alSelectedPlayersToKeepSelected){
            int iSel = displayedPlayersList.indexOf(p);
            if ( iSel >= 0) tblPlayers.addRowSelectionInterval(iSel, iSel);
        }
    }

    class TableCellEditor extends AbstractCellEditor implements javax.swing.table.TableCellEditor {
        final RatingListComboBox component = new RatingListComboBox();

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int rowIndex, int vColIndex) {
            component.setRatingList(ratingList);
            component.setSelectedIndex(ratingList.indexOf((RatedPlayer) value));
            return component;
        }

        @Override
        public Object getCellEditorValue() {
            return component.getSelectedItem();
        }
    }

    static class PlayersURTableCellRenderer extends JLabel implements TableCellRenderer {
        private final DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();
        private RatingList ratingList = null;

        // This method is called each time a cell in a column
        // using this renderer needs to be rendered.
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int colIndex) {
            Component comp = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIndex, colIndex);
            // TODO: theme
            comp.setBackground(Color.WHITE);
            comp.setForeground(Color.BLACK);

            if (value instanceof RatedPlayer && null != ratingList) {
                ((JLabel) comp).setText(ratingList.getRatedPlayerString((RatedPlayer) value));
            }

            TableModel model = table.getModel();
            if (colIndex == JFrUpdateRatings.RATING_COL){
                int rating = (Integer) model.getValueAt(rowIndex, JFrUpdateRatings.RATING_COL);
                String ratingOrigin = model.getValueAt(rowIndex, JFrUpdateRatings.RATINGORIGIN_COL).toString();

                int newRating = -9999;
                try{
                    String strNewRating = (String) model.getValueAt(rowIndex, JFrUpdateRatings.NEWRATING_COL);
                    newRating = Integer.parseInt(strNewRating);
                }catch(Exception e){
                    newRating = -9999;
                }
                if (newRating != -9999){
                    boolean bSame = true;
                    if (newRating != rating) bSame = false;
                    if (!ratingOrigin.equals("EGF")) bSame = false;
                    if (bSame)
                        comp.setForeground(Color.BLACK);
                    else
                        comp.setForeground(Color.RED);
                }
            }

            if (colIndex == JFrUpdateRatings.NEWRATING_COL){
                Integer nRating = (Integer)model.getValueAt(rowIndex, JFrUpdateRatings.RATING_COL);
                int rating = nRating.intValue();
                String ratingOrigin = model.getValueAt(rowIndex, JFrUpdateRatings.RATINGORIGIN_COL).toString();

                int newRating = -9999;
                try{
                    String strNewRating = (String)model.getValueAt(rowIndex, JFrUpdateRatings.NEWRATING_COL);
                    newRating = Integer.parseInt(strNewRating);
                }catch(Exception e){
                    newRating = -9999;
                }
                if (newRating != -9999){
                    boolean bSame = true;
                    if (newRating != rating) bSame = false;
                    if (!ratingOrigin.equals("EGF")) bSame = false;
                    if (bSame)
                        comp.setForeground(Color.BLACK);
                    else
                        comp.setForeground(Color.BLUE);
                }
                else{
                    comp.setForeground(Color.RED);
                }
            }


            if (colIndex == JFrUpdateRatings.NEWRATING_COL ||
                colIndex == JFrUpdateRatings.PLAYERID_COL ||
                colIndex == JFrUpdateRatings.RATINGLIST_COL)
                comp.setBackground(Color.LIGHT_GRAY);

            if (colIndex == JFrUpdateRatings.STATUS_COL){
                String strStatus = (String)model.getValueAt(rowIndex, JFrUpdateRatings.STATUS_COL);
                if (strStatus.equals("e")) comp.setBackground(Color.GREEN);
                else if (strStatus.equals("L")) comp.setBackground(Color.GREEN);
                else if (strStatus.equals("C")) comp.setBackground(Color.CYAN);
                else comp.setBackground(Color.RED);

            }

            return comp;
        }
    }
}

