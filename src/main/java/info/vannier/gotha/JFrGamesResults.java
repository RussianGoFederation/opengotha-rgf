/*
 * JFrGamesResults.java
 */
package info.vannier.gotha;

import net.miginfocom.swing.MigLayout;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import ru.gofederation.gotha.util.GothaLocale;

/**
 *
 * @author Luc Vannier
 */
public class JFrGamesResults extends javax.swing.JFrame {

    public static final int TABLE_NUMBER_COL = 0;
    public static final int LEFT_PLAYER_COL = 1;
    public static final int RIGHT_PLAYER_COL = 2;
    public static final int HANDICAP_COL = 3;
    public static final int RESULT_COL = 4;
    public static final int TABLE_NUMBER_WIDTH = 40;
    public static final int PLAYER_WIDTH = 150;
    public static final int HANDICAP_WIDTH = 20;
    public static final int RESULT_WIDTH = 40;
    private static final long REFRESH_DELAY = 2000;
    private long lastComponentsUpdateTime = 0;
    public int gamesSortType = GameComparator.TABLE_NUMBER_ORDER;
    /**
     * current Tournament
     */
    private TournamentInterface tournament;
    /**
     * current Round
     */
    private int processedRoundNumber = 0;

    private final GothaLocale locale = GothaLocale.getCurrentLocale();

    /**
     * Creates new form JFrPlayerManager
     */
    public JFrGamesResults(TournamentInterface tournament) throws RemoteException {
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
//                System.out.println("actionPerformed");
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

    /**
     * This method is called from within the constructor to initialize the form.
     * Unlike initComponents, customInitComponents is editable
     */
    private void customInitComponents() throws RemoteException {
        initGamesComponents();
        this.updateAllViews();
        
        getRootPane().setDefaultButton(btnSearch);
    }

    /**
     */
    private void initGamesComponents() {

        JFrGotha.formatColumn(tblGames, TABLE_NUMBER_COL, locale.getString("game.board"), TABLE_NUMBER_WIDTH, JLabel.RIGHT, JLabel.RIGHT);
        JFrGotha.formatColumn(tblGames, LEFT_PLAYER_COL, "", PLAYER_WIDTH, JLabel.LEFT, JLabel.CENTER);
        JFrGotha.formatColumn(tblGames, RIGHT_PLAYER_COL, "", PLAYER_WIDTH, JLabel.LEFT, JLabel.CENTER);
        JFrGotha.formatColumn(tblGames, HANDICAP_COL, locale.getString("game.handicap"), HANDICAP_WIDTH, JLabel.CENTER, JLabel.CENTER);
        JFrGotha.formatColumn(tblGames, RESULT_COL, locale.getString("game.result"), RESULT_WIDTH, JLabel.CENTER, JLabel.CENTER);
    }

    private void updateComponents() {
        this.spnRoundNumber.setValue(this.processedRoundNumber + 1);
        
        // If Team presentation, color may vary in each column
        if (this.ckbTeamOrder.isSelected()){
            JFrGotha.formatColumn(tblGames, LEFT_PLAYER_COL, "", PLAYER_WIDTH, JLabel.LEFT, JLabel.CENTER);
            JFrGotha.formatColumn(tblGames, RIGHT_PLAYER_COL, "", PLAYER_WIDTH, JLabel.LEFT, JLabel.CENTER);
        }
        // else White = left, Black = right
        else{
            JFrGotha.formatColumn(tblGames, LEFT_PLAYER_COL, locale.getString("game.white"), PLAYER_WIDTH, JLabel.LEFT, JLabel.CENTER);
            JFrGotha.formatColumn(tblGames, RIGHT_PLAYER_COL, locale.getString("game.black"), PLAYER_WIDTH, JLabel.LEFT, JLabel.CENTER);
        }
        
 
        ArrayList<Game> alCurrentActualGames = null;
        try {
            alCurrentActualGames = tournament.gamesList(processedRoundNumber);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesResults.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (int nCol = 0; nCol < tblGames.getColumnCount(); nCol++) {
            TableColumn col = tblGames.getColumnModel().getColumn(nCol);
            col.setCellRenderer(new ResultsTableCellRenderer());
        }
        boolean bTeamOrder = this.ckbTeamOrder.isSelected();
        if (bTeamOrder) {
            fillTeamGamesTable(tblGames);
        } else {
            fillGamesTable(alCurrentActualGames, tblGames);
        }
    }

    private void fillGamesTable(ArrayList<Game> alG, JTable tblG) {
        // sort
        ArrayList<Game> alDisplayedGames = new ArrayList<Game>(alG);

        GameComparator gameComparator = new GameComparator(gamesSortType);
        Collections.sort(alDisplayedGames, gameComparator);

        DefaultTableModel model = (DefaultTableModel) tblGames.getModel();
        model.setRowCount(alDisplayedGames.size());

        for (int iG = 0; iG < alDisplayedGames.size(); iG++) {
            Game g = alDisplayedGames.get(iG);
            String strResult = g.resultAsString(true);
            model.setValueAt(strResult, iG, RESULT_COL);
            int col = 0;
            model.setValueAt("" + (g.getTableNumber() + 1), iG, col++);
            Player wP = g.getWhitePlayer();
            model.setValueAt(wP.fullName(), iG, col++);
            Player bP = g.getBlackPlayer();
            model.setValueAt(bP.fullName(), iG, col++);
            model.setValueAt("" + g.getHandicap(), iG, col++);
        }
    }

    private void fillTeamGamesTable(JTable tblM) {
        ArrayList<Match> alMatches = null;
        TeamTournamentParameterSet ttps = null;
        try {
            alMatches = tournament.matchesList(processedRoundNumber);
            ttps = tournament.getTeamTournamentParameterSet();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesResults.class.getName()).log(Level.SEVERE, null, ex);
        }
        int teamSize = ttps.getTeamGeneralParameterSet().getTeamSize();

        ArrayList<ComparableMatch> alCM = ComparableMatch.buildComparableMatchesArray(alMatches, tournament, processedRoundNumber);

        MatchComparator matchComparator = new MatchComparator(MatchComparator.BOARD0_TABLE_NUMBER_ORDER);
        Collections.sort(alCM, matchComparator);

        DefaultTableModel model = (DefaultTableModel) tblM.getModel();
        int numberOfLines = (teamSize + 1) * alCM.size();
        model.setRowCount(numberOfLines);
        
        int ln = 0;
        for (ComparableMatch cm : alCM) {
            Match m = null;
            try {
                m = tournament.getMatch(processedRoundNumber, cm.board0TableNumber);
            } catch (RemoteException ex) {
                Logger.getLogger(JFrGamesResults.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (m == null) continue;
            
//            int wResult = m.getTeamScore(m.getWhiteTeam());
//            int bResult = m.getTeamScore(m.getBlackTeam());
//            String strWTeamResult = "" + Gotha.formatFractNumber(wResult, 1);
//            String strBTeamResult = "" + Gotha.formatFractNumber(bResult, 1);
//            String strTeamResult = strWTeamResult + "-" + strBTeamResult;
//            model.setValueAt(strTeamResult, ln, RESULT_COL);
            
            String strWTeamNbW = Gotha.formatFractNumber(m.getWX2(m.getWhiteTeam()), 2);
            String strBTeamNbW = Gotha.formatFractNumber(m.getWX2(m.getBlackTeam()), 2);
            model.setValueAt(strWTeamNbW + "-" + strBTeamNbW, ln, RESULT_COL);
 
            model.setValueAt("" + (cm.board0TableNumber + 1) + "---", ln, JFrGamesResults.TABLE_NUMBER_COL);
            Team wt = m.getWhiteTeam();
            Team bt = m.getBlackTeam();
            model.setValueAt(wt.getTeamName(), ln, JFrGamesResults.LEFT_PLAYER_COL);
            model.setValueAt(bt.getTeamName(), ln, JFrGamesResults.RIGHT_PLAYER_COL);
            model.setValueAt("", ln, JFrGamesResults.HANDICAP_COL);

            ln++;
            for (int ib = 0; ib < teamSize; ib++) {
                Game g = null;
                try {
                    Player wPlayer = m.getWhiteTeam().getTeamMember(processedRoundNumber, ib);
                    Player bPlayer = m.getBlackTeam().getTeamMember(processedRoundNumber, ib);                   
                    g = tournament.getGame(processedRoundNumber, wPlayer);
                    if (!bPlayer.hasSameKeyString(tournament.opponent(g, wPlayer))) g = null;
                } catch (RemoteException ex) {
                    Logger.getLogger(JFrGamesResults.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (g == null){
                    model.setValueAt("", ln, RESULT_COL);
                    model.setValueAt("", ln, JFrGamesResults.TABLE_NUMBER_COL);
                    model.setValueAt("", ln, JFrGamesResults.LEFT_PLAYER_COL);
                    model.setValueAt("", ln, JFrGamesResults.RIGHT_PLAYER_COL);
                    model.setValueAt("", ln, JFrGamesResults.HANDICAP_COL);

                    ln++;
                    continue;
                }
                Player wP = g.getWhitePlayer();
                Player bP = g.getBlackPlayer();

                boolean wb = this.wbOrder(processedRoundNumber, g.getTableNumber());
                Player p1 = wP;
                Player p2 = bP;
                String strP1Color = "(w)";
                String strP2Color = "(b)";
                if (!wb) {
                    p1 = bP;
                    p2 = wP;
                    strP1Color = "(b)";
                    strP2Color = "(w)";
                }
                String strResult = g.resultAsString(wb);
                model.setValueAt(strResult, ln, RESULT_COL);
                model.setValueAt("" + (g.getTableNumber() + 1), ln, JFrGamesResults.TABLE_NUMBER_COL);
                model.setValueAt(p1.fullName() + strP1Color, ln, JFrGamesResults.LEFT_PLAYER_COL);
                model.setValueAt(p2.fullName() + strP2Color, ln, JFrGamesResults.RIGHT_PLAYER_COL);
                model.setValueAt("" + g.getHandicap(), ln, JFrGamesResults.HANDICAP_COL);

                ln++;
            }
        }
    }

    private void initComponents() {

        pnlGames = new javax.swing.JPanel();
        scpGames = new javax.swing.JScrollPane();
        tblGames = new javax.swing.JTable();
        btnPrint = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        txfSearchPlayer = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        spnRoundNumber = new javax.swing.JSpinner();
        btnHelp = new javax.swing.JButton();
        ckbTeamOrder = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Games .. Results");
        getContentPane().setLayout(new MigLayout("flowy", "[fill]unrel[]", "[growprio 0]unrel[]unrel[][][]unrel[]unrel[]"));

        add(new JLabel(locale.getString("tournament.round")), "split 2, flowx");

        spnRoundNumber.addChangeListener(this::spnRoundNumberStateChanged);
        add(spnRoundNumber, "wmin 40lp, hmin 30lp, gapleft push");

        ckbTeamOrder.setText(locale.getString("game.team_presentation"));
        ckbTeamOrder.addActionListener(this::ckbTeamOrderActionPerformed);
        add(ckbTeamOrder);

        add(new JLabel(locale.getString("player.search")));

        add(txfSearchPlayer);

        btnSearch.setText(locale.getString("player.btn_search"));
        btnSearch.addActionListener(this::btnSearchActionPerformed);
        add(btnSearch, "pushy 100, aligny top");

        btnHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/info/vannier/gotha/gothalogo16.jpg")));
        btnHelp.setText(locale.getString("btn.help"));
        btnHelp.addActionListener(this::btnHelpActionPerformed);
        add(btnHelp, "wrap");

        pnlGames.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("game.games")));
        pnlGames.setLayout(new MigLayout("flowy, insets 0", "[grow, fill]", "[][]unrel[]"));

        tblGames.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "Table", "White", "Black", "Hd", "Result"
            }
        ) {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        });
        tblGames.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tblGamesMousePressed(evt);
            }
        });
        scpGames.setViewportView(tblGames);

        pnlGames.add(scpGames, "grow, push");

        pnlGames.add(new JLabel(locale.getString("game.results.help")));

        btnPrint.setText(locale.getString("btn.print"));
        btnPrint.addActionListener(this::btnPrintActionPerformed);
        pnlGames.add(btnPrint);

        add(pnlGames, "grow, push, spany 5");

        btnClose.setText(locale.getString("btn.close"));
        btnClose.addActionListener(this::btnCloseActionPerformed);
        add(btnClose, "grow");

        pack();
    }

    private void tblGamesMousePressed(java.awt.event.MouseEvent evt) {
        int r = tblGames.rowAtPoint(evt.getPoint());
        int c = tblGames.columnAtPoint(evt.getPoint());
        Game g = null;
        int tn = -1;
        try {
            String strTableNumber = "" + tblGames.getModel().getValueAt(r, TABLE_NUMBER_COL);
            tn = Integer.parseInt(strTableNumber) - 1;
            g = tournament.getGame(processedRoundNumber, tn);
        } catch (NumberFormatException ex) {
            return;
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesResults.class.getName()).log(Level.SEVERE, null, ex);
        }

        int oldResult = g.getResult();
        int newResult = oldResult;
        
        boolean wb = this.wbOrder(processedRoundNumber, tn);
        if (this.ckbTeamOrder.isSelected()) wb = this.wbOrder(processedRoundNumber, tn);
        else wb = true;

        if ((c == LEFT_PLAYER_COL && wb) || (c == RIGHT_PLAYER_COL && !wb)) {
            newResult = Game.RESULT_WHITEWINS;
        } else if ((c == LEFT_PLAYER_COL && !wb) || (c == RIGHT_PLAYER_COL && wb)) {
            newResult = Game.RESULT_BLACKWINS;
        } else if (c == TABLE_NUMBER_COL) {
            newResult = Game.RESULT_UNKNOWN;
        } else if (c == RESULT_COL) {
            if (evt.getModifiers() == InputEvent.BUTTON3_MASK) {
                if (oldResult == Game.RESULT_WHITEWINS) {
                    newResult = Game.RESULT_WHITEWINS_BYDEF;
                }
                if (oldResult == Game.RESULT_BLACKWINS) {
                    newResult = Game.RESULT_BLACKWINS_BYDEF;
                }
                if (oldResult == Game.RESULT_EQUAL) {
                    newResult = Game.RESULT_EQUAL_BYDEF;
                }
                if (oldResult == Game.RESULT_BOTHWIN) {
                    newResult = Game.RESULT_BOTHWIN_BYDEF;
                }
                if (oldResult == Game.RESULT_BOTHLOSE) {
                    newResult = Game.RESULT_BOTHLOSE_BYDEF;
                }
                if (oldResult == Game.RESULT_WHITEWINS_BYDEF) {
                    newResult = Game.RESULT_WHITEWINS;
                }
                if (oldResult == Game.RESULT_BLACKWINS_BYDEF) {
                    newResult = Game.RESULT_BLACKWINS;
                }
                if (oldResult == Game.RESULT_EQUAL_BYDEF) {
                    newResult = Game.RESULT_EQUAL;
                }
                if (oldResult == Game.RESULT_BOTHWIN_BYDEF) {
                    newResult = Game.RESULT_BOTHWIN;
                }
                if (oldResult == Game.RESULT_BOTHLOSE_BYDEF) {
                    newResult = Game.RESULT_BOTHLOSE;
                }
            } else {
                if (oldResult == Game.RESULT_UNKNOWN) {
                    newResult = Game.RESULT_WHITEWINS;
                } else if (oldResult == Game.RESULT_WHITEWINS) {
                    newResult = Game.RESULT_BLACKWINS;
                } else if (oldResult == Game.RESULT_BLACKWINS) {
                    newResult = Game.RESULT_EQUAL;
                } else if (oldResult == Game.RESULT_EQUAL) {
                    newResult = Game.RESULT_BOTHWIN;
                } else if (oldResult == Game.RESULT_BOTHWIN) {
                    newResult = Game.RESULT_BOTHLOSE;
                } else if (oldResult == Game.RESULT_BOTHLOSE) {
                    newResult = Game.RESULT_WHITEWINS_BYDEF;
                } else if (oldResult == Game.RESULT_WHITEWINS_BYDEF) {
                    newResult = Game.RESULT_BLACKWINS_BYDEF;
                } else if (oldResult == Game.RESULT_BLACKWINS_BYDEF) {
                    newResult = Game.RESULT_EQUAL_BYDEF;
                } else if (oldResult == Game.RESULT_EQUAL_BYDEF) {
                    newResult = Game.RESULT_BOTHWIN_BYDEF;
                } else if (oldResult == Game.RESULT_BOTHWIN_BYDEF) {
                    newResult = Game.RESULT_BOTHLOSE_BYDEF;
                } else if (oldResult == Game.RESULT_BOTHLOSE_BYDEF) {
                    newResult = Game.RESULT_UNKNOWN;
                }
            }
        }

        if (newResult == oldResult) {
            return;
        }
        try {
            tournament.setResult(g, newResult);
            this.tournamentChanged();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesResults.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {
        if (!this.ckbTeamOrder.isSelected()) TournamentPrinting.printGamesList(tournament, processedRoundNumber);
        else TournamentPrinting.printMatchesList(tournament, processedRoundNumber);

    }

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {
        cleanClose();
    }

    private void cleanClose(){
        running = false;
        dispose();
    }

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {
        String strSearchPlayer = this.txfSearchPlayer.getText().toLowerCase();
        if (strSearchPlayer.length() == 0) {
            tblGames.clearSelection();
            return;
        }
        TableModel model = tblGames.getModel();
        int rowNumber = -1;
        int startRow = tblGames.getSelectedRow() + 1;
        int nbRows = model.getRowCount();
        for (int iR = 0; iR < nbRows; iR++) {
            int row = (startRow + iR) % nbRows;
            String str = (String) model.getValueAt(row, LEFT_PLAYER_COL);
            str = str.toLowerCase();
            if (str.indexOf(strSearchPlayer) >= 0) {
                rowNumber = row;
                break;
            }
            str = (String) model.getValueAt(row, RIGHT_PLAYER_COL);
            str = str.toLowerCase();
            if (str.indexOf(strSearchPlayer) >= 0) {
                rowNumber = row;
                break;
            }
        }

        tblGames.clearSelection();
        if (rowNumber == -1) {
            JOptionPane.showMessageDialog(this,
                    locale.format("game.pairing.player_not_participating_in_round", this.processedRoundNumber + 1),
                    locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
        } else {
            tblGames.setRowSelectionAllowed(true);
            tblGames.clearSelection();
            tblGames.addRowSelectionInterval(rowNumber, rowNumber);

            Rectangle rect = tblGames.getCellRect(rowNumber, 0, true);
            tblGames.scrollRectToVisible(rect);
        }

        tblGames.repaint();
    }

    private void spnRoundNumberStateChanged(javax.swing.event.ChangeEvent evt) {
        int demandedRN = (Integer) (spnRoundNumber.getValue()) - 1;
        this.demandedDisplayedRoundNumberHasChanged(demandedRN);
}

    private void btnHelpActionPerformed(java.awt.event.ActionEvent evt) {
        Gotha.displayGothaHelp("Games Results frame");
}

    private void ckbTeamOrderActionPerformed(java.awt.event.ActionEvent evt) {
        this.updateAllViews();
    }
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnHelp;
    private javax.swing.JButton btnPrint;
    private javax.swing.JButton btnSearch;
    private javax.swing.JCheckBox ckbTeamOrder;
    private javax.swing.JPanel pnlGames;
    private javax.swing.JScrollPane scpGames;
    private javax.swing.JSpinner spnRoundNumber;
    private javax.swing.JTable tblGames;
    private javax.swing.JTextField txfSearchPlayer;

    private void tournamentChanged() {
        try {
            tournament.setLastTournamentModificationTime(tournament.getCurrentTournamentTime());
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesResults.class.getName()).log(Level.SEVERE, null, ex);
        }
        updateAllViews();
    }

    private void updateAllViews() {
        try {
            if (!tournament.isOpen()) cleanClose();
            this.lastComponentsUpdateTime = tournament.getCurrentTournamentTime();
            setTitle(locale.format("game.results.window_title", tournament.getFullName()));
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesResults.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        int nbRounds = Gotha.MAX_NUMBER_OF_ROUNDS;
        try {
            nbRounds = tournament.getTournamentParameterSet().getGeneralParameterSet().getNumberOfRounds();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesPair.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (this.processedRoundNumber >= nbRounds) {
            JOptionPane.showMessageDialog(this, locale.getString("tournament.round_count_changed"),
                    locale.getString("alert.message"), JOptionPane.WARNING_MESSAGE);
            this.processedRoundNumber = nbRounds - 1;
        }
        
        updateComponents();
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

    // finds the 
    /**
     * finds g, the game at tn table number and rn round number finds m, the
     * match containing g if white player of g is member of black team in m
     * match, returns false else return true
     *
     * @param rn round number
     * @param tn table number
     * @return
     */
    private boolean wbOrder(int rn, int tn) {
        Game g = null;
        Player wP = null;
        Match m = null;
        Team bT = null;
        try {
            g = tournament.getGame(rn, tn);
            if (g == null) {
                return true;
            }
            wP = g.getWhitePlayer();
            if (wP == null) return true;
            m = tournament.getMatch(rn, tn);
            if (m == null) return true;
            bT = m.getBlackTeam();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesResults.class.getName()).log(Level.SEVERE, null, ex);
        }
        int bn = bT.boardNumber(rn, wP);
        if (bn >= 0) {
            return false;
        }
        return true;

    }
}

class ResultsTableCellRenderer extends JLabel implements TableCellRenderer {
    // This method is called each time a cell in a column
    // using this renderer needs to be rendered.

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int rowIndex, int colIndex) {
        Font gameFont = new Font("Arial", Font.BOLD, 12);
        Font teamFont = new Font("Arial", Font.PLAIN, 16);
        TableModel model = table.getModel();
        setText("" + model.getValueAt(rowIndex, colIndex));

        String strResult = "" + model.getValueAt(rowIndex, JFrGamesResults.RESULT_COL);
        String strWR = "";
        String strBR = "";
        if (strResult.length() >= 3) {
            strWR = strResult.substring(0, 1);
            strBR = strResult.substring(2, 3);
        }

        String strTN = "" + model.getValueAt(rowIndex, JFrGamesResults.TABLE_NUMBER_COL);
        boolean teamLine = false;
        if ((strTN.length() > 0) && strTN.charAt(strTN.length() - 1) == '-'){
            teamLine = true;
        }

        if (teamLine) {
            // team Line   
            setFont(teamFont);
            setForeground(Color.BLACK);
        } else {
            // Game line
            setFont(gameFont);
            if (colIndex == JFrGamesResults.LEFT_PLAYER_COL) {
                setFont(this.getFont().deriveFont(Font.PLAIN));

                if (strWR.compareTo("1") == 0) {
                    setForeground(Color.RED);
                } else if (strWR.compareTo("0") == 0) {
                    setForeground(Color.BLUE);
                } else if (strWR.compareTo("½") == 0) {
                    setForeground(Color.MAGENTA);
                } else {
                    setForeground(Color.BLACK);
                    setFont(this.getFont().deriveFont(Font.PLAIN));
                }
            } else if (colIndex == JFrGamesResults.RIGHT_PLAYER_COL) {
                setFont(this.getFont().deriveFont(Font.PLAIN));
                if (strBR.compareTo("1") == 0) {
                    setForeground(Color.RED);
                } else if (strBR.compareTo("0") == 0) {
                    setForeground(Color.BLUE);
                } else if (strBR.compareTo("½") == 0) {
                    setForeground(Color.MAGENTA);
                } else {
                    setForeground(Color.BLACK);
                }
            } else {
                setFont(this.getFont().deriveFont(Font.PLAIN));
            }
        }

        if (isSelected) // setFont(new Font("Arial", Font.BOLD, 12));
        {
            setFont(this.getFont().deriveFont(Font.BOLD));
        }

        return this;
    }
}
