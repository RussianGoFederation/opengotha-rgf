/*
 * JFrTeamsManager.java
 */

package info.vannier.gotha;

import net.miginfocom.swing.MigLayout;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import ru.gofederation.gotha.util.GothaLocale;

/**
 *
 * @author Luc Vannier
 */
public class JFrTeamsManager extends javax.swing.JFrame {
    private static final long REFRESH_DELAY = 2000;
    private long lastComponentsUpdateTime = 0;

    private static final int PL_NUMBER_OF_COLS = 4;
    private static final int PL_NAME_COL = 0;

    private static final int PL_COUNTRY_COL = 1;
    private static final int PL_CLUB_COL = 2;
    private static final int PL_RATING_COL = 3;

    private static final int TM_NUMBER_OF_COLS = 7;
    private static final int TM_TEAM_NUMBER_COL = 0;
    private static final int TM_TEAM_NAME_COL = 1;
    private static final int TM_BOARD_NUMBER_COL = 2;
    private static final int TM_PL_NAME_COL = 3;
    private static final int TM_PL_COUNTRY_COL = 4;
    private static final int TM_PL_CLUB_COL = 5;
    private static final int TM_PL_RATING_COL = 6;

    private int teamsSortType = TeamComparator.TEAM_NUMBER_ORDER;

    /**  current Tournament */
    private TournamentInterface tournament;

    private int processedRoundNumber = 0;

    private final GothaLocale locale = GothaLocale.getCurrentLocale();

    /** Creates new form JFrTeamsManager */
    public JFrTeamsManager(TournamentInterface tournament) throws RemoteException{
        this.tournament = tournament;
        processedRoundNumber = tournament.presumablyCurrentRoundNumber();

        initComponents();
        customInitComponents();
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

    private void customInitComponents() throws RemoteException {
        initPlayersComponents();
        initTeamsComponents();
        initDnDIssues();

        updateAllViews();
    }

    private void initPlayersComponents(){
        initPlayersTable();
    }

    private void initTeamsComponents(){
        initTeamsTable();
    }

    // Drag and drop issues
    private void initDnDIssues(){
        this.tblTeamablePlayers.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.tblTeams.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        this.tblTeamablePlayers.setDragEnabled(true);

        this.tblTeams.setTransferHandler(new TransferHandler(){
            @Override
            public boolean canImport(TransferHandler.TransferSupport info){
                JTable.DropLocation dl = (JTable.DropLocation)info.getDropLocation();
                if (dl.getRow() == -1) {
                    return false;
                }
                return true;
            }
            @Override
            public boolean importData(TransferHandler.TransferSupport info){
                if (!info.isDrop()){
                    return false;
                }
                JTable.DropLocation dl = (JTable.DropLocation)info.getDropLocation();
                int indexDest = dl.getRow();

                String data = null;
                try {
                    data = (String)info.getTransferable().getTransferData(DataFlavor.stringFlavor);
                } catch (UnsupportedFlavorException ex) {
                    Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
                }

                // extract player from data
                Player player = JFrTeamsManager.this.findPlayerFromTransferData(data);
                if (player == null){
                    JOptionPane.showMessageDialog(JFrTeamsManager.this, "Player not found", "Message", JOptionPane.ERROR_MESSAGE);
                    return true;
                }
                // Is this player teamable ?
                HashMap<String, Player> hmTeamablePlayers = new HashMap<String, Player>();
                try {
                     hmTeamablePlayers = tournament.teamablePlayersHashMap(processedRoundNumber);
                } catch (RemoteException ex) {
                    Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (hmTeamablePlayers.get(player.getKeyString()) == null){
                    JOptionPane.showMessageDialog(JFrTeamsManager.this, "Not teamable player", "Message", JOptionPane.ERROR_MESSAGE);
                    return true;
                }

               Team team = findTeamFromIndexInTeamsTable(indexDest);
               if (team == null){
                    JOptionPane.showMessageDialog(JFrTeamsManager.this, "Unexisting team", "Message", JOptionPane.ERROR_MESSAGE);
                    return true;
                }

                // What board Number ?
                int teamSize = 1;
                try {
                    teamSize = tournament.getTeamSize();
                } catch (RemoteException ex) {
                    Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                int boardNumber = indexDest % teamSize;
                try {
                    // affect current round + all rounds with member = null
                    tournament.setTeamMember(team, processedRoundNumber, boardNumber, player);
                    for (int r = 0; r < Gotha.MAX_NUMBER_OF_ROUNDS; r++){
                        Player currentP = team.getTeamMember(r, boardNumber);
                        if (currentP == null){
                            tournament.setTeamMember(team, r, boardNumber, player);
                        }
                    }
                } catch (RemoteException ex) {
                    Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                JFrTeamsManager.this.tournamentChanged();

                return true;
            }
        });
    }

    private Player findPlayerFromTransferData(String transferData){
        Player player = null;

        String[] tabSTR = transferData.split("\t", 2);
        String strNF = tabSTR[0];
        if (strNF == null) return null;
        strNF = strNF.trim();
        try {
            player = tournament.getPlayerByKeyString(strNF);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return player;
    }

    private Team findTeamFromIndexInTeamsTable (int index){
        int teamSize = 1;
        try {
            teamSize = tournament.getTeamSize();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        int teamRow = (index / teamSize) * teamSize;
        int teamNumber = Integer.parseInt((String)this.tblTeams.getModel().getValueAt(teamRow, TM_TEAM_NUMBER_COL)) - 1;

        ArrayList<Team> alTeams = null;
        try {
            alTeams = tournament.teamsList();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (Team team : alTeams){
            if (team.getTeamNumber() == teamNumber) return team;
        }
        return null;
    }


    private void initPlayersTable(){
        JTable tbl = this.tblTeamablePlayers;
        final int NAME_WIDTH = 125;
        final int RATING_WIDTH = 40;
        final int COUNTRY_WIDTH = 25;
        final int CLUB_WIDTH = 40;

        JFrGotha.formatColumn(tblTeamablePlayers, PL_NAME_COL, "Last name", NAME_WIDTH, JLabel.LEFT, JLabel.LEFT);
        JFrGotha.formatColumn(tblTeamablePlayers, PL_COUNTRY_COL, "Co", COUNTRY_WIDTH, JLabel.LEFT, JLabel.LEFT);
        JFrGotha.formatColumn(tblTeamablePlayers, PL_CLUB_COL, "Club", CLUB_WIDTH, JLabel.LEFT, JLabel.LEFT);
        JFrGotha.formatColumn(tblTeamablePlayers, PL_RATING_COL, "Rating", RATING_WIDTH, JLabel.RIGHT, JLabel.RIGHT);
    }

    private void initTeamsTable(){
        final int TM_TEAM_NUMBER_WIDTH = 30;
        final int TM_TEAM_NAME_WIDTH = 125;
        final int TM_BOARD_NUMBER_WIDTH = 20;
        final int TM_PL_NAME_WIDTH = 125;
        final int TM_PL_COUNTRY_WIDTH = 25;
        final int TM_PL_CLUB_WIDTH = 40;
        final int TM_PL_RATING_WIDTH = 40;

        JFrGotha.formatColumn(tblTeams, TM_TEAM_NUMBER_COL, "Nr", TM_TEAM_NUMBER_WIDTH, JLabel.RIGHT, JLabel.RIGHT);
        JFrGotha.formatColumn(tblTeams, TM_TEAM_NAME_COL, "Team name", TM_TEAM_NAME_WIDTH, JLabel.LEFT, JLabel.LEFT);
        JFrGotha.formatColumn(tblTeams, TM_BOARD_NUMBER_COL, "Board", TM_BOARD_NUMBER_WIDTH, JLabel.RIGHT, JLabel.RIGHT);
        JFrGotha.formatColumn(tblTeams, TM_PL_NAME_COL, "Player name", TM_PL_NAME_WIDTH, JLabel.LEFT, JLabel.LEFT);
        JFrGotha.formatColumn(tblTeams, TM_PL_COUNTRY_COL, "Co", TM_PL_COUNTRY_WIDTH, JLabel.LEFT, JLabel.LEFT);
        JFrGotha.formatColumn(tblTeams, TM_PL_CLUB_COL, "Club", TM_PL_CLUB_WIDTH, JLabel.LEFT, JLabel.LEFT);
        JFrGotha.formatColumn(tblTeams, TM_PL_RATING_COL, "Rating", TM_PL_RATING_WIDTH, JLabel.RIGHT, JLabel.RIGHT);
    }

    private void updateComponents() {
        this.pupTeams.setVisible(false);
        int teamSize = 0;
        try {
            teamSize = tournament.getTeamSize();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.txfTeamSize.setText("" + teamSize);

        DefaultTableModel teamablePlayersModel = (DefaultTableModel) tblTeamablePlayers.getModel();
        DefaultTableModel teamsModel = (DefaultTableModel) tblTeams.getModel();

        while (teamablePlayersModel.getRowCount() > 0) {
            teamablePlayersModel.removeRow(0);
        }
        while (teamsModel.getRowCount() > 0) {
            teamsModel.removeRow(0);
        }

        this.spnRoundNumber.setValue(this.processedRoundNumber + 1);

        HashMap<String, Player> hmTeamablePlayers = new HashMap<String, Player>();
        try {
            hmTeamablePlayers = tournament.teamablePlayersHashMap(processedRoundNumber);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        ArrayList<Player> alTeamablePlayers = new ArrayList<Player>(hmTeamablePlayers.values());
        PlayerComparator playerComparator = new PlayerComparator(PlayerComparator.RATING_ORDER);
        Collections.sort(alTeamablePlayers, playerComparator);

        this.lblTeamablePlayers.setText(locale.format("tournament.teams.available_players", alTeamablePlayers.size()));

        ArrayList<Team> alTeams = new ArrayList<Team>();
        try {
            alTeams = tournament.teamsList();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.lblNbTeams.setText(locale.format("tournament.teams.count", alTeams.size()));

        fillPlayersTable(alTeamablePlayers, tblTeamablePlayers);
        fillTeamsTable(alTeams, tblTeams);

    }

    private void fillPlayersTable(ArrayList<Player> alP, JTable tblP) {
        ArrayList<Player> alDisplayedPlayers = new ArrayList<Player>(alP);

        for (Player p : alDisplayedPlayers) {
            Object[] row = new Object[PL_NUMBER_OF_COLS];
            row[PL_NAME_COL] = p.fullName();
            row[PL_RATING_COL] = p.getRating();
            row[PL_COUNTRY_COL] = p.getCountry();
            row[PL_CLUB_COL] = p.getClub();

            DefaultTableModel model = (DefaultTableModel) tblP.getModel();
            model.addRow(row);
        }
    }

    private void fillTeamsTable(ArrayList<Team> alT, JTable tblT) {
        ArrayList<Team> alDisplayedTeams = new ArrayList<Team>(alT);

        int teamSize = 0;
        try {
            teamSize = tournament.getTeamSize();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        TeamComparator teamComparator = new TeamComparator(teamsSortType, teamSize);
        Collections.sort(alDisplayedTeams, teamComparator);

        for (Team t : alDisplayedTeams) {
            for (int iTM = 0; iTM < teamSize; iTM++){
                Object[] row = new Object[TM_NUMBER_OF_COLS];
                if (iTM == 0){
                    row[TM_TEAM_NUMBER_COL] = "" + (t.getTeamNumber() + 1);
                    row[TM_TEAM_NAME_COL] = t.getTeamName();
                }
                else{
                    row[TM_TEAM_NUMBER_COL] = "";
                    row[TM_TEAM_NAME_COL] = "";
                }

                row[TM_BOARD_NUMBER_COL] = "" + (iTM + 1);
                Player p = t.getTeamMember(processedRoundNumber, iTM);
                if (p == null){
                    row[TM_PL_NAME_COL] = "";
                    row[TM_PL_RATING_COL] = "";
                    row[TM_PL_COUNTRY_COL] = "";
                    row[TM_PL_CLUB_COL] = "";
                }
                else{
                    row[TM_PL_NAME_COL] = p.getName() + " " + p.getFirstName();
                    row[TM_PL_RATING_COL] = p.getRating();
                    row[TM_PL_COUNTRY_COL] = p.getCountry();
                    row[TM_PL_CLUB_COL] = p.getClub();
                }
                DefaultTableModel model = (DefaultTableModel) tblT.getModel();
                model.addRow(row);
            }
        }
    }

    private void initComponents() {
        pupTeams = new javax.swing.JPopupMenu();
        mniRemoveOneTeam = new javax.swing.JMenuItem();
        mniRemoveAllTeams = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        mniUnteamOneMember = new javax.swing.JMenuItem();
        mniUnteamOneMemberAllRounds = new javax.swing.JMenuItem();
        mniUnteamOneTeam = new javax.swing.JMenuItem();
        mniUnteamOneTeamAllRounds = new javax.swing.JMenuItem();
        mniUnteamAllTeams = new javax.swing.JMenuItem();
        mniUnteamAllTeamsAllRounds = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        mniRenameTeam = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        mniReorderMembersOfOneTeam = new javax.swing.JMenuItem();
        mniReorderMembersOfOneTeamAllRounds = new javax.swing.JMenuItem();
        mniReorderPlayersOfAllTeams = new javax.swing.JMenuItem();
        mniReorderPlayersOfAllTeamsAllRounds = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        mniRenumberTeams = new javax.swing.JMenuItem();
        mniChangeTeamNumber = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        mniCancel = new javax.swing.JMenuItem();
        btnHelp = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        pnlPlayers = new javax.swing.JPanel();
        scpTeamablePlayers = new javax.swing.JScrollPane();
        tblTeamablePlayers = new javax.swing.JTable();
        lblTeamablePlayers = new javax.swing.JLabel();
        pnlTeams = new javax.swing.JPanel();
        scpTeams = new javax.swing.JScrollPane();
        tblTeams = new javax.swing.JTable();
        lblNbTeams = new javax.swing.JLabel();
        btnCreateNewTeam = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        txfTeamSize = new javax.swing.JTextField();
        btnPrint = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        spnRoundNumber = new javax.swing.JSpinner();

        mniRemoveOneTeam.setText("jMenuItem1");
        mniRemoveOneTeam.addActionListener(this::mniRemoveOneTeamActionPerformed);
        pupTeams.add(mniRemoveOneTeam);

        mniRemoveAllTeams.setText("jMenuItem1");
        mniRemoveAllTeams.addActionListener(this::mniRemoveAllTeamsActionPerformed);
        pupTeams.add(mniRemoveAllTeams);
        pupTeams.add(jSeparator4);

        mniUnteamOneMember.addActionListener(this::mniUnteamOneMemberActionPerformed);
        pupTeams.add(mniUnteamOneMember);

        mniUnteamOneMemberAllRounds.addActionListener(this::mniUnteamOneMemberAllRoundsActionPerformed);
        pupTeams.add(mniUnteamOneMemberAllRounds);

        mniUnteamOneTeam.setText("Unteam one team");
        mniUnteamOneTeam.addActionListener(this::mniUnteamOneTeamActionPerformed);
        pupTeams.add(mniUnteamOneTeam);

        mniUnteamOneTeamAllRounds.setText("Unteam one team for all rounds");
        mniUnteamOneTeamAllRounds.addActionListener(this::mniUnteamOneTeamAllRoundsActionPerformed);
        pupTeams.add(mniUnteamOneTeamAllRounds);

        mniUnteamAllTeams.setText("Unteam all teams");
        mniUnteamAllTeams.addActionListener(this::mniUnteamAllTeamsActionPerformed);
        pupTeams.add(mniUnteamAllTeams);

        mniUnteamAllTeamsAllRounds.setText("Unteam all teams for all rounds");
        mniUnteamAllTeamsAllRounds.addActionListener(this::mniUnteamAllTeamsAllRoundsActionPerformed);
        pupTeams.add(mniUnteamAllTeamsAllRounds);
        pupTeams.add(jSeparator1);

        mniRenameTeam.setText("Rename one team");
        mniRenameTeam.addActionListener(this::mniRenameTeamActionPerformed);
        pupTeams.add(mniRenameTeam);
        pupTeams.add(jSeparator2);

        mniReorderMembersOfOneTeam.setText("Reorder players of one team by rating");
        mniReorderMembersOfOneTeam.addActionListener(this::mniReorderMembersOfOneTeamActionPerformed);
        pupTeams.add(mniReorderMembersOfOneTeam);

        mniReorderMembersOfOneTeamAllRounds.setText("Reorder players of one team by rating for all rounds");
        mniReorderMembersOfOneTeamAllRounds.addActionListener(this::mniReorderMembersOfOneTeamAllRoundsActionPerformed);
        pupTeams.add(mniReorderMembersOfOneTeamAllRounds);

        mniReorderPlayersOfAllTeams.setText("Reorder players of all teams by rating");
        mniReorderPlayersOfAllTeams.addActionListener(this::mniReorderPlayersOfAllTeamsActionPerformed);
        pupTeams.add(mniReorderPlayersOfAllTeams);

        mniReorderPlayersOfAllTeamsAllRounds.setText("Reorder players of all teams by rating for all rounds");
        mniReorderPlayersOfAllTeamsAllRounds.addActionListener(this::mniReorderPlayersOfAllTeamsAllRoundsActionPerformed);
        pupTeams.add(mniReorderPlayersOfAllTeamsAllRounds);
        pupTeams.add(jSeparator3);

        mniRenumberTeams.setText("Renumber teams according to mean rating");
        mniRenumberTeams.addActionListener(this::mniRenumberTeamsActionPerformed);
        pupTeams.add(mniRenumberTeams);

        mniChangeTeamNumber.setText("Change number of One team ");
        mniChangeTeamNumber.addActionListener(this::mniChangeTeamNumberActionPerformed);
        pupTeams.add(mniChangeTeamNumber);
        pupTeams.add(jSeparator5);

        mniCancel.setText("Cancel");
        mniCancel.addActionListener(this::mniCancelActionPerformed);
        pupTeams.add(mniCancel);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new MigLayout("insets dialog, wrap 3", "[][]unrel[]", "[][]unrel[]unrel[]"));



















        jLabel9.setText("Round");
        getContentPane().add(jLabel9);

        spnRoundNumber.addChangeListener(this::spnRoundNumberStateChanged);
        getContentPane().add(spnRoundNumber, "wmin 36lp");

        pnlTeams.setBorder(javax.swing.BorderFactory.createTitledBorder("Teams"));
        pnlTeams.setLayout(new MigLayout("flowy, insets panel"));

        lblNbTeams.setText("teams");
        pnlTeams.add(lblNbTeams);

        tblTeams.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "Team number", "Team name", "Board number", "Name", "Co", "Club", "Rating"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblTeams.setToolTipText("To modify, right click !");
        tblTeams.setDragEnabled(true);
        tblTeams.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblTeamsMouseClicked(evt);
            }
        });
        tblTeams.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tblTeamsFocusLost(evt);
            }
        });
        scpTeams.setViewportView(tblTeams);

        pnlTeams.add(scpTeams);

        btnCreateNewTeam.setText("Create a new Team");
        btnCreateNewTeam.addActionListener(this::btnCreateNewTeamActionPerformed);
        pnlTeams.add(btnCreateNewTeam);

        getContentPane().add(pnlTeams, "push, grow, spany 3");

        jLabel3.setText("Team size");
        getContentPane().add(jLabel3);

        txfTeamSize.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        txfTeamSize.setText("4");
        txfTeamSize.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txfTeamSizeFocusLost(evt);
            }
        });
        getContentPane().add(txfTeamSize, "wmin 36lp");

        pnlPlayers.setBorder(javax.swing.BorderFactory.createTitledBorder("Players"));
        pnlPlayers.setLayout(new MigLayout("flowy, insets panel"));

        lblTeamablePlayers.setText("teamable players");
        pnlPlayers.add(lblTeamablePlayers);

        tblTeamablePlayers.setAutoCreateRowSorter(true);
        tblTeamablePlayers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "Last name", "Co", "Club", "Rating"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblTeamablePlayers.setToolTipText("To team a player, select the player, then drag and drop it to the desired team ");
        scpTeamablePlayers.setViewportView(tblTeamablePlayers);

        pnlPlayers.add(scpTeamablePlayers);

        getContentPane().add(pnlPlayers, "push, grow, spanx 2, spany 2");

        btnPrint.setText("Print ...");
        btnPrint.addActionListener(this::btnPrintActionPerformed);
        getContentPane().add(btnPrint);

        btnHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/info/vannier/gotha/gothalogo16.jpg")));
        btnHelp.setText("help");
        btnHelp.addActionListener(this::btnHelpActionPerformed);
        getContentPane().add(btnHelp, "spanx 3, split 2, tag help");

        btnClose.setText("Close");
        btnClose.addActionListener(this::btnCloseActionPerformed);
        getContentPane().add(btnClose, "tag cancel");

        pack();
    }

    private void btnHelpActionPerformed(java.awt.event.ActionEvent evt) {
        Gotha.displayGothaHelp("Teams Manager frame");
}

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {
        cleanClose();
}

    private void cleanClose(){
        running = false;
        dispose();
    }

    private void btnCreateNewTeamActionPerformed(java.awt.event.ActionEvent evt) {
        int teamSize = 0;
        try {
            teamSize = tournament.getTeamSize();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        String strTeamName = JOptionPane.showInputDialog(this, "Give a name to the team", "New Team");
        Team team = new Team(strTeamName);
        try {
            tournament.addTeam(team);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.tournamentChanged();
    }

    private void tblTeamsMouseClicked(java.awt.event.MouseEvent evt) {
        this.pupTeams.setVisible(false);
        // Right click
        if (evt.getModifiers() != InputEvent.BUTTON3_MASK) return;

        Team team = this.selectedTeam();
        if (team == null){
            Point p = evt.getPoint();
            int row = this.tblTeams.rowAtPoint(p);
            this.tblTeams.setRowSelectionInterval(row, row);
            team = this.selectedTeam();
        }
        int boardNumber = this.selectedBoard();
        this.mniRemoveAllTeams.setText("Remove all teams");
        this.mniUnteamAllTeams.setText("Unteam all teams for round " + (processedRoundNumber + 1));
        this.mniUnteamAllTeamsAllRounds.setText("Unteam all teams for all rounds");
        this.mniReorderPlayersOfAllTeams.setText("Reorder members of all teams by rating for round " + (processedRoundNumber + 1));
        this.mniReorderPlayersOfAllTeamsAllRounds.setText("Reorder members of all teams by rating for all rounds");
        this.mniRenumberTeams.setText("Renumber teams according to mean rating");
        if (team == null){
            this.mniRemoveOneTeam.setEnabled(false);
            this.mniUnteamOneMember.setEnabled(false);
            this.mniUnteamOneMemberAllRounds.setEnabled(false);
            this.mniUnteamOneTeam.setEnabled(false);
            this.mniUnteamOneTeamAllRounds.setEnabled(false);
            this.mniRenameTeam.setEnabled(false);
            this.mniReorderMembersOfOneTeam.setEnabled(false);
            this.mniChangeTeamNumber.setEnabled(false);
            this.mniRemoveOneTeam.setText("Remove one team");
            this.mniUnteamOneMember.setText("Unteam one member");
            this.mniUnteamOneMemberAllRounds.setText("Unteam one member for all rounds");
            this.mniUnteamOneTeam.setText("Unteam one team");
            this.mniUnteamOneTeamAllRounds.setText("Unteam one team for all rounds");
            this.mniRenameTeam.setText("Rename one team");
            this.mniReorderMembersOfOneTeam.setText("Reorder members of one team by rating");
            this.mniReorderMembersOfOneTeamAllRounds.setText("Reorder members of one team by rating for all rounds");
            this.mniChangeTeamNumber.setText("Change number of one team");
         }
         else{
            this.mniRemoveOneTeam.setEnabled(true);
            this.mniUnteamOneMember.setEnabled(true);
            this.mniUnteamOneMemberAllRounds.setEnabled(true);
            this.mniUnteamOneTeam.setEnabled(true);
            this.mniUnteamOneTeamAllRounds.setEnabled(true);
            this.mniRenameTeam.setEnabled(true);
            this.mniReorderMembersOfOneTeam.setEnabled(true);
            this.mniRemoveOneTeam.setText("Remove " + team.getTeamName());
            this.mniUnteamOneMember.setText("Unteam board " + (boardNumber + 1) + " of " + team.getTeamName() + " for round " + (processedRoundNumber + 1));
            this.mniUnteamOneMemberAllRounds.setText("Unteam board " + (boardNumber + 1) + " of " + team.getTeamName() + " for all rounds ");
            this.mniUnteamOneTeam.setText("Unteam " + team.getTeamName() + " for round " + (processedRoundNumber + 1));
            this.mniUnteamOneTeamAllRounds.setText("Unteam " + team.getTeamName() + " for all rounds");
            this.mniRenameTeam.setText("Rename " + team.getTeamName());
            this.mniReorderMembersOfOneTeam.setText("Reorder members of " + team.getTeamName() + " by rating for round " + (processedRoundNumber + 1));
            this.mniReorderMembersOfOneTeamAllRounds.setText("Reorder members of " + team.getTeamName() + " by rating for all rounds");
            this.mniChangeTeamNumber.setText("Change number of " + team.getTeamName());
         }

        Point p = evt.getLocationOnScreen();
        pupTeams.setLocation(p);
        pupTeams.setVisible(true);

    }
    /**
     * Returns the first selected team
     * or null if no team is selected
     */
    private Team selectedTeam() {
        Team selectedTeam = null;
        int teamSize = 1;
        try {
            teamSize = tournament.getTeamSize();
            int row = tblTeams.getSelectedRow();
            if (row < 0) return null;
            row = (row /teamSize) * teamSize;
            if (row < 0) return null;
            String strTeamName = (String) tblTeams.getModel().getValueAt(row, TM_TEAM_NAME_COL);
            selectedTeam = tournament.getTeamByName(strTeamName);
         } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return selectedTeam;
    }
   /**
     * Returns the first selected team
     * or null if no team is selected
     */
    private int selectedBoard() {
        int row = tblTeams.getSelectedRow();
        if (row < 0) return -1;
        String strBoardNumber = (String) tblTeams.getModel().getValueAt(row, TM_BOARD_NUMBER_COL);
        int boardNumber = Integer.parseInt(strBoardNumber) - 1;
        return boardNumber;
    }

    private void mniUnteamOneTeamActionPerformed(java.awt.event.ActionEvent evt) {
        pupTeams.setVisible(false);
        Team team = this.selectedTeam();
        try {
            this.tournament.unteamTeamMembers(team, processedRoundNumber);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.tournamentChanged();
    }

    private void mniUnteamAllTeamsActionPerformed(java.awt.event.ActionEvent evt) {
        pupTeams.setVisible(false);
        try {
            tournament.unteamAllTeams(processedRoundNumber);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.tournamentChanged();
    }

    private void mniRenameTeamActionPerformed(java.awt.event.ActionEvent evt) {
        pupTeams.setVisible(false);
        Team team = this.selectedTeam();
        String oldName = team.getTeamName();
        String newName = JOptionPane.showInputDialog(this, "Rename team", oldName);
        newName = newName.trim();
        if (newName.equals(oldName)) return;
        try {
            tournament.modifyTeamName(team, newName);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.tournamentChanged();

    }

    private void mniRemoveOneTeamActionPerformed(java.awt.event.ActionEvent evt) {
        pupTeams.setVisible(false);
        try {
            tournament.removeTeam(this.selectedTeam());
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.tournamentChanged();
    }

    private void mniRemoveAllTeamsActionPerformed(java.awt.event.ActionEvent evt) {
        pupTeams.setVisible(false);
        try {
            tournament.removeAllTeams();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.tournamentChanged();

    }

    private void mniReorderMembersOfOneTeamActionPerformed(java.awt.event.ActionEvent evt) {
        pupTeams.setVisible(false);
        Team team = this.selectedTeam();
        try {
            tournament.reorderTeamMembersByRating(team, processedRoundNumber);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.tournamentChanged();
    }

    private void mniUnteamOneMemberActionPerformed(java.awt.event.ActionEvent evt) {
        pupTeams.setVisible(false);
        Team team = this.selectedTeam();
        int bn = this.selectedBoard();

        try {
            this.tournament.unteamTeamMember(team, processedRoundNumber, bn);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.tournamentChanged();
    }

    private void mniReorderPlayersOfAllTeamsActionPerformed(java.awt.event.ActionEvent evt) {
        pupTeams.setVisible(false);
        try {
            tournament.reorderTeamMembersByRating(processedRoundNumber);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.tournamentChanged();
    }

    private void mniRenumberTeamsActionPerformed(java.awt.event.ActionEvent evt) {
        pupTeams.setVisible(false);
        try {
            tournament.renumberTeamsByTotalRating();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.tournamentChanged();
    }

    private void txfTeamSizeFocusLost(java.awt.event.FocusEvent evt) {
        int newTS = -1;
        try{
            newTS = Integer.parseInt(txfTeamSize.getText());
        }
        catch(NumberFormatException exc){

        }
        int oldTS = newTS;
        try {
            oldTS = tournament.getTeamSize();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (newTS == oldTS) return;
        if (newTS < Gotha.MIN_NUMBER_OF_MEMBERS_BY_TEAM || newTS > Gotha.MAX_NUMBER_OF_MEMBERS_BY_TEAM){
            this.txfTeamSize.setText("" + oldTS);
            return;
        }
        int nbTeams;
        int nbGames;
        try {
            nbTeams = tournament.teamsList().size();
            nbGames = tournament.gamesList().size();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            tournament.setTeamSize(newTS);
            tournament.cleanTeams();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.tournamentChanged();
    }

    private void mniChangeTeamNumberActionPerformed(java.awt.event.ActionEvent evt) {
        pupTeams.setVisible(false);
        Team t1 = this.selectedTeam();
        // Ask for a new number
        int oldTN = t1.getTeamNumber();
        String strOldTN = "" + (oldTN + 1);
        String strResponse = JOptionPane.showInputDialog("Enter a new team number", strOldTN);
        int newTN = (Integer.parseInt(strResponse)) - 1;
        if (newTN < 0 || newTN >= Gotha.MAX_NUMBER_OF_TEAMS) {
            JOptionPane.showMessageDialog(this, " Team number should be a number between 1 and " + Gotha.MAX_NUMBER_OF_TEAMS,
                    "Message", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (newTN == oldTN) {
            return;
        }
        // If there already a team with newTN as team number ?
        Team t2 = null;
        try {
            ArrayList<Team> alTeams = tournament.teamsList();
            for (Team t : alTeams) {
                if (t.getTeamNumber() == newTN) {
                    t2 = t;
                }
            }
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesPair.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            tournament.removeTeam(t1);
            t1.setTeamNumber(newTN);
            if (t2 != null) {
                tournament.removeTeam(t2);
                t2.setTeamNumber(oldTN);
                tournament.addTeam(t2);
            }
            tournament.addTeam(t1);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesPair.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.tournamentChanged();
    }

    private void mniCancelActionPerformed(java.awt.event.ActionEvent evt) {
        this.pupTeams.setVisible(false);
    }

    private void formWindowClosed(java.awt.event.WindowEvent evt) {
         this.pupTeams.setVisible(false);
    }

    private void tblTeamsFocusLost(java.awt.event.FocusEvent evt) {
        this.pupTeams.setVisible(false);
    }

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {
        TournamentPrinting.printTeamsList(tournament);
    }

    private void spnRoundNumberStateChanged(javax.swing.event.ChangeEvent evt) {
        int demandedRN = (Integer) (spnRoundNumber.getValue()) - 1;
        this.demandedDisplayedRoundNumberHasChanged(demandedRN);
    }

    private void mniUnteamOneMemberAllRoundsActionPerformed(java.awt.event.ActionEvent evt) {
        Team team = this.selectedTeam();
        int bn = this.selectedBoard();

        try {
            for (int r = 0; r < Gotha.MAX_NUMBER_OF_ROUNDS; r++){
                this.tournament.unteamTeamMember(team, r, bn);
            }
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.tournamentChanged();
    }

    private void mniUnteamOneTeamAllRoundsActionPerformed(java.awt.event.ActionEvent evt) {
        pupTeams.setVisible(false);
        Team team = this.selectedTeam();
        try {
            for(int r = 0; r < Gotha.MAX_NUMBER_OF_ROUNDS; r++){
                this.tournament.unteamTeamMembers(team, r);
            }
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.tournamentChanged();
    }

    private void mniUnteamAllTeamsAllRoundsActionPerformed(java.awt.event.ActionEvent evt) {
        pupTeams.setVisible(false);
        try {
            for (int r = 0; r < Gotha.MAX_NUMBER_OF_ROUNDS; r++){
                 tournament.unteamAllTeams(r);
            }
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.tournamentChanged();
    }

    private void mniReorderMembersOfOneTeamAllRoundsActionPerformed(java.awt.event.ActionEvent evt) {
        pupTeams.setVisible(false);
        Team team = this.selectedTeam();
        try {
            for (int r= 0; r < Gotha.MAX_NUMBER_OF_ROUNDS; r++){
                tournament.reorderTeamMembersByRating(team, r);
            }
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.tournamentChanged();

    }

    private void mniReorderPlayersOfAllTeamsAllRoundsActionPerformed(java.awt.event.ActionEvent evt) {
        pupTeams.setVisible(false);
        try {
            for (int r = 0; r < Gotha.MAX_NUMBER_OF_ROUNDS; r++){
                tournament.reorderTeamMembersByRating(r);
            }
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.tournamentChanged();
    }

    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        cleanClose();
    }

    private void demandedDisplayedRoundNumberHasChanged(int demandedRN) {
        int numberOfRounds = 0;
        try {
            numberOfRounds = tournament.getTournamentParameterSet().getGeneralParameterSet().getNumberOfRounds();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (demandedRN < 0 || demandedRN >= numberOfRounds) {
            spnRoundNumber.setValue(processedRoundNumber + 1);
            return;
        }
        if (demandedRN == processedRoundNumber) {
            return;
        }

        processedRoundNumber = demandedRN;
        updateAllViews();
    }

    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnCreateNewTeam;
    private javax.swing.JButton btnHelp;
    private javax.swing.JButton btnPrint;
    private javax.swing.JLabel lblTeamablePlayers;
    private javax.swing.JLabel lblNbTeams;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JMenuItem mniCancel;
    private javax.swing.JMenuItem mniChangeTeamNumber;
    private javax.swing.JMenuItem mniRemoveAllTeams;
    private javax.swing.JMenuItem mniRemoveOneTeam;
    private javax.swing.JMenuItem mniRenameTeam;
    private javax.swing.JMenuItem mniRenumberTeams;
    private javax.swing.JMenuItem mniReorderMembersOfOneTeam;
    private javax.swing.JMenuItem mniReorderMembersOfOneTeamAllRounds;
    private javax.swing.JMenuItem mniReorderPlayersOfAllTeams;
    private javax.swing.JMenuItem mniReorderPlayersOfAllTeamsAllRounds;
    private javax.swing.JMenuItem mniUnteamAllTeams;
    private javax.swing.JMenuItem mniUnteamAllTeamsAllRounds;
    private javax.swing.JMenuItem mniUnteamOneMember;
    private javax.swing.JMenuItem mniUnteamOneMemberAllRounds;
    private javax.swing.JMenuItem mniUnteamOneTeam;
    private javax.swing.JMenuItem mniUnteamOneTeamAllRounds;
    private javax.swing.JPanel pnlPlayers;
    private javax.swing.JPanel pnlTeams;
    private javax.swing.JPopupMenu pupTeams;
    private javax.swing.JScrollPane scpTeamablePlayers;
    private javax.swing.JScrollPane scpTeams;
    private javax.swing.JSpinner spnRoundNumber;
    private javax.swing.JTable tblTeamablePlayers;
    private javax.swing.JTable tblTeams;
    private javax.swing.JTextField txfTeamSize;

    private void tournamentChanged() {
        try {
            tournament.setLastTournamentModificationTime(tournament.getCurrentTournamentTime());
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        updateAllViews();
    }

    private void updateAllViews() {
        try {
            if (!tournament.isOpen()) cleanClose();
            this.lastComponentsUpdateTime = tournament.getCurrentTournamentTime();
            setTitle("Teams Manager. " + tournament.getFullName());
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        updateComponents();
    }

}
