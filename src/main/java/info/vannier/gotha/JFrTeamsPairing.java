/*
 * JFrTeamsPair.java
 */
package info.vannier.gotha;

import net.miginfocom.swing.MigLayout;
import ru.gofederation.gotha.model.Game;
import ru.gofederation.gotha.util.GothaLocale;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Luc Vannier
 */
public class JFrTeamsPairing extends javax.swing.JFrame {

    private static final long REFRESH_DELAY = 2000;
    private static final int TEAM_NUMBER_COL = 0;
    private static final int TEAM_NAME_COL = 1;
    private static final int TEAM_SCORE_COL = 2;
    private static final int MATCH_TABLE_NUMBER_COL = 0;
    private static final int MATCH_WHITE_TEAM_COL = 1;
    private static final int MATCH_BLACK_TEAM_COL = 2;
    // private int gamesSortType = GameComparator.TABLE_NUMBER_ORDER;
    private long lastComponentsUpdateTime = 0;
    private TournamentInterface tournament;
    /** current Round */
    private int processedRoundNumber = 0;

    private final GothaLocale locale = GothaLocale.getCurrentLocale();

    /** Creates new form JFrTeamsPair */
    public JFrTeamsPairing(TournamentInterface tournament) throws RemoteException {
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

    private void initComponents() {
        pupMatches = new javax.swing.JPopupMenu();
        mniRenumberTables = new javax.swing.JMenuItem();
        mniChangeTableNumbers = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        mniExchangeColours = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        mniCancel = new javax.swing.JMenuItem();
        btnPair = new javax.swing.JButton();
        btnUnpair = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        pnlTeams = new javax.swing.JPanel();
        lblPairableTeams = new javax.swing.JLabel();
        scpPairableTeams = new javax.swing.JScrollPane();
        tblPairableTeams = new javax.swing.JTable();
        pnlMatches = new javax.swing.JPanel();
        scpMatches = new javax.swing.JScrollPane();
        tblMatches = new javax.swing.JTable();
        lblMatches = new javax.swing.JLabel();
        btnPrint = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        spnRoundNumber = new javax.swing.JSpinner();
        btnHelp = new javax.swing.JButton();

        mniRenumberTables.setText("Renumber all tables by Team score");
        mniRenumberTables.addActionListener(this::mniRenumberTablesActionPerformed);
        pupMatches.add(mniRenumberTables);

        mniChangeTableNumbers.setText("Change Table Numbers");
        mniChangeTableNumbers.addActionListener(this::mniChangeTableNumbersActionPerformed);
        pupMatches.add(mniChangeTableNumbers);
        pupMatches.add(jSeparator1);

        mniExchangeColours.setText("Exchange colours");
        mniExchangeColours.addActionListener(this::mniExchangeColoursActionPerformed);
        pupMatches.add(mniExchangeColours);
        pupMatches.add(jSeparator5);

        mniCancel.setText(locale.getString("btn.cancel"));
        mniCancel.addActionListener(this::mniCancelActionPerformed);
        pupMatches.add(mniCancel);

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
        getContentPane().setLayout(new MigLayout("insets dialog", "[sg, grow, fill][al c][sg, grow, fill]", "[]unrel[][]36lp[][]unrel[]"));

        jLabel9.setText(locale.getString("tournament.round"));
        getContentPane().add(jLabel9, "split 2");

        spnRoundNumber.addChangeListener(this::spnRoundNumberStateChanged);
        getContentPane().add(spnRoundNumber, "wmin 36lp, gapright push, wrap");

        pnlTeams.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("tournament.teams")));
        pnlTeams.setLayout(new MigLayout("flowy, insets panel"));

        lblPairableTeams.setText(locale.getString("tournament.teams.pairable"));
        pnlTeams.add(lblPairableTeams);

        tblPairableTeams.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "Nr", "Team name", "TP"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblPairableTeams.setToolTipText("Select 2 teams, then click \"Pair\"");
        scpPairableTeams.setViewportView(tblPairableTeams);

        pnlTeams.add(scpPairableTeams, "push, grow");

        getContentPane().add(pnlTeams, "spany 4");

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText(locale.getString("game.pairing.pair"));
        getContentPane().add(jLabel6, "gaptop 72lp");

        pnlMatches.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("tournament.teams.matches")));
        pnlMatches.setLayout(new MigLayout("flowy, insets panel"));

        lblMatches.setText(locale.getString("tournament.teams.match_count"));
        pnlMatches.add(lblMatches);

        tblMatches.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "Tables", "", ""
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblMatches.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblMatchesMouseClicked(evt);
            }
        });
        tblMatches.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tblMatchesFocusLost(evt);
            }
        });
        scpMatches.setViewportView(tblMatches);

        pnlMatches.add(scpMatches, "push, grow");

        btnPrint.setText(locale.getString("btn.print"));
        btnPrint.addActionListener(this::btnPrintActionPerformed);
        pnlMatches.add(btnPrint);

        getContentPane().add(pnlMatches, "spany 4, wrap");

        btnPair.setText(">>>");
        btnPair.addActionListener(this::btnPairActionPerformed);
        getContentPane().add(btnPair, "wrap");

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText(locale.getString("game.pairing.unpair"));
        getContentPane().add(jLabel7, "wrap");

        btnUnpair.setText("<<<");
        btnUnpair.addActionListener(this::btnUnpairActionPerformed);
        getContentPane().add(btnUnpair, "ay top, wrap");

        btnClose.setText(locale.getString("btn.close"));
        btnClose.addActionListener(this::btnCloseActionPerformed);
        getContentPane().add(btnClose, "span, split 2, tag cancel");

        btnHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/info/vannier/gotha/gothalogo16.jpg")));
        btnHelp.setText(locale.getString("btn.help"));
        btnHelp.addActionListener(this::btnHelpActionPerformed);
        getContentPane().add(btnHelp, "tag close");

        pack();
    }

    private void btnPairActionPerformed(java.awt.event.ActionEvent evt) {
        ArrayList<Team> alTeamsToPair = selectedTeamsList();
        if (alTeamsToPair.size() != 2) {
            JOptionPane.showMessageDialog(this, locale.getString("tournament.teams.select_two"),
                    locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        Team t0 = alTeamsToPair.get(0);
        Team t1 = alTeamsToPair.get(1);

        try {
            String strUncompleteTeam = "";
            if (!tournament.isTeamComplete(t1, processedRoundNumber)) {
                strUncompleteTeam = t1.getTeamName();
            }
            if (!tournament.isTeamComplete(t0, processedRoundNumber)) {
                strUncompleteTeam = t0.getTeamName();
            }
            if (!strUncompleteTeam.equals("")) {
                JOptionPane.showMessageDialog(this, locale.format("tournament.teams.incomplete", strUncompleteTeam), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            // These teams, have they been paired in a previous round ?
            ArrayList<Match> alOldMatches = tournament.matchesListUpTo(processedRoundNumber - 1);
            Match questionableMatch = null;
            for (Match oldMatch : alOldMatches) {
                Team wT = oldMatch.getWhiteTeam();
                Team bT = oldMatch.getBlackTeam();
                if (wT.getTeamName().equals(t0.getTeamName()) && bT.getTeamName().equals(t1.getTeamName())){
                    questionableMatch = oldMatch;
                    break;
                }
                if (wT.getTeamName().equals(t1.getTeamName()) && bT.getTeamName().equals(t0.getTeamName())){
                    questionableMatch = oldMatch;
                    break;
                }
            }

            if (questionableMatch != null) {
                Team wT = questionableMatch.getWhiteTeam();
                Team bT = questionableMatch.getBlackTeam();
                int r = questionableMatch.getRoundNumber();

                int bAnswer = JOptionPane.showConfirmDialog(this, locale.format("tournament.teams.already_paired", wT.getTeamName(), bT.getTeamName(), r + 1),
                        locale.getString("alert.message"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (bAnswer == JOptionPane.NO_OPTION) return;
                return;
            }
            tournament.pairTeams(alTeamsToPair.get(0), alTeamsToPair.get(1), this.processedRoundNumber);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsPairing.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.tournamentChanged();
}

    /**
     * Produces a list of selected teams in tblPairable
     * If no team is selected, returns the full list
     */
    private ArrayList<Team> selectedTeamsList() {
        ArrayList<Team> alSelectedTeams = new ArrayList<Team>();

        boolean bNoTeamSelected = false;
        if (tblPairableTeams.getSelectedRowCount() == 0) {
            bNoTeamSelected = true;
            // gather selected players into alPlayersToPair
        }
        for (int iRow = 0; iRow < tblPairableTeams.getModel().getRowCount(); iRow++) {
            if (tblPairableTeams.isRowSelected(iRow) || bNoTeamSelected) {
                String teamName = (String) tblPairableTeams.getModel().getValueAt(iRow, TEAM_NAME_COL);
                Team t = null;
                try {
                    t = tournament.getTeamByName(teamName);
                } catch (RemoteException ex) {
                    Logger.getLogger(JFrGamesPair.class.getName()).log(Level.SEVERE, null, ex);
                }
                alSelectedTeams.add(t);
            }
        }
        return alSelectedTeams;
    }

    private void btnUnpairActionPerformed(java.awt.event.ActionEvent evt) {
        int teamSize = Gotha.MAX_NUMBER_OF_MEMBERS_BY_TEAM;
        try {
            teamSize = tournament.getTeamSize();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsPairing.class.getName()).log(Level.SEVERE, null, ex);
        }

        ArrayList<Match> alMatchesToRemove = selectedMatchesList();

        int nbMatchesToRemove = alMatchesToRemove.size();
        if (nbMatchesToRemove == 0) try {
            alMatchesToRemove = tournament.matchesList(processedRoundNumber);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsPairing.class.getName()).log(Level.SEVERE, null, ex);
        }
        nbMatchesToRemove = alMatchesToRemove.size();

        if (nbMatchesToRemove > 1) {
            int response = JOptionPane.showConfirmDialog(this,
                    locale.format("tournament.teams.confirm_unpair", nbMatchesToRemove, nbMatchesToRemove * teamSize),
                    locale.getString("alert.message"),
                    JOptionPane.WARNING_MESSAGE,
                    JOptionPane.OK_CANCEL_OPTION);
            if (response == JOptionPane.CANCEL_OPTION) {
                return;
            }

        }
        try {
            // And now, remove matches from tournament
            for (Match m : alMatchesToRemove) {
                Team wt = m.getWhiteTeam();
                for (int ib = 0; ib < teamSize; ib++) {
                    Player wp = wt.getTeamMember(processedRoundNumber, (ib));
                    if (wp == null) {
                        continue;
                    }
                    Game g = tournament.getGame(processedRoundNumber, wp);
                    if (g != null) {
                        tournament.removeGame(g);
                    }
                }
            }
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesPair.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TournamentException ex) {
            Logger.getLogger(JFrGamesPair.class.getName()).log(Level.SEVERE, null, ex);
        }
        // If all games removed, also remove bye player
        try {
            if ((tournament.getByePlayer(processedRoundNumber) != null) && (tournament.gamesList(processedRoundNumber).isEmpty())) {
                tournament.unassignByePlayer(processedRoundNumber);
            }
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesPair.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.tournamentChanged();
    }
    /**
     * Produces a list of selected matches in tblMatches
     * If no match is selected, returns an empty list
     */
    private ArrayList<Match> selectedMatchesList() {
        ArrayList<Match> alSelectedMatches = new ArrayList<Match>();

        for (int iRow = 0; iRow < tblMatches.getModel().getRowCount(); iRow++) {
            if (tblMatches.isRowSelected(iRow)) {
                String s = (String) tblMatches.getModel().getValueAt(iRow, MATCH_TABLE_NUMBER_COL);
                s = s.replaceAll("-", "");

                int tableNumber = Integer.parseInt(s) - 1;
                Match m = null;
                try {
                    m = tournament.getMatch(processedRoundNumber, tableNumber);
                } catch (RemoteException ex) {
                    Logger.getLogger(JFrGamesPair.class.getName()).log(Level.SEVERE, null, ex);
                }
                alSelectedMatches.add(m);
            }
        }
        return alSelectedMatches;
    }

    private void tblMatchesMouseClicked(java.awt.event.MouseEvent evt) {
        this.pupMatches.setVisible(false);
        // Right click
        if (evt.getModifiers() != InputEvent.BUTTON3_MASK) return;

        ArrayList<Match> alMatches = this.selectedMatchesList();
        Point pInScreen = evt.getLocationOnScreen();
        Point pInSourceComponent = evt.getPoint();
        if (alMatches.isEmpty()) {
            int row = this.tblMatches.rowAtPoint(pInSourceComponent);
            this.tblMatches.setRowSelectionInterval(row, row);
            alMatches = this.selectedMatchesList();
        }

        Match match = alMatches.get(0);
        String strWTN = match.getWhiteTeam().getTeamName();
        if (strWTN.length() > 20) {
            strWTN = strWTN.substring(0, 20);
        }
        String strBTN = match.getBlackTeam().getTeamName();
        if (strBTN.length() > 20) {
            strBTN = strBTN.substring(0, 20);
        }
        this.mniChangeTableNumbers.setText(locale.format("tournament.teams.menu.change_table_numbers", strWTN, strBTN));
        this.mniExchangeColours.setText(locale.format("tournament.teams.menu.exchange_colors", strWTN, strBTN));


        pupMatches.setLocation(pInScreen);
        pupMatches.setVisible(true);

}

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {
        // this.pupMatches.setVisible(false);
        cleanClose();
}

    private void cleanClose(){
        running = false;
        dispose();
    }

    private void spnRoundNumberStateChanged(javax.swing.event.ChangeEvent evt) {
        int demandedRN = (Integer) (spnRoundNumber.getValue()) - 1;
        this.demandedDisplayedRoundNumberHasChanged(demandedRN);
}

    private void btnHelpActionPerformed(java.awt.event.ActionEvent evt) {
        Gotha.displayGothaHelp("Teams Pairing frame");
}

    private void mniCancelActionPerformed(java.awt.event.ActionEvent evt) {
        this.pupMatches.setVisible(false);
        tblMatches.removeRowSelectionInterval(0, tblMatches.getRowCount() - 1);
}

    private void mniRenumberTablesActionPerformed(java.awt.event.ActionEvent evt) {
        this.pupMatches.setVisible(false);
        ArrayList<Match> alActualMatches = null;
        try {
            alActualMatches = tournament.matchesList(processedRoundNumber);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsPairing.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.lblMatches.setText(locale.format("tournament.teams.match_count", alActualMatches.size()));

        renumberMatchTablesByBestScore(alActualMatches);

        this.tournamentChanged();
    }

    private void renumberMatchTablesByBestScore(ArrayList<Match> alMatchesToRenumber) {
        ArrayList<ComparableMatch> alCM = ComparableMatch.buildComparableMatchesArray(alMatchesToRenumber, tournament, processedRoundNumber - 1);

        MatchComparator matchComparator = new MatchComparator(MatchComparator.BEST_TEAM_TS);
        Collections.sort(alCM, matchComparator);

        // Now, according to this new Order, renumber all concerned games

        int teamSize = Gotha.MAX_NUMBER_OF_MEMBERS_BY_TEAM;
        try {
            teamSize = tournament.getTeamSize();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsPairing.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Remove games
        for (ComparableMatch cm : alCM) {
            ScoredTeam wst = cm.wst;
            for (int ib = 0; ib < teamSize; ib++) {
                Player p = wst.getTeamMember(processedRoundNumber, ib);
                if (p == null) {
                    continue;
                }
                try {
                    Game g = tournament.getGame(processedRoundNumber, p);
                    tournament.removeGame(g);
                } catch (RemoteException ex) {
                    Logger.getLogger(JFrTeamsPairing.class.getName()).log(Level.SEVERE, null, ex);
                } catch (TournamentException ex) {
                    Logger.getLogger(JFrTeamsPairing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        for (ComparableMatch cm : alCM) {
            ScoredTeam wst = cm.wst;
            ScoredTeam bst = cm.bst;
            for (int ib = 0; ib < teamSize; ib++) {
                Player wp = wst.getTeamMember(processedRoundNumber, ib);
                Player bp = bst.getTeamMember(processedRoundNumber, ib);
                if (wp == null) {
                    continue;
                }
                if (bp == null) {
                    continue;
                }
                int tn = 0;
                try {
                    tn = tournament.findFirstAvailableTableNumber(processedRoundNumber);
                    Game.Builder g = new Game.Builder(processedRoundNumber, tn, wp, bp, true, 0, Game.Result.UNKNOWN);
                    tournament.addGame(g.build());
                } catch (RemoteException ex) {
                    Logger.getLogger(JFrTeamsPairing.class.getName()).log(Level.SEVERE, null, ex);
                } catch (TournamentException ex) {
                    Logger.getLogger(JFrTeamsPairing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    private void formWindowClosed(java.awt.event.WindowEvent evt) {
        this.pupMatches.setVisible(false);
    }

    private void mniExchangeColoursActionPerformed(java.awt.event.ActionEvent evt) {
        this.pupMatches.setVisible(false);
        int teamSize = Gotha.MAX_NUMBER_OF_MEMBERS_BY_TEAM;
        try {
            teamSize = tournament.getTeamSize();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsPairing.class.getName()).log(Level.SEVERE, null, ex);
        }

        ArrayList<Match> alMatches = this.selectedMatchesList();
        if (alMatches.isEmpty()) {
            return;
        }
        Match match = alMatches.get(0);

        Team wt = match.getWhiteTeam();
        Team bt = match.getBlackTeam();

        // Check if there are hd games in this match
        for (int ib = 0; ib < teamSize; ib++) {
            Player wp = wt.getTeamMember(processedRoundNumber, ib);
            Player bp = bt.getTeamMember(processedRoundNumber, ib);
            if (wp == null) {
                continue;
            }
            if (bp == null) {
                continue;
            }
            Game g = null;
            try {
                g = tournament.getGame(processedRoundNumber, wp);
                } catch (RemoteException ex) {
                    Logger.getLogger(JFrTeamsPairing.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (g == null) {
                    return;
            }
            if (g.getHandicap() != 0){
                JOptionPane.showMessageDialog(this, locale.getString("tournament.teams.cannot_change_colors_handicap"),
                    locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        for (int ib = 0; ib < teamSize; ib++) {
            Player wp = wt.getTeamMember(processedRoundNumber, ib);
            Player bp = bt.getTeamMember(processedRoundNumber, ib);
            if (wp == null) {
                continue;
            }
            if (bp == null) {
                continue;
            }
            this.changeColor(wp, bp);
        }

        this.tournamentChanged();

    }

    private void tblMatchesFocusLost(java.awt.event.FocusEvent evt) {
        this.pupMatches.setVisible(false);
    }

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {
        TournamentPrinting.printMatchesList(tournament, processedRoundNumber);
    }

    private void mniChangeTableNumbersActionPerformed(java.awt.event.ActionEvent evt) {
        this.pupMatches.setVisible(false);
        ArrayList<Match> alMatches = this.selectedMatchesList();
        if (alMatches.isEmpty()) {
            return;
        }
        Match match = alMatches.get(0);
        // Ask for a new number
        Team wt = match.getWhiteTeam();
        Team bt = match.getBlackTeam();
        Player player0 = wt.getTeamMember(processedRoundNumber, 0);
        Game game = null;
        try {
            game = tournament.getGame(processedRoundNumber, player0);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsPairing.class.getName()).log(Level.SEVERE, null, ex);
        }
        int oldB0TN = game.getBoard();
        String strOldB0TN = "" + (oldB0TN + 1);
        String strResponse = JOptionPane.showInputDialog(locale.format("tournament.teams.change_table_numbers", wt.getTeamName(), bt.getTeamName()), strOldB0TN);

        int newB0TN = -1;
        try {
            newB0TN = Integer.parseInt(strResponse) - 1;
        } catch (NumberFormatException exc) {
        }

        int teamSize = Gotha.MAX_NUMBER_OF_MEMBERS_BY_TEAM;
        try {
            teamSize = tournament.getTeamSize();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsPairing.class.getName()).log(Level.SEVERE, null, ex);
        }
        int lastTableForB0 = Gotha.MAX_NUMBER_OF_TABLES - teamSize;
        if (newB0TN < 0 || newB0TN > lastTableForB0) {
            JOptionPane.showMessageDialog(this, locale.format("tournament.teams.table_number_limits", lastTableForB0 + 1),
                    locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }


        for (int ib = 0; ib < teamSize; ib++) {
            wt = match.getWhiteTeam();
            Player player = wt.getTeamMember(processedRoundNumber, ib);
            int oldTN;
            int newTN = newB0TN + ib;
            Game g1;
            Game g2 = null;
            try {
                g1 = tournament.getGame(processedRoundNumber, player);
                oldTN = g1.getBoard();
                ArrayList<Game> alGames = tournament.gamesList(this.processedRoundNumber);
                for (Game g : alGames) {
                    if (g.getBoard() == newTN) {
                        g2 = g;
                        break;
                    }
                }

                tournament.removeGame(g1);
                if (g2 != null) {
                    tournament.removeGame(g2);
                    g2 = g2.withBoard(oldTN);
                    tournament.addGame(g2);
                }
                g1 = g1.withBoard(newTN);
                tournament.addGame(g1);

            } catch (RemoteException ex) {
                Logger.getLogger(JFrTeamsPairing.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TournamentException ex) {
                Logger.getLogger(JFrTeamsPairing.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        this.tournamentChanged();
    }

    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        cleanClose();
    }

    private void changeColor(Player p1, Player p2) {
        Game g = null;
        try {
            g = tournament.getGame(processedRoundNumber, p1);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsPairing.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (g == null) {
            return;
        }
        try {
            tournament.exchangeGameColors(g);
        } catch (RemoteException | TournamentException ex) {
            Logger.getLogger(JFrGamesRR.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * Unlike initComponents, customInitComponents is editable
     */
    private void customInitComponents() throws RemoteException {
        initTeamsComponents();
        initMatchesComponents();

        updateAllViews();
    }

    private void initTeamsComponents() {
        initTeamsTable(this.tblPairableTeams);
    }

    private void initTeamsTable(JTable tbl) {
        final int TEAM_NUMBER_WIDTH = 30;
        final int TEAM_NAME_WIDTH = 125;
        final int TEAM_SCORE_WIDTH = 20;
        tbl.getColumnModel().getColumn(TEAM_NUMBER_COL).setPreferredWidth(TEAM_NUMBER_WIDTH);
        tbl.getColumnModel().getColumn(TEAM_NAME_COL).setPreferredWidth(TEAM_NAME_WIDTH);
        tbl.getColumnModel().getColumn(TEAM_SCORE_COL).setPreferredWidth(TEAM_SCORE_WIDTH);

        // Column names
        TableColumnModel tcm = tbl.getColumnModel();
        tcm.getColumn(TEAM_NUMBER_COL).setHeaderValue(locale.getString("nr"));
        tcm.getColumn(TEAM_NAME_COL).setHeaderValue(locale.getString("tournament.team_name"));
        tcm.getColumn(TEAM_SCORE_COL).setHeaderValue("TP");
    }


    private void initMatchesComponents() {
        initMatchesTable(this.tblMatches);
    }

    private void initMatchesTable(JTable tbl) {
        final int MATCH_TABLE_NUMBER_WIDTH = 40;
        final int MATCH_WHITE_TEAM_WIDTH = 160;
        final int MATCH_BLACK_TEAM_WIDTH = 160;

        tbl.getColumnModel().getColumn(MATCH_TABLE_NUMBER_COL).setPreferredWidth(MATCH_TABLE_NUMBER_WIDTH);
        tbl.getColumnModel().getColumn(MATCH_WHITE_TEAM_COL).setPreferredWidth(MATCH_WHITE_TEAM_WIDTH);
        tbl.getColumnModel().getColumn(MATCH_BLACK_TEAM_COL).setPreferredWidth(MATCH_BLACK_TEAM_WIDTH);

        // Column names
        TableColumnModel tcm = tbl.getColumnModel();
        tcm.getColumn(MATCH_TABLE_NUMBER_COL).setHeaderValue(locale.getString("game.boards"));
        tcm.getColumn(MATCH_WHITE_TEAM_COL).setHeaderValue("");
        tcm.getColumn(MATCH_BLACK_TEAM_COL).setHeaderValue("");
    }

    private void tournamentChanged() {
        try {
            tournament.setLastTournamentModificationTime(tournament.getCurrentTournamentTime());
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesPair.class.getName()).log(Level.SEVERE, null, ex);
        }

        updateAllViews();
    }

    private void updateAllViews() {
        int nbRounds = Gotha.MAX_NUMBER_OF_ROUNDS;
        try {
            if (!tournament.isOpen()) cleanClose();
            this.lastComponentsUpdateTime = tournament.getCurrentTournamentTime();
            setTitle(locale.format("tournament.teams.pairing.window_title", tournament.getFullName()));
            nbRounds = tournament.getTournamentParameterSet().getGeneralParameterSet().getNumberOfRounds();
            if (this.processedRoundNumber >= nbRounds) {
                JOptionPane.showMessageDialog(this, locale.getString("tournament.round_count_changed"),
                        locale.getString("alert.message"), JOptionPane.WARNING_MESSAGE);
                this.processedRoundNumber = nbRounds - 1;
            }
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsPairing.class.getName()).log(Level.SEVERE, null, ex);
        }

        updateComponents();
    }

    private void updateComponents() {
        DefaultTableModel pairableTeamsModel = (DefaultTableModel) tblPairableTeams.getModel();
        DefaultTableModel matchesModel = (DefaultTableModel) tblMatches.getModel();
        while (pairableTeamsModel.getRowCount() > 0) {
            pairableTeamsModel.removeRow(0);
        }
        while (matchesModel.getRowCount() > 0) {
            matchesModel.removeRow(0);
        }

        this.spnRoundNumber.setValue(this.processedRoundNumber + 1);

        ArrayList<Match> alActualMatches = null;
        try {
            alActualMatches = tournament.matchesList(processedRoundNumber);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsPairing.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.lblMatches.setText(locale.format("tournament.teams.match_count", alActualMatches.size()));

        // alPairableTeams will be set by substraction
        ArrayList<Team> alTeams = null;
        try {
            alTeams = tournament.teamsList();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsPairing.class.getName()).log(Level.SEVERE, null, ex);
        }

        ArrayList<Team> alPairableTeams = new ArrayList<Team>(alTeams);
        for (Match m : alActualMatches) {
            Team wt = m.getWhiteTeam();
            Team bt = m.getBlackTeam();
            for (Team t : alTeams) {
                if (t.getTeamName().equals(wt.getTeamName())) {
                    alPairableTeams.remove(t);
                }
                if (t.getTeamName().equals(bt.getTeamName())) {
                    alPairableTeams.remove(t);
                }
            }
        }
        int teamSize = Gotha.MAX_NUMBER_OF_MEMBERS_BY_TEAM;
        try {
            teamSize = tournament.getTeamSize();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsPairing.class.getName()).log(Level.SEVERE, null, ex);
        }
        TeamComparator teamComparator = new TeamComparator(TeamComparator.TEAM_NUMBER_ORDER, teamSize);
        Collections.sort(alPairableTeams, teamComparator);

        this.lblPairableTeams.setText(locale.format("tournament.teams.pairable", alPairableTeams.size()));

        fillTeamsTable(alPairableTeams, tblPairableTeams);
        fillMatchesTable(alActualMatches, tblMatches);

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

    private void fillTeamsTable(ArrayList<Team> alT, JTable tblT) {
        DefaultTableModel model = (DefaultTableModel) tblT.getModel();
        for (Team t : alT) {
            Vector<String> row = new Vector<String>();
            row.add("" + (t.getTeamNumber() + 1));
            row.add(t.getTeamName());
            model.addRow(row);
        }
    }

    private void fillMatchesTable(ArrayList<Match> alM, JTable tblM) {
        ArrayList<ComparableMatch> alCM = ComparableMatch.buildComparableMatchesArray(alM, tournament, processedRoundNumber);

        MatchComparator matchComparator = new MatchComparator(MatchComparator.BOARD0_TABLE_NUMBER_ORDER);
        Collections.sort(alCM, matchComparator);

        DefaultTableModel model = (DefaultTableModel) tblM.getModel();
        for (ComparableMatch cm : alCM) {
            Vector<String> row = new Vector<String>();

            String strTN = "" + (cm.board0TableNumber + 1);
            strTN += "---";
            row.add(strTN);

            ScoredTeam wst = cm.wst;
            row.add("" + wst.getTeamName());
            Team bst = cm.bst;
            row.add("" + bst.getTeamName());
            model.addRow(row);
        }

    }

    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnHelp;
    private javax.swing.JButton btnPair;
    private javax.swing.JButton btnPrint;
    private javax.swing.JButton btnUnpair;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JLabel lblMatches;
    private javax.swing.JLabel lblPairableTeams;
    private javax.swing.JMenuItem mniCancel;
    private javax.swing.JMenuItem mniChangeTableNumbers;
    private javax.swing.JMenuItem mniExchangeColours;
    private javax.swing.JMenuItem mniRenumberTables;
    private javax.swing.JPanel pnlMatches;
    private javax.swing.JPanel pnlTeams;
    private javax.swing.JPopupMenu pupMatches;
    private javax.swing.JScrollPane scpMatches;
    private javax.swing.JScrollPane scpPairableTeams;
    private javax.swing.JSpinner spnRoundNumber;
    private javax.swing.JTable tblMatches;
    private javax.swing.JTable tblPairableTeams;
}
