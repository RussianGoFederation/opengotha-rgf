/*
 * JFrPlayersQuickCheck.java
 */

package info.vannier.gotha;

import net.miginfocom.swing.MigLayout;
import ru.gofederation.gotha.model.Rating;
import ru.gofederation.gotha.presenter.PlayersQuickCheckTableModel;
import ru.gofederation.gotha.printing.PlayerListPrinter;
import ru.gofederation.gotha.ui.Dialog;
import ru.gofederation.gotha.ui.FrameBase;
import ru.gofederation.gotha.ui.PrinterSettings;
import ru.gofederation.gotha.util.GothaLocale;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ru.gofederation.gotha.model.PlayerRegistrationStatus.FINAL;
import static ru.gofederation.gotha.model.PlayerRegistrationStatus.PRELIMINARY;

/**
 *
 * @author  Administrateur
 */
public class JFrPlayersQuickCheck extends javax.swing.JFrame{
    private static final long REFRESH_DELAY = 2000;
    private long lastComponentsUpdateTime = 0;

    public  static final int REG_COL = 0;
    private static final int NAME_COL = 1;
    private static final int FIRSTNAME_COL = 2;
    private static final int COUNTRY_COL = 3;
    private static final int CLUB_COL = 4;
    public static final int RANK_COL = 5;
    public static final int RATING_COL = 6;
    public static final int PARTICIPATING_COL0 = 7;

    private int playersSortType = PlayerComparator.NAME_ORDER;
    private ArrayList<Player> alSelectedPlayersToKeepSelected = new ArrayList<Player>();

    private TournamentInterface tournament;

    private final GothaLocale locale = GothaLocale.getCurrentLocale();

    /**
     * Creates new form JFrPlayersQuickCheck
     */
    public JFrPlayersQuickCheck() {
        initComponents();
        setupRefreshTimer();
    }

    private volatile boolean running = true;
    javax.swing.Timer timer = null;
    private void setupRefreshTimer() {
        ActionListener taskPerformer;
        taskPerformer = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
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
            }
        };
        timer = new javax.swing.Timer((int) REFRESH_DELAY, taskPerformer);
        timer.start();
    }

    public JFrPlayersQuickCheck(TournamentInterface tournament) throws RemoteException{
        this.tournament = tournament;

        initComponents();
        customInitComponents();
        setupRefreshTimer();
    }

    private void initComponents() {
        pupRegisteredPlayers = new javax.swing.JPopupMenu();
        mniSortByName = new javax.swing.JMenuItem();
        mniSortByRank = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        mniCancel = new javax.swing.JMenuItem();
        btnClose = new javax.swing.JButton();
        pnlPlayersList = new javax.swing.JPanel();
        lblPlFin = new javax.swing.JLabel();
        lblPlPre = new javax.swing.JLabel();
        txfNbPlFin = new javax.swing.JTextField();
        txfNbPlPre = new javax.swing.JTextField();
        scpRegisteredPlayers = new javax.swing.JScrollPane();
        tblRegisteredPlayers = new javax.swing.JTable();
        btnPrint = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        lblLastRound = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        btnRemovePrePlayers = new javax.swing.JButton();
        btnIncreaseRank = new javax.swing.JButton();
        btnDecreaseRank = new javax.swing.JButton();
        btnSetRegToFin = new javax.swing.JButton();
        btnSetRegToPre = new javax.swing.JButton();
        btnHelp = new javax.swing.JButton();
        btnSetRanksFromRatings = new javax.swing.JButton();
        btnSetRatingsFromRanks = new javax.swing.JButton();
        btnUpdateRatings = new javax.swing.JButton();
        btnModifyRatings = new javax.swing.JButton();

        mniSortByName.setText("Sort by name");
        mniSortByName.addActionListener(this::mniSortByNameActionPerformed);
        pupRegisteredPlayers.add(mniSortByName);

        mniSortByRank.setText("Sort by rank");
        mniSortByRank.addActionListener(this::mniSortByRankActionPerformed);
        pupRegisteredPlayers.add(mniSortByRank);
        pupRegisteredPlayers.add(jSeparator5);

        mniCancel.setText("Cancel");
        mniCancel.addActionListener(this::mniCancelActionPerformed);
        pupRegisteredPlayers.add(mniCancel);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Players Quick check");
        setResizable(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }

            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new MigLayout("flowy, insets dialog", "[]unrel[]rel[]", "[growprio 0][]unrel[][][]unrel[][][]unrel[][]unrel[]"));

        pnlPlayersList.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("player.players")));
        pnlPlayersList.setLayout(new MigLayout("flowy, insets panel", null, "[][]unrel[]unrel[]"));

        lblPlPre.setText(locale.getString("player.players.registered_preliminary"));
        pnlPlayersList.add(lblPlPre);

        lblPlFin.setText(locale.getString("player.players.registered_final"));
        pnlPlayersList.add(lblPlFin);

        tblRegisteredPlayers.setModel(new javax.swing.table.DefaultTableModel(0, 7) {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        });
        tblRegisteredPlayers.setToolTipText("To modify, right click !");
        tblRegisteredPlayers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblRegisteredPlayersMouseClicked(evt);
            }
        });
        tblRegisteredPlayers.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tblRegisteredPlayersKeyPressed(evt);
            }

            public void keyTyped(java.awt.event.KeyEvent evt) {
                tblRegisteredPlayersKeyTyped(evt);
            }
        });
        scpRegisteredPlayers.setViewportView(tblRegisteredPlayers);

        pnlPlayersList.add(scpRegisteredPlayers, "push, grow");

        btnPrint.setText(locale.getString("btn.print"));
        btnPrint.addActionListener(this::btnPrintActionPerformed);
        pnlPlayersList.add(btnPrint, "growx");

        add(pnlPlayersList, "spany 10, push, grow");

        btnClose.setText(locale.getString("btn.close"));
        btnClose.addActionListener(this::btnCloseActionPerformed);
        getContentPane().add(btnClose, "split 2, spanx 3, flowx, tag cancel");

        btnHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/info/vannier/gotha/gothalogo16.jpg")));
        btnHelp.setText(locale.getString("btn.help"));
        btnHelp.addActionListener(this::btnHelpActionPerformed);
        add(btnHelp, "tag help, wrap");

        btnRemovePrePlayers.setText(locale.getString("player.check.btn_remove_preliminary"));
        btnRemovePrePlayers.addActionListener(this::btnRemovePrePlayersActionPerformed);
        add(btnRemovePrePlayers, "spanx 2, growx");

        btnUpdateRatings.setText(locale.getString("player.check.btn_update_ratings"));
        btnUpdateRatings.addActionListener(this::btnUpdateRatingsActionPerformed);
        add(btnUpdateRatings, "spanx 2, pushy, growx, aligny top");

        jLabel1.setFont(jLabel1.getFont().deriveFont(Font.ITALIC));
        jLabel1.setText(locale.getString("player.check.action_on_selected"));
        add(jLabel1, "spanx 2");

        final Font keyHintFont = FrameBase.scaleFont(jLabel13.getFont(), 1.5f);

        jLabel13.setFont(keyHintFont);
        jLabel13.setText("F");
        add(jLabel13, "ay center");

        jLabel16.setFont(keyHintFont);
        jLabel16.setText("P");
        add(jLabel16, "ay center");

        jLabel6.setFont(keyHintFont);
        jLabel6.setText("+");
        add(jLabel6, "ay center");

        jLabel10.setFont(keyHintFont);
        jLabel10.setText("−");
        add(jLabel10, "ay center, wrap");

        btnSetRegToFin.setText(locale.getString("player.check.btn_set_final"));
        btnSetRegToFin.addActionListener(this::btnSetRegToFinActionPerformed);
        add(btnSetRegToFin, "growx");

        btnSetRegToPre.setText(locale.getString("player.check.btn_set_preliminary"));
        btnSetRegToPre.addActionListener(this::btnSetRegToPreActionPerformed);
        add(btnSetRegToPre, "growx");

        btnIncreaseRank.setText(locale.getString("player.check.btn_increase_rank_selected"));
        btnIncreaseRank.addActionListener(this::btnIncreaseRankActionPerformed);
        add(btnIncreaseRank, "growx");

        btnDecreaseRank.setText(locale.getString("player.check.decrease_rank_selected"));
        btnDecreaseRank.addActionListener(this::btnDecreaseRankActionPerformed);
        add(btnDecreaseRank, "growx");

        btnSetRanksFromRatings.setText(locale.getString("player.check.set_ranks_to_ratings"));
        btnSetRanksFromRatings.addActionListener(this::btnSetRanksFromRatingsActionPerformed);
        add(btnSetRanksFromRatings, "growx");

        btnSetRatingsFromRanks.setText(locale.getString("player.check.btn_set_ratings_to_ranks"));
        btnSetRatingsFromRanks.addActionListener(this::btnSetRatingsFromRanksActionPerformed);
        add(btnSetRatingsFromRanks, "growx");

        btnModifyRatings.setText(locale.getString("player.check.btn_modify_ratings"));
        btnModifyRatings.addActionListener(this::btnModifyRatingsActionPerformed);
        add(btnModifyRatings, "growx");

        pack();
    }

    private void btnSetRegToFinActionPerformed(java.awt.event.ActionEvent evt) {
        ArrayList<Player> alP = this.selectedPlayersList(this.tblRegisteredPlayers);
        // Keep a track of selected Players
        alSelectedPlayersToKeepSelected = new ArrayList<Player>(alP);

        if (alP.isEmpty()) {
            JOptionPane.showMessageDialog(this, locale.getString("player.select_at_least_one"), locale.getString("alert.message"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        this.setPlayersRegStatus(alP, 'F');
    }

    private void btnSetRegToPreActionPerformed(java.awt.event.ActionEvent evt) {
        ArrayList<Player> alP = this.selectedPlayersList(this.tblRegisteredPlayers);
        // Keep a track of selected Players
        alSelectedPlayersToKeepSelected = new ArrayList<Player>(alP);

        if (alP.isEmpty()) {
            JOptionPane.showMessageDialog(this, locale.getString("player.select_at_least_one"), locale.getString("alert.message"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        this.setPlayersRegStatus(alP, 'P');
    }

    private void btnDecreaseRankActionPerformed(java.awt.event.ActionEvent evt) {
        ArrayList<Player> alP = this.selectedPlayersList(this.tblRegisteredPlayers);
        // Keep a track of selected Players
        alSelectedPlayersToKeepSelected = new ArrayList<Player>(alP);

        if (alP.isEmpty()) {
            JOptionPane.showMessageDialog(this, locale.getString("player.select_at_least_one"), locale.getString("alert.message"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        this.changePlayersRank(alP, -1);
    }

    private void btnIncreaseRankActionPerformed(java.awt.event.ActionEvent evt) {
        ArrayList<Player> alP = this.selectedPlayersList(this.tblRegisteredPlayers);
        // Keep a track of selected Players
        alSelectedPlayersToKeepSelected = new ArrayList<Player>(alP);

        if (alP.isEmpty()) {
            JOptionPane.showMessageDialog(this, locale.getString("player.select_at_least_one"), locale.getString("alert.message"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        this.changePlayersRank(alP, 1);
    }

    private void btnRemovePrePlayersActionPerformed(java.awt.event.ActionEvent evt) {
        ArrayList<Player> alP;
        try {
            alP = tournament.playersList();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrPlayersQuickCheck.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        int nbP = 0;
        for (Player p: alP){
            if (p.getRegisteringStatus() == PRELIMINARY) nbP++;
        }
        if (nbP == 0) {
            JOptionPane.showMessageDialog(this, locale.getString("player.check.no_player_preliminary"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        int response =  JOptionPane.showConfirmDialog(this, locale.format("player.check.confirm_remove", nbP), locale.getString("alert.message"), JOptionPane.OK_CANCEL_OPTION);
        if (response == JOptionPane.OK_OPTION){
            int nbRemovedPlayers = 0;
            int nbNotRemovedPlayers = 0;
            for (Player p: alP){
                if (p.getRegisteringStatus() == PRELIMINARY){
                    try {
                        tournament.removePlayer(p);
                        nbRemovedPlayers++;
                    } catch (RemoteException ex) {
                        Logger.getLogger(JFrPlayersQuickCheck.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (TournamentException ex) {
                        nbNotRemovedPlayers++;
                        Logger.getLogger(JFrPlayersQuickCheck.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            if (nbNotRemovedPlayers != 0){
                JOptionPane.showMessageDialog(this, locale.format("player.check.could_not_be_removed", nbNotRemovedPlayers),
                            locale.getString("alert.message"), JOptionPane.WARNING_MESSAGE);
            }
            if (nbRemovedPlayers > 0){
                this.tournamentChanged();
            }

        }

    }

    private void mniSortByRankActionPerformed(java.awt.event.ActionEvent evt) {
        playersSortType = PlayerComparator.RANK_ORDER;
        pupRegisteredPlayers.setVisible(false);
        try{
            updatePnlRegisteredPlayers(tournament.playersList());
        } catch (RemoteException ex) {
            Logger.getLogger(JFrPlayersQuickCheck.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void mniSortByNameActionPerformed(java.awt.event.ActionEvent evt) {
        playersSortType = PlayerComparator.NAME_ORDER;
        pupRegisteredPlayers.setVisible(false);
        try{
            updatePnlRegisteredPlayers(tournament.playersList());
        } catch (RemoteException ex) {
            Logger.getLogger(JFrPlayersQuickCheck.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void tblRegisteredPlayersKeyTyped(java.awt.event.KeyEvent evt) {
        ArrayList<Player> alP = this.selectedPlayersList(this.tblRegisteredPlayers);
        // Keep a track of selected Players
        alSelectedPlayersToKeepSelected = new ArrayList<Player>(alP);

        if (alP.isEmpty()) {
            JOptionPane.showMessageDialog(this, locale.getString("player.select_at_least_one"), locale.getString("alert.message"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (evt.getKeyChar() == '+') this.changePlayersRank(alP, 1);
        else if (evt.getKeyChar() == '-') this.changePlayersRank(alP, -1);
        else if (evt.getKeyChar() == 'P' || evt.getKeyChar() == 'p') this.setPlayersRegStatus(alP, 'P');
        else if (evt.getKeyChar() == 'F' || evt.getKeyChar() == 'f') this.setPlayersRegStatus(alP, 'F');

    }

    /** changes rank of players in alP by deltaRank
     */
    private void changePlayersRank(ArrayList<Player> alP, int deltaRank){
        boolean bSomethingHasChanged = false;
        int confirm = JOptionPane.OK_OPTION;
        if (alP.size() > 1) confirm = JOptionPane.showConfirmDialog(this, locale.format("player.check.confirm_change_rank", alP.size()),
                locale.getString("alert.message"), JOptionPane.OK_CANCEL_OPTION);
        if (confirm == JOptionPane.OK_OPTION){
            try {
                for (Player p : alP){
                     p.setRank(p.getRank() + deltaRank);
                     tournament.modifyPlayer(p, p);
                }
            } catch (RemoteException ex) {
                Logger.getLogger(JFrPlayersQuickCheck.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TournamentException ex) {
                Logger.getLogger(JFrPlayersQuickCheck.class.getName()).log(Level.SEVERE, null, ex);
            }
            bSomethingHasChanged = true;
        }

        if (bSomethingHasChanged){
            this.tournamentChanged();
        }
    }

    /**
     * changes rank of players according to rating
     */
    private void setPlayersRanksFromRatings(ArrayList<Player> alP){
        int nbChanged = 0;

        for (Player p : alP){
            int newRank = p.getRating().toRank().getValue();
            if (p.getRank() != newRank)
                nbChanged++;
        }
        int confirm;
        confirm = JOptionPane.showConfirmDialog(this, locale.format("player.check.confirm_change_rank", nbChanged),
                locale.getString("alert.message"), JOptionPane.OK_CANCEL_OPTION);
        if (confirm != JOptionPane.OK_OPTION) return;

        boolean bSomethingHasChanged = false;
        try {
            for (Player p : alP){
                int rating = p.getRating().toRank().getValue();
                int newRank = Player.rankFromRating(p.getRatingOrigin(), rating);
                p.setRank(newRank);
                tournament.modifyPlayer(p, p);
                bSomethingHasChanged = true;
            }
        } catch (RemoteException ex) {
            Logger.getLogger(JFrPlayersQuickCheck.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TournamentException ex) {
            Logger.getLogger(JFrPlayersQuickCheck.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (bSomethingHasChanged){
            this.tournamentChanged();
        }
    }

    /**
     * changes rank of players according to rating
     */
    private void setPlayersRatingsFromRanks(ArrayList<Player> alP){
        int nbChanged = 0;

        for (Player p : alP){
            int rank = p.getRank();
            int newRating = Player.ratingFromRank(p.getRatingOrigin(), rank);
            if (p.getRating().getValue() != newRating)
                nbChanged++;
        }
        int confirm;
        String str = "Rating will be changed for " + nbChanged + " players";
        confirm = JOptionPane.showConfirmDialog(this, locale.format("player.check.confirm_change_rating", nbChanged),
                locale.getString("alert.message"), JOptionPane.OK_CANCEL_OPTION);
        if (confirm != JOptionPane.OK_OPTION) return;

        boolean bSomethingHasChanged = false;
        try {
            for (Player p : alP){
                int rank = p.getRank();
                int newRating = Player.ratingFromRank(p.getRatingOrigin(), rank);
                if (p.getRating().getValue() != newRating){
                    p.setRating(new Rating(p.getRating().getOrigin(), newRating));
                    tournament.modifyPlayer(p, p);
                    bSomethingHasChanged = true;
                }
            }

        } catch (RemoteException ex) {
            Logger.getLogger(JFrPlayersQuickCheck.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TournamentException ex) {
            Logger.getLogger(JFrPlayersQuickCheck.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (bSomethingHasChanged){
            this.tournamentChanged();
        }
    }


    /**
     * changes rank of players according to rating
     */
    private void modifyRatings(ArrayList<Player> alP){
        int nb = alP.size();
        String strResponse =  JOptionPane.showInputDialog(this, locale.format("player.check.input_rating_delta", nb), "0");
        int delta;
        try{
            delta = Integer.parseInt(strResponse);
        }catch(Exception e){
            delta = 0;
        }

        if (delta == 0) return;

        boolean bSomethingHasChanged = false;
        try {
            for (Player p : alP){
                p.setRating(p.getRating().plus(delta));
                tournament.modifyPlayer(p, p);
                bSomethingHasChanged = true;
            }
        } catch (RemoteException ex) {
            Logger.getLogger(JFrPlayersQuickCheck.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TournamentException ex) {
            Logger.getLogger(JFrPlayersQuickCheck.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (bSomethingHasChanged){
            this.tournamentChanged();
        }
    }

    /** sets registration status of alP to newRegStatus
     */
    private void setPlayersRegStatus(ArrayList<Player> alP, char newRegStatus){
        boolean bSomethingHasChanged = false;
        if (newRegStatus == 'P'){
            int nbPlayersMod = 0;
            int nbPlayersPRE = 0;
            for (Player p : alP){
                if (p.getRegisteringStatus() == PRELIMINARY) nbPlayersPRE++;
                else{
                    try {
                        if (!tournament.isPlayerImplied(p)){
                            p.setRegisteringStatus(PRELIMINARY);
                            tournament.modifyPlayer(p, p);
                            nbPlayersMod++;
                        }
                    } catch (RemoteException ex) {
                        Logger.getLogger(JFrPlayersQuickCheck.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (TournamentException ex) {
                        Logger.getLogger(JFrPlayersQuickCheck.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            int nbPlayersNotModifiable = alP.size() - nbPlayersPRE - nbPlayersMod;
            if (nbPlayersNotModifiable >= 1){
                JOptionPane.showMessageDialog(this, locale.format("player.check.already_assigned", nbPlayersNotModifiable), locale.getString("alert.message"), JOptionPane.WARNING_MESSAGE);
            }
            if (nbPlayersMod != 0) bSomethingHasChanged = true;
        }
        if (newRegStatus == 'F'){
            int nbPlayersMod = 0;
            for (Player p : alP){
                if (p.getRegisteringStatus() != FINAL){
                    p.setRegisteringStatus(FINAL);
                    try {
                        tournament.modifyPlayer(p, p);
                    } catch (RemoteException ex) {
                        Logger.getLogger(JFrPlayersQuickCheck.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (TournamentException ex) {
                        Logger.getLogger(JFrPlayersQuickCheck.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    nbPlayersMod++;
                }
            }
            if (nbPlayersMod != 0) bSomethingHasChanged = true;
        }
        if (bSomethingHasChanged){
            this.tournamentChanged();
        }
    }

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {
        cleanClose();
    }

    private void cleanClose(){
        running = false;
        dispose();
    }

    private void tblRegisteredPlayersKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_DELETE){
            ArrayList<Player> alP = this.selectedPlayersList(this.tblRegisteredPlayers);
            if (alP.isEmpty()) {
                JOptionPane.showMessageDialog(this, locale.getString("player.select_at_least_one"), locale.getString("alert.message"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int nbJ = alP.size();
            if (JOptionPane.showConfirmDialog(this, locale.format("player.check.confirm_remove", nbJ), locale.getString("alert.message"),
                    JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
            int nbPlayersRem = removePlayers(alP);
            int nbPlayersNotRem = alP.size() - nbPlayersRem;

            StringBuilder sb = new StringBuilder();;
            int messageType = JOptionPane.INFORMATION_MESSAGE;
            if (nbPlayersNotRem >= 1){
                sb.append(locale.format("player.check.could_not_be_removed", nbPlayersNotRem)).append("\n");
                messageType = JOptionPane.WARNING_MESSAGE;
            }
            sb.append(locale.format("player.check.removed", nbPlayersRem));
            JOptionPane.showMessageDialog(this, sb, locale.getString("alert.message"), messageType);
            if (nbPlayersRem > 0){
                this.tournamentChanged();
            }
        }
    }

    private void tblRegisteredPlayersMouseClicked(java.awt.event.MouseEvent evt) {
        // Left click on participation
        if (evt.getModifiers() == InputEvent.BUTTON1_MASK){
            int iRow = tblRegisteredPlayers.rowAtPoint(evt.getPoint());
            int iCol = tblRegisteredPlayers.columnAtPoint(evt.getPoint());
            System.out.println("iRow = " + iRow + " iCol = " + iCol);
            if (iCol < PARTICIPATING_COL0) return;
            int round = iCol - PARTICIPATING_COL0;
            String name = (String)tblRegisteredPlayers.getModel().getValueAt(iRow, NAME_COL);
            String firstName = (String)tblRegisteredPlayers.getModel().getValueAt(iRow, FIRSTNAME_COL);
            Player p = null;
            try {
                p = tournament.getPlayerByKeyString(name + firstName);
            } catch (RemoteException ex) {
                Logger.getLogger(JFrPlayersQuickCheck.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("p = " + p.getName() + " " + p.getFirstName());
            boolean part[] = p.getParticipating();
            boolean bP = part[round];
            p.setParticipating(round, !bP);
            String strPart = "";
            if (bP) strPart = "V";
            DefaultTableModel model = (DefaultTableModel)tblRegisteredPlayers.getModel();
            model.setValueAt(strPart, iRow, iCol);
            this.tournamentChanged();

        }
        // Right click
        if (evt.getModifiers() != InputEvent.BUTTON3_MASK) return;
        Point p = evt.getLocationOnScreen();
        pupRegisteredPlayers.setLocation(p);
        pupRegisteredPlayers.setVisible(true);

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

    private void btnHelpActionPerformed(java.awt.event.ActionEvent evt) {
        Gotha.displayGothaHelp("Players Quick check frame");
}

    private void mniCancelActionPerformed(java.awt.event.ActionEvent evt) {
        this.pupRegisteredPlayers.setVisible(false);
        this.tblRegisteredPlayers.removeRowSelectionInterval(0, tblRegisteredPlayers.getRowCount() - 1);
}

    private void formWindowClosed(java.awt.event.WindowEvent evt) {
         this.pupRegisteredPlayers.setVisible(false);
    }

    private void btnSetRanksFromRatingsActionPerformed(java.awt.event.ActionEvent evt) {
        ArrayList<Player> alP = this.selectedPlayersList(this.tblRegisteredPlayers);
        // Keep a track of selected Players
        alSelectedPlayersToKeepSelected = new ArrayList<Player>(alP);

        if (alP.isEmpty()) {
            JOptionPane.showMessageDialog(this, locale.getString("player.select_at_least_one"), locale.getString("alert.message"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.setPlayersRanksFromRatings(alP);

    }

    private void btnSetRatingsFromRanksActionPerformed(java.awt.event.ActionEvent evt) {
        ArrayList<Player> alP = this.selectedPlayersList(this.tblRegisteredPlayers);
        // Keep a track of selected Players
        alSelectedPlayersToKeepSelected = new ArrayList<Player>(alP);

        if (alP.isEmpty()) {
            JOptionPane.showMessageDialog(this, locale.getString("player.select_at_least_one"), locale.getString("alert.message"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.setPlayersRatingsFromRanks(alP);

    }

    private void btnUpdateRatingsActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            JFrame jfr = new JFrUpdateRatings(tournament);
            jfr.setVisible(true);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void btnModifyRatingsActionPerformed(java.awt.event.ActionEvent evt) {
        ArrayList<Player> alP = this.selectedPlayersList(this.tblRegisteredPlayers);
        // Keep a track of selected Players
        alSelectedPlayersToKeepSelected = new ArrayList<Player>(alP);

        if (alP.isEmpty()) {
            JOptionPane.showMessageDialog(this, locale.getString("player.select_at_least_one"), locale.getString("alert.message"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.modifyRatings(alP);

    }

    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        cleanClose();
    }

    private void customInitComponents()throws RemoteException{
        initPnlRegisteredPlayers();
        this.updateAllViews();
    }

    private void initPnlRegisteredPlayers() {
        JFrGotha.formatColumn(this.tblRegisteredPlayers, REG_COL, "R", 10, JLabel.LEFT, JLabel.LEFT);
        JFrGotha.formatColumn(this.tblRegisteredPlayers, NAME_COL, locale.getString("player.last_name"),110, JLabel.LEFT, JLabel.LEFT);
        JFrGotha.formatColumn(this.tblRegisteredPlayers, FIRSTNAME_COL, locale.getString("player.first_name"), 80, JLabel.LEFT, JLabel.LEFT);
        JFrGotha.formatColumn(this.tblRegisteredPlayers, COUNTRY_COL, locale.getString("player.country_s"), 30, JLabel.LEFT, JLabel.LEFT);
        JFrGotha.formatColumn(this.tblRegisteredPlayers, CLUB_COL, locale.getString("player.club"), 40, JLabel.LEFT, JLabel.LEFT);
        JFrGotha.formatColumn(this.tblRegisteredPlayers, RANK_COL, locale.getString("player.rank_s"), 30, JLabel.RIGHT, JLabel.RIGHT);
        JFrGotha.formatColumn(this.tblRegisteredPlayers, RATING_COL, locale.getString("player.rating"), 40, JLabel.RIGHT, JLabel.RIGHT);
    }

    private void tournamentChanged(){
        try {
            tournament.setLastTournamentModificationTime(tournament.getCurrentTournamentTime());
        } catch (RemoteException ex) {
            Logger.getLogger(JFrPlayersQuickCheck.class.getName()).log(Level.SEVERE, null, ex);
        }
        updateAllViews();
    }

    private void updateAllViews(){
        try {
            if (!tournament.isOpen()) cleanClose();
            this.lastComponentsUpdateTime = tournament.getCurrentTournamentTime();
            setTitle(locale.format("player.check.window_title", tournament.getFullName()));        } catch (RemoteException ex) {
            Logger.getLogger(JFrPlayersQuickCheck.class.getName()).log(Level.SEVERE, null, ex);
        }

        updateComponents();

    }

    private void updateComponents(){
        ArrayList<Player> playersList = null;
        int numberOfRounds = 0;
        try {
            playersList = tournament.playersList();
            numberOfRounds = tournament.getTournamentParameterSet().getGeneralParameterSet().getNumberOfRounds();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrPlayersQuickCheck.class.getName()).log(Level.SEVERE, null, ex);
        }

        DefaultTableModel model = (DefaultTableModel)tblRegisteredPlayers.getModel();
        model.setColumnCount(PARTICIPATING_COL0 + numberOfRounds);
        initPnlRegisteredPlayers();
        for (int r = PARTICIPATING_COL0; r < PARTICIPATING_COL0 + numberOfRounds; r++) {
            JFrGotha.formatColumn(this.tblRegisteredPlayers, r, "" + (r + 1 - PARTICIPATING_COL0), 20, JLabel.CENTER, JLabel.CENTER);
        }

        updatePnlRegisteredPlayers(playersList);
    }

    private void updatePnlRegisteredPlayers(ArrayList<Player> playersList) {
        this.pnlPlayersList.setVisible(true);

        int nbPreliminary = 0;
        int nbFinal = 0;
        for (Player p : playersList){
            if (p.getRegisteringStatus() == PRELIMINARY) nbPreliminary++;
            if (p.getRegisteringStatus() == FINAL) nbFinal++;
        }
        txfNbPlPre.setText(""+ nbPreliminary);
        txfNbPlFin.setText(""+ nbFinal);
        lblPlPre.setText(locale.format("player.players.registered_preliminary", nbPreliminary));
        lblPlFin.setText(locale.format("player.players.registered_final", nbFinal));

        DefaultTableModel model = (DefaultTableModel)tblRegisteredPlayers.getModel();

        ArrayList<Player> displayedPlayersList = new ArrayList<Player>(playersList);

        PlayerComparator playerComparator = new PlayerComparator(playersSortType);
        Collections.sort(displayedPlayersList, playerComparator);

        model.setRowCount(displayedPlayersList.size());

        int numberOfRounds = 0;
        try {
            numberOfRounds = tournament.getTournamentParameterSet().getGeneralParameterSet().getNumberOfRounds();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }

        TableColumnModel tcm = this.tblRegisteredPlayers.getColumnModel();

        for (int col = PARTICIPATING_COL0; col < PARTICIPATING_COL0 + numberOfRounds; col++){
            TableColumn tc = tcm.getColumn(col);
            tc.setMinWidth(0);
            tc.setMaxWidth(20);
        }

        for (Player p:displayedPlayersList){
            int line = displayedPlayersList.indexOf(p);
            model.setValueAt((p.getRegisteringStatus()==PRELIMINARY)?"P":"F", line, JFrPlayersQuickCheck.REG_COL);
            model.setValueAt(p.getName(), line, JFrPlayersQuickCheck.NAME_COL);
            model.setValueAt(p.getFirstName(), line, JFrPlayersQuickCheck.FIRSTNAME_COL);
            model.setValueAt(p.getCountry(), line, JFrPlayersQuickCheck.COUNTRY_COL);
            model.setValueAt(p.getClub(), line, JFrPlayersQuickCheck.CLUB_COL);
            model.setValueAt(Player.convertIntToKD(p.getRank()), line, JFrPlayersQuickCheck.RANK_COL);
            model.setValueAt(p.getRating(), line, JFrPlayersQuickCheck.RATING_COL);
            boolean[] bPart = p.getParticipating();
            for (int round = 0; round < numberOfRounds; round++){
                String strPart = "";
                if (bPart[round]) strPart = "V";
                model.setValueAt(strPart, line, JFrPlayersQuickCheck.PARTICIPATING_COL0 + round);
            }
        }


        for (int nCol = 0; nCol < this.tblRegisteredPlayers.getColumnCount(); nCol++){
            TableColumn col = tblRegisteredPlayers.getColumnModel().getColumn(nCol);
            col.setCellRenderer(new PlayersQCTableCellRenderer());
        }

        // Reselect players that may have been deselected by this update
        for (Player p:alSelectedPlayersToKeepSelected){
            int iSel = displayedPlayersList.indexOf(p);
            if ( iSel >= 0) tblRegisteredPlayers.addRowSelectionInterval(iSel, iSel);
        }

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

    private int removePlayers(ArrayList<Player> alP){
        int nbPlayersRem = 0;
        for (Player p : alP){
            boolean b = false;
            try {
                b = tournament.isPlayerImplied(p);
            } catch (RemoteException ex) {
                Logger.getLogger(JFrPlayersQuickCheck.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (!b){
                try{
                    Player playerToRemove = tournament.getPlayerByKeyString(p.getName() + p.getFirstName());
                    if(tournament.removePlayer(playerToRemove)) nbPlayersRem++;
                } catch(TournamentException te){
                    JOptionPane.showMessageDialog(this, te.getMessage(), "Message", JOptionPane.ERROR_MESSAGE);
                } catch (RemoteException ex) {
                    Logger.getLogger(JFrPlayersQuickCheck.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return nbPlayersRem;
    }


    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnDecreaseRank;
    private javax.swing.JButton btnHelp;
    private javax.swing.JButton btnIncreaseRank;
    private javax.swing.JButton btnModifyRatings;
    private javax.swing.JButton btnPrint;
    private javax.swing.JButton btnRemovePrePlayers;
    private javax.swing.JButton btnSetRanksFromRatings;
    private javax.swing.JButton btnSetRatingsFromRanks;
    private javax.swing.JButton btnSetRegToFin;
    private javax.swing.JButton btnSetRegToPre;
    private javax.swing.JButton btnUpdateRatings;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JLabel lblLastRound;
    private javax.swing.JLabel lblPlFin;
    private javax.swing.JLabel lblPlPre;
    private javax.swing.JMenuItem mniCancel;
    private javax.swing.JMenuItem mniSortByName;
    private javax.swing.JMenuItem mniSortByRank;
    private javax.swing.JPanel pnlPlayersList;
    private javax.swing.JPopupMenu pupRegisteredPlayers;
    private javax.swing.JScrollPane scpRegisteredPlayers;
    private javax.swing.JTable tblRegisteredPlayers;
    private javax.swing.JTextField txfNbPlFin;
    private javax.swing.JTextField txfNbPlPre;
}

class PlayersQCTableCellRenderer extends JLabel implements TableCellRenderer {
    // This method is called each time a cell in a column
    // using this renderer needs to be rendered.
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int rowIndex, int colIndex) {

        Component comp = new DefaultTableCellRenderer().getTableCellRendererComponent(table,  value, isSelected, hasFocus, rowIndex, colIndex);
        TableModel model = table.getModel();
        String strRegStatus = "" + model.getValueAt(rowIndex, JFrPlayersQuickCheck.REG_COL);

        if (strRegStatus.compareTo("P") == 0) comp.setForeground(Color.RED);
        else comp.setForeground(UIManager.getColor(isSelected ? "Table.selectionForeground" : "Table.foreground"));

        return comp;
    }
}

