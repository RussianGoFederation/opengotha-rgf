package info.vannier.gotha;

import net.miginfocom.swing.MigLayout;
import ru.gofederation.gotha.model.Player;
import ru.gofederation.gotha.util.GothaLocale;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Luc Vannier
 */
public class JFrPublish extends javax.swing.JFrame {
    private static final long REFRESH_DELAY = 2000;
    private long lastComponentsUpdateTime = 0;
    private TournamentInterface tournament;
    int processedRoundNumber = 0;

	private final GothaLocale locale = GothaLocale.getCurrentLocale();

    /**
     * Creates new form JFrPublish
     */
    public JFrPublish(TournamentInterface tournament) throws RemoteException {
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

        grpGameFormat = new javax.swing.ButtonGroup();
        jLabel4 = new javax.swing.JLabel();
        grpSortType = new javax.swing.ButtonGroup();
        grpRemote = new javax.swing.ButtonGroup();
        jLabel9 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        btnClose = new javax.swing.JButton();
        tpnPublish = new javax.swing.JTabbedPane();
        pnlContents = new javax.swing.JPanel();
        pnlPL = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        rdbSortByName = new javax.swing.JRadioButton();
        rdbSortByRank = new javax.swing.JRadioButton();
        rdbSortByGrade = new javax.swing.JRadioButton();
        pnlGL = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        ckbShowPlayerGrade = new javax.swing.JCheckBox();
        ckbShowPlayerCountry = new javax.swing.JCheckBox();
        ckbShowPlayerClub = new javax.swing.JCheckBox();
        pnlNPP = new javax.swing.JPanel();
        ckbShowByePlayer = new javax.swing.JCheckBox();
        ckbShowNotPairedPlayers = new javax.swing.JCheckBox();
        ckbShowNotParticipatingPlayers = new javax.swing.JCheckBox();
        ckbShowNotFinallyRegisteredPlayers = new javax.swing.JCheckBox();
        pnlSt = new javax.swing.JPanel();
        ckbDisplayNPPlayers = new javax.swing.JCheckBox();
        ckbDisplayNumCol = new javax.swing.JCheckBox();
        ckbDisplayPlCol = new javax.swing.JCheckBox();
        ckbDisplayCoCol = new javax.swing.JCheckBox();
        ckbDisplayClCol = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        rdbGameFormatFull = new javax.swing.JRadioButton();
        rdbGameFormatShort = new javax.swing.JRadioButton();
        pnlML = new javax.swing.JPanel();
        ckbDisplayIndGames = new javax.swing.JCheckBox();
        pnlPar = new javax.swing.JPanel();
        pnlActions = new javax.swing.JPanel();
        ckbPrint = new javax.swing.JCheckBox();
        ckbExportToLocalFile = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        ckbHTMLAutoscroll = new javax.swing.JCheckBox();
        pnlPub = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        spnRoundNumber = new javax.swing.JSpinner();
        btnPublishPL = new javax.swing.JButton();
        btnPrintTP = new javax.swing.JButton();
        btnPublishGL = new javax.swing.JButton();
        btnPrintNPP = new javax.swing.JButton();
        btnPublishSt = new javax.swing.JButton();
        btnExportRLEGF = new javax.swing.JButton();
        btnExportRLFFG = new javax.swing.JButton();
        btnExportRLAGA = new javax.swing.JButton();
        btnExportPlayersCSV = new javax.swing.JButton();
        pnlTeams = new javax.swing.JPanel();
        btnPublishTL = new javax.swing.JButton();
        btnPublishML = new javax.swing.JButton();
        btnPublishTS = new javax.swing.JButton();
        btnPrintRS = new javax.swing.JButton();
        btnHelp = new javax.swing.JButton();

        jLabel4.setText("jLabel4");

        jLabel9.setText("jLabel9");

        jCheckBox1.setText("jCheckBox1");

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new MigLayout("insets dialog"));

        pnlContents.setLayout(new MigLayout("insets panel"));

        pnlPL.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("publish.player_list")));
        pnlPL.setLayout(new MigLayout("insets panel, flowy"));

        jLabel1.setText(locale.getString("publish.player_list.sort_by"));
        pnlPL.add(jLabel1);

        grpSortType.add(rdbSortByName);
        rdbSortByName.setText(locale.getString("publish.player_list.sort_by.name"));
        rdbSortByName.addActionListener(this::allSortRDBActionPerformed);
        pnlPL.add(rdbSortByName, "gapleft indent");

        rdbSortByRank.setText(locale.getString("publish.player_list.sort_by.rank"));
        rdbSortByRank.setEnabled(false);
        rdbSortByRank.addActionListener(this::allSortRDBActionPerformed);
        pnlPL.add(rdbSortByRank, "gapleft indent");

        grpSortType.add(rdbSortByGrade);
        rdbSortByGrade.setText(locale.getString("publish.player_list.sort_by.grade"));
        rdbSortByGrade.addActionListener(this::allSortRDBActionPerformed);
        pnlPL.add(rdbSortByGrade, "gapleft indent");

        pnlContents.add(pnlPL);

        pnlGL.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("publish.games_list")));
        pnlGL.setLayout(new MigLayout("insets panel, flowy"));

        jLabel3.setText(locale.getString("publish.games_list.for_each_player"));
        pnlGL.add(jLabel3);

        ckbShowPlayerGrade.setSelected(true);
        ckbShowPlayerGrade.setText(locale.getString("publish.games_list.grade"));
        ckbShowPlayerGrade.addActionListener(this::allContentsCKBActionPerformed);
        pnlGL.add(ckbShowPlayerGrade, "gapleft indent");

        ckbShowPlayerCountry.setText(locale.getString("publish.games_list.country"));
        ckbShowPlayerCountry.addActionListener(this::allContentsCKBActionPerformed);
        pnlGL.add(ckbShowPlayerCountry, "gapleft indent");

        ckbShowPlayerClub.setSelected(true);
        ckbShowPlayerClub.setText(locale.getString("publish.games_list.club"));
        ckbShowPlayerClub.addActionListener(this::allContentsCKBActionPerformed);
        pnlGL.add(ckbShowPlayerClub, "gapleft indent");

        pnlContents.add(pnlGL, "wrap");

        pnlNPP.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("publish.non_playing_player_list")));
        pnlNPP.setLayout(new MigLayout("flowy, insets panel"));

        ckbShowByePlayer.setSelected(true);
        ckbShowByePlayer.setText(locale.getString("publish.non_playing_player_list.show_bye"));
        ckbShowByePlayer.setEnabled(false);
        ckbShowByePlayer.addActionListener(this::allContentsCKBActionPerformed);
        pnlNPP.add(ckbShowByePlayer);

        ckbShowNotPairedPlayers.setSelected(true);
        ckbShowNotPairedPlayers.setText(locale.getString("publish.non_playing_player_list.show_unpaired"));
        ckbShowNotPairedPlayers.addActionListener(this::allContentsCKBActionPerformed);
        pnlNPP.add(ckbShowNotPairedPlayers);

        ckbShowNotParticipatingPlayers.setText(locale.getString("publish.non_playing_player_list.show_not_participating"));
        ckbShowNotParticipatingPlayers.addActionListener(this::allContentsCKBActionPerformed);
        pnlNPP.add(ckbShowNotParticipatingPlayers);

        ckbShowNotFinallyRegisteredPlayers.setSelected(true);
        ckbShowNotFinallyRegisteredPlayers.setText(locale.getString("publish.non_playing_player_list.show_non_final_registration"));
        ckbShowNotFinallyRegisteredPlayers.setEnabled(false);
        ckbShowNotFinallyRegisteredPlayers.addActionListener(this::allContentsCKBActionPerformed);
        pnlNPP.add(ckbShowNotFinallyRegisteredPlayers);

        pnlContents.add(pnlNPP);

        pnlML.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("publish.matches")));
        pnlML.setLayout(new MigLayout("insets panel"));

        ckbDisplayIndGames.setSelected(true);
        ckbDisplayIndGames.setText(locale.getString("publish.matches.individual_games"));
        ckbDisplayIndGames.addActionListener(this::allContentsCKBActionPerformed);
        pnlML.add(ckbDisplayIndGames);

        pnlContents.add(pnlML, "wrap");

        pnlSt.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("publish.standings")));
        pnlSt.setLayout(new MigLayout("insets panel, flowy"));

        ckbDisplayNPPlayers.setText(locale.getString("publish.standings.display_non_playing"));
        ckbDisplayNPPlayers.addActionListener(this::allContentsCKBActionPerformed);
        pnlSt.add(ckbDisplayNPPlayers, "wrap");

        ckbDisplayNumCol.setSelected(true);
        ckbDisplayNumCol.setText(locale.getString("publish.standings.display_num"));
        ckbDisplayNumCol.addActionListener(this::allContentsCKBActionPerformed);
        pnlSt.add(ckbDisplayNumCol);

        ckbDisplayPlCol.setSelected(true);
        ckbDisplayPlCol.setText(locale.getString("publish.standings.display_pl"));
        ckbDisplayPlCol.addActionListener(this::allContentsCKBActionPerformed);
        pnlSt.add(ckbDisplayPlCol);

        ckbDisplayCoCol.setText(locale.getString("publish.standings.display_country"));
        ckbDisplayCoCol.addActionListener(this::allContentsCKBActionPerformed);
        pnlSt.add(ckbDisplayCoCol);

        ckbDisplayClCol.setText(locale.getString("publish.standings.display_club"));
        ckbDisplayClCol.addActionListener(this::allContentsCKBActionPerformed);
        pnlSt.add(ckbDisplayClCol, "wrap");

        jLabel5.setText(locale.getString("publish.standings.game_format"));
        pnlSt.add(jLabel5);

        grpGameFormat.add(rdbGameFormatFull);
        rdbGameFormatFull.setText(locale.getString("publish.standings.game_format_full"));
        rdbGameFormatFull.addActionListener(this::allGameFormatRDBActionPerformed);
        pnlSt.add(rdbGameFormatFull, "gapleft indent");

        grpGameFormat.add(rdbGameFormatShort);
        rdbGameFormatShort.setText(locale.getString("publish.standings.game_format_short"));
        rdbGameFormatShort.addActionListener(this::allGameFormatRDBActionPerformed);
        pnlSt.add(rdbGameFormatShort, "gapleft indent");

        pnlContents.add(pnlSt, "spanx 2");

        tpnPublish.addTab(locale.getString("publish.contents"), pnlContents);

        pnlPar.setLayout(new MigLayout("insets panel"));

        pnlActions.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("publish.buttons_action")));
        pnlActions.setLayout(new MigLayout("flowy, insets panel"));

        ckbPrint.setSelected(true);
        ckbPrint.setText(locale.getString("publish.buttons_action_print"));
        ckbPrint.addActionListener(this::allParametersCKBActionPerformed);
        pnlActions.add(ckbPrint);

        ckbExportToLocalFile.setSelected(true);
        ckbExportToLocalFile.setText(locale.getString("publish.buttons_action_export_file"));
        ckbExportToLocalFile.setEnabled(false);
        ckbExportToLocalFile.addActionListener(this::allParametersCKBActionPerformed);
        pnlActions.add(ckbExportToLocalFile);

        pnlPar.add(pnlActions);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("publish.html_export")));
        jPanel1.setLayout(new MigLayout("insets panel"));

        ckbHTMLAutoscroll.setText(locale.getString("publish.html_export_autoscroll"));
        ckbHTMLAutoscroll.setToolTipText(locale.getString("publish.html_export_autoscroll_tooltip"));
        ckbHTMLAutoscroll.addActionListener(this::allParametersCKBActionPerformed);
        jPanel1.add(ckbHTMLAutoscroll);

        pnlPar.add(jPanel1);

        tpnPublish.addTab(locale.getString("publish.parameters"), pnlPar);

        pnlPub.setLayout(new MigLayout("insets panel, wrap 3", "[fill, sg]u[fill, sg]u[fill, sg]", "[]u[][][]u[]u[]"));

        jLabel2.setText(locale.getString("tournament.round"));
        pnlPub.add(jLabel2, "skip 1, split 2, gapleft push");

        spnRoundNumber.addChangeListener(this::spnRoundNumberStateChanged);
        pnlPub.add(spnRoundNumber, "gapright push, wrap");

        btnPublishPL.setText(locale.getString("publish.publish_player_list"));
        btnPublishPL.addActionListener(this::btnPublishPLActionPerformed);
        pnlPub.add(btnPublishPL, "h min*1.5, sg big");

        btnPublishGL.setText(locale.getString("publish.publish_games"));
        btnPublishGL.addActionListener(this::btnPublishGLActionPerformed);
        pnlPub.add(btnPublishGL, "sg big");

        btnExportRLEGF.setForeground(new java.awt.Color(255, 0, 0));
        btnExportRLEGF.setText(locale.getString("publish.publish_export_egf"));
        btnExportRLEGF.addActionListener(this::btnExportRLEGFActionPerformed);
        pnlPub.add(btnExportRLEGF, "sg big, split 4, flowy, gapbottom push, spany 5");
        btnExportRLEGF.setBounds(540, 80, 230, 50);

        btnExportRLFFG.setForeground(new java.awt.Color(255, 0, 0));
        btnExportRLFFG.setText(locale.getString("publish.publish_export_ffg"));
        btnExportRLFFG.addActionListener(this::btnExportRLFFGActionPerformed);
        pnlPub.add(btnExportRLFFG, "sg big, gapbottom push");
        btnExportRLFFG.setBounds(540, 180, 230, 50);

        btnExportRLAGA.setForeground(new java.awt.Color(255, 0, 0));
        btnExportRLAGA.setText(locale.getString("publish.publish_export_aga"));
        btnExportRLAGA.addActionListener(this::btnExportRLAGAActionPerformed);
        pnlPub.add(btnExportRLAGA, "sg big, gapbottom push");
        btnExportRLAGA.setBounds(540, 280, 230, 50);

        btnExportPlayersCSV.setForeground(new java.awt.Color(0, 128, 0));
        btnExportPlayersCSV.setText(locale.getString("publish.publish_export_players_csv"));
        btnExportPlayersCSV.addActionListener(this::btnExportPlayersCSVActionPerformed);
        pnlPub.add(btnExportPlayersCSV, "sg big");
        btnExportPlayersCSV.setBounds(540, 380, 230, 50);

        btnPrintTP.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnPrintTP.setForeground(new java.awt.Color(0, 0, 255)); // TODO: theme
        btnPrintTP.setText(locale.getString("publish.publish_print_tournament_settings"));
        btnPrintTP.addActionListener(this::btnPrintTPActionPerformed);
        pnlPub.add(btnPrintTP);

        btnPrintRS.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnPrintRS.setForeground(new java.awt.Color(0, 0, 255));
        btnPrintRS.setText(locale.getString("publish.publish_print_result_sheets"));
        btnPrintRS.addActionListener(this::btnPrintRSActionPerformed);
        pnlPub.add(btnPrintRS, "wrap");
        btnPrintRS.setBounds(260, 130, 260, 25);

        btnPrintNPP.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnPrintNPP.setForeground(new java.awt.Color(0, 0, 255));
        btnPrintNPP.setText(locale.getString("publish.publish_print_non_playing_players"));
        btnPrintNPP.addActionListener(this::btnPrintNPPActionPerformed);
        pnlPub.add(btnPrintNPP, "skip 1, wrap");
        btnPrintNPP.setBounds(260, 170, 260, 25);

        btnPublishSt.setText(locale.getString("publish.publish_standings"));
        btnPublishSt.addActionListener(this::btnPublishStActionPerformed);
        pnlPub.add(btnPublishSt, "sg big, skip 1, wrap");
        btnPublishSt.setBounds(260, 220, 260, 40);

        pnlTeams.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("tournament.teams")));
        pnlTeams.setLayout(new MigLayout("insets panel", "[grow, fill, sg]unrel[grow, fill, sg]"));

        btnPublishTL.setText(locale.getString("publish.publish_teams_list"));
        btnPublishTL.addActionListener(this::btnPublishTLActionPerformed);
        pnlTeams.add(btnPublishTL, "sg big, h min*1.5");
        btnPublishTL.setBounds(10, 30, 230, 50);

        btnPublishML.setText(locale.getString("publish.publish_team_matches"));
        btnPublishML.addActionListener(this::btnPublishMLActionPerformed);
        pnlTeams.add(btnPublishML, "sg big, wrap");
        btnPublishML.setBounds(260, 30, 260, 50);

        btnPublishTS.setText(locale.getString("publish.publish_team_standings"));
        btnPublishTS.addActionListener(this::btnPublishTSActionPerformed);
        pnlTeams.add(btnPublishTS, "sg big, skip 1");
        btnPublishTS.setBounds(260, 120, 260, 50);

        pnlPub.add(pnlTeams, "spanx 2");
        pnlTeams.setBounds(0, 260, 530, 180);

        tpnPublish.addTab(locale.getString("publish.publish"), pnlPub);

        getContentPane().add(tpnPublish, "wrap");

        btnHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/info/vannier/gotha/gothalogo16.jpg")));
        btnHelp.setText(locale.getString("btn.help"));
        btnHelp.addActionListener(this::btnHelpActionPerformed);
        getContentPane().add(btnHelp, "split 2, tag help");

        btnClose.setText(locale.getString("btn.close"));
        btnClose.addActionListener(this::btnCloseActionPerformed);
        getContentPane().add(btnClose, "tag cancel");

        pack();
    }

    private void customInitComponents() throws RemoteException {
        int w = JFrGotha.MEDIUM_FRAME_WIDTH;
        int h = JFrGotha.MEDIUM_FRAME_HEIGHT;

        this.tpnPublish.setBounds(0, 0, w - 6, h - 74);

        this.tpnPublish.setSelectedComponent(pnlPub);

        this.rdbSortByRank.setVisible(false);
        updateAllViews();
    }

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {
        cleanClose();
    }

    private void cleanClose(){
        running = false;
        dispose();
    }

    private void btnExportRLFFGActionPerformed(java.awt.event.ActionEvent evt) {
        if (tournament == null) {
            JOptionPane.showMessageDialog(this, locale.getString("error.no_open_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        TournamentParameterSet tps = null;
        try {
            tps = tournament.getTournamentParameterSet();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        GeneralParameterSet gps = tps.getGeneralParameterSet();
        if (gps.getStrSize().length() == 0 || gps.getBasicTime() == 0) {
            JOptionPane.showMessageDialog(this, locale.getString("publish.err_goban_size_or_thinking_time"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        File f = ExternalDocument.chooseAFileForExport(tournament, Gotha.exportDirectory, "tou");
        if (f == null) {
            return;
        }
        // Keep tournamentDirectory
        Gotha.exportDirectory = f.getParentFile();

        ExternalDocument.generateTouFile(tournament, f);
    }

    private void btnPrintTPActionPerformed(java.awt.event.ActionEvent evt) {
        TournamentPrinting.printTournamentParameters(tournament);
    }

    private void btnExportRLAGAActionPerformed(java.awt.event.ActionEvent evt) {
        if (tournament == null) {
            JOptionPane.showMessageDialog(this, locale.getString("error.no_open_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        // If some players have no aga id, should OpenGotha generate dummy Ids ?
        ArrayList<Player> alP = null;
        try {
            alP = tournament.playersList();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
        int nbPWithoutId = 0;
        for (Player p : alP) {
            if (p.getAgaId().equals("")) {
                nbPWithoutId++;
            }
        }
        if (nbPWithoutId > 0) {
            int response = JOptionPane.showConfirmDialog(this, locale.format("publish.err_no_aga_id", nbPWithoutId), locale.getString("alert.message"), JOptionPane.OK_CANCEL_OPTION);

            if (response == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        File f = ExternalDocument.chooseAFileForExport(tournament, Gotha.exportDirectory, "txt");
        if (f == null) {
            return;
        }
        // Keep tournamentDirectory
        Gotha.exportDirectory = f.getParentFile();

        ExternalDocument.generateAGAResultsFile(tournament, f);
    }

    private void btnExportRLEGFActionPerformed(java.awt.event.ActionEvent evt) {
        if (tournament == null) {
            JOptionPane.showMessageDialog(this, locale.getString("error.no_open_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        File f = ExternalDocument.chooseAFileForExport(tournament, Gotha.exportDirectory, "h9");
        if (f == null) {
            return;
        }
        // Keep tournamentDirectory
        Gotha.exportDirectory = f.getParentFile();

        int response = JOptionPane.showConfirmDialog(this, locale.getString("publish.err_keep_default_results"), locale.getString("alert.message"), JOptionPane.YES_NO_CANCEL_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            ExternalDocument.generateH9File(tournament, f, true);
        } else if (response == JOptionPane.NO_OPTION) {
            ExternalDocument.generateH9File(tournament, f, false);
        } else {
            return;
        }
    }

    private void spnRoundNumberStateChanged(javax.swing.event.ChangeEvent evt) {
        int demandedRN = (Integer) (spnRoundNumber.getValue()) - 1;
        this.demandedDisplayedRoundNumberHasChanged(demandedRN);
    }

    private void btnExportPlayersCSVActionPerformed(java.awt.event.ActionEvent evt) {
        if (tournament == null) {
            JOptionPane.showMessageDialog(this, locale.getString("error.no_open_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        File f = ExternalDocument.chooseAFileForExport(tournament, Gotha.exportDirectory, "csv");
        if (f == null) {
            return;
        }
        // Keep tournamentDirectory
        Gotha.exportDirectory = f.getParentFile();

        ExternalDocument.generatePlayersCSVFile(tournament, f);
    }

    private void btnHelpActionPerformed(java.awt.event.ActionEvent evt) {
        Gotha.displayGothaHelp("Publish menu");
    }

    private void allSortRDBActionPerformed(java.awt.event.ActionEvent evt) {
        TournamentParameterSet tps;
        DPParameterSet dpps;
        try {
            tps = tournament.getTournamentParameterSet();
            dpps = tps.getDPParameterSet();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTournamentOptions.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        boolean somethingHasChanged = false;
        int newPlayerSortType = PlayerComparator.NAME_ORDER;
        if (this.rdbSortByRank.isSelected()) {
            newPlayerSortType = PlayerComparator.RANK_ORDER;
        }
        if (this.rdbSortByGrade.isSelected()) {
            newPlayerSortType = PlayerComparator.GRADE_ORDER;
        }
        if (newPlayerSortType != dpps.getPlayerSortType()) {
            dpps.setPlayerSortType(newPlayerSortType);
            somethingHasChanged = true;
        }

        if (somethingHasChanged) {
            try {
                tournament.setTournamentParameterSet(tps);
                this.tournamentChanged();
            } catch (RemoteException ex) {
                Logger.getLogger(JFrTournamentOptions.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void allGameFormatRDBActionPerformed(java.awt.event.ActionEvent evt) {
        TournamentParameterSet tps;
        DPParameterSet dpps;
        try {
            tps = tournament.getTournamentParameterSet();
            dpps = tps.getDPParameterSet();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTournamentOptions.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        boolean somethingHasChanged = false;
        int newGameFormat = DPParameterSet.DP_GAME_FORMAT_FULL;
        if (this.rdbGameFormatShort.isSelected()) {
            newGameFormat = DPParameterSet.DP_GAME_FORMAT_SHORT;
        }
        if (newGameFormat != dpps.getGameFormat()) {
            dpps.setGameFormat(newGameFormat);
            somethingHasChanged = true;
        }

        if (somethingHasChanged) {
            try {
                tournament.setTournamentParameterSet(tps);
                this.tournamentChanged();
            } catch (RemoteException ex) {
                Logger.getLogger(JFrTournamentOptions.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void allContentsCKBActionPerformed(java.awt.event.ActionEvent evt) {
        TournamentParameterSet tps;
        DPParameterSet dpps;
        try {
            tps = tournament.getTournamentParameterSet();
            dpps = tps.getDPParameterSet();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTournamentOptions.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        boolean oldValue;
        boolean newValue;

        boolean somethingHasChanged = false;

        oldValue = dpps.isShowPlayerGrade();
        newValue = this.ckbShowPlayerGrade.isSelected();
        if (newValue != oldValue) {
            dpps.setShowPlayerGrade(newValue);
            somethingHasChanged = true;
        }
        oldValue = dpps.isShowPlayerCountry();
        newValue = this.ckbShowPlayerCountry.isSelected();
        if (newValue != oldValue) {
            dpps.setShowPlayerCountry(newValue);
            somethingHasChanged = true;
        }
        oldValue = dpps.isShowPlayerClub();
        newValue = this.ckbShowPlayerClub.isSelected();
        if (newValue != oldValue) {
            dpps.setShowPlayerClub(newValue);
            somethingHasChanged = true;
        }
        oldValue = dpps.isShowByePlayer();
        newValue = this.ckbShowByePlayer.isSelected();
        if (newValue != oldValue) {
            dpps.setShowByePlayer(newValue);
            somethingHasChanged = true;
        }
        oldValue = dpps.isShowNotPairedPlayers();
        newValue = this.ckbShowNotPairedPlayers.isSelected();
        if (newValue != oldValue) {
            dpps.setShowNotPairedPlayers(newValue);
            somethingHasChanged = true;
        }
        oldValue = dpps.isShowNotParticipatingPlayers();
        newValue = this.ckbShowNotParticipatingPlayers.isSelected();
        if (newValue != oldValue) {
            dpps.setShowNotParticipatingPlayers(newValue);
            somethingHasChanged = true;
        }
        oldValue = dpps.isShowNotFinallyRegisteredPlayers();
        newValue = this.ckbShowNotFinallyRegisteredPlayers.isSelected();
        if (newValue != oldValue) {
            dpps.setShowNotFinallyRegisteredPlayers(newValue);
            somethingHasChanged = true;
        }

        oldValue = dpps.isDisplayNPPlayers();
        newValue = this.ckbDisplayNPPlayers.isSelected();
        if (newValue != oldValue) {
            dpps.setDisplayNPPlayers(newValue);
            somethingHasChanged = true;
        }

        oldValue = dpps.isDisplayNumCol();
        newValue = this.ckbDisplayNumCol.isSelected();
        if (newValue != oldValue) {
            dpps.setDisplayNumCol(newValue);
            somethingHasChanged = true;
        }
        oldValue = dpps.isDisplayPlCol();
        newValue = this.ckbDisplayPlCol.isSelected();
        if (newValue != oldValue) {
            dpps.setDisplayPlCol(newValue);
            somethingHasChanged = true;
        }
        oldValue = dpps.isDisplayCoCol();
        newValue = this.ckbDisplayCoCol.isSelected();
        if (newValue != oldValue) {
            dpps.setDisplayCoCol(newValue);
            somethingHasChanged = true;
        }
        oldValue = dpps.isDisplayClCol();
        newValue = this.ckbDisplayClCol.isSelected();
        if (newValue != oldValue) {
            dpps.setDisplayClCol(newValue);
            somethingHasChanged = true;
        }
        oldValue = dpps.isDisplayIndGamesInMatches();
        newValue = this.ckbDisplayIndGames.isSelected();
        if (newValue != oldValue) {
            dpps.setDisplayIndGamesInMatches(newValue);
            somethingHasChanged = true;
        }

        if (somethingHasChanged) {
            try {
                tournament.setTournamentParameterSet(tps);
                this.tournamentChanged();
            } catch (RemoteException ex) {
                Logger.getLogger(JFrTournamentOptions.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void allParametersCKBActionPerformed(java.awt.event.ActionEvent evt) {
        TournamentParameterSet tps;
        PublishParameterSet pubPS;
        try {
            tps = tournament.getTournamentParameterSet();
            pubPS = tps.getPublishParameterSet();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTournamentOptions.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        boolean oldValue;
        boolean newValue;

        boolean somethingHasChanged = false;

        oldValue = pubPS.isPrint();
        newValue = this.ckbPrint.isSelected();
        if (newValue != oldValue) {
            pubPS.setPrint(newValue);
            somethingHasChanged = true;
        }
        oldValue = pubPS.isExportToLocalFile();
        newValue = this.ckbExportToLocalFile.isSelected();
        if (newValue != oldValue) {
            pubPS.setExportToLocalFile(newValue);
            somethingHasChanged = true;
        }
        oldValue = pubPS.isHtmlAutoScroll();
        newValue = this.ckbHTMLAutoscroll.isSelected();
        if (newValue != oldValue) {
            pubPS.setHtmlAutoScroll(newValue);
            somethingHasChanged = true;
        }

        if (somethingHasChanged) {
            try {
                tournament.setTournamentParameterSet(tps);
                this.tournamentChanged();
            } catch (RemoteException ex) {
                Logger.getLogger(JFrTournamentOptions.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void btnPublishPLActionPerformed(java.awt.event.ActionEvent evt) {
        TournamentPublishing.publish(tournament, processedRoundNumber,
                TournamentPublishing.TYPE_PLAYERSLIST, TournamentPublishing.SUBTYPE_DEFAULT);
    }

    private void btnPublishGLActionPerformed(java.awt.event.ActionEvent evt) {
        TournamentPublishing.publish(tournament, processedRoundNumber,
                TournamentPublishing.TYPE_GAMESLIST, TournamentPublishing.SUBTYPE_DEFAULT);
    }

    private void btnPublishStActionPerformed(java.awt.event.ActionEvent evt) {
        TournamentPublishing.publish(tournament, processedRoundNumber,
                TournamentPublishing.TYPE_STANDINGS, TournamentPublishing.SUBTYPE_DEFAULT);
    }

    private void btnPrintNPPActionPerformed(java.awt.event.ActionEvent evt) {
        TournamentPrinting.printNotPlayingPlayersList(tournament, processedRoundNumber);
    }

    private void btnPublishMLActionPerformed(java.awt.event.ActionEvent evt) {
        TournamentPublishing.publish(tournament, processedRoundNumber,
                TournamentPublishing.TYPE_MATCHESLIST, TournamentPublishing.SUBTYPE_DEFAULT);
    }

    private void btnPublishTLActionPerformed(java.awt.event.ActionEvent evt) {
        TournamentPublishing.publish(tournament, processedRoundNumber,
                TournamentPublishing.TYPE_TEAMSLIST, TournamentPublishing.SUBTYPE_DEFAULT);
    }

    private void btnPublishTSActionPerformed(java.awt.event.ActionEvent evt) {
       TournamentPublishing.publish(tournament, processedRoundNumber,
                TournamentPublishing.TYPE_TEAMSSTANDINGS, TournamentPublishing.SUBTYPE_DEFAULT);
    }

    private void btnPrintRSActionPerformed(java.awt.event.ActionEvent evt) {
        TournamentPrinting.printResultSheets(tournament, processedRoundNumber);
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

    private void tournamentChanged() {
        try {
            tournament.setLastTournamentModificationTime(tournament.getCurrentTournamentTime());
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTournamentOptions.class.getName()).log(Level.SEVERE, null, ex);
        }
        updateAllViews();
    }

    private void updateAllViews() {
        try {
            if (!tournament.isOpen()) {
                cleanClose();
            }
            this.lastComponentsUpdateTime = tournament.getCurrentTournamentTime();
            setTitle("Publish. " + tournament.getFullName());
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesPair.class.getName()).log(Level.SEVERE, null, ex);
        }

        int nbRounds = Gotha.MAX_NUMBER_OF_ROUNDS;
        try {
            nbRounds = tournament.getTournamentParameterSet().getGeneralParameterSet().getNumberOfRounds();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesPair.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (this.processedRoundNumber >= nbRounds) {
            JOptionPane.showMessageDialog(this, locale.getString("tournament.round_count_changed"), locale.getString("alert.warning"), JOptionPane.WARNING_MESSAGE);
            this.processedRoundNumber = nbRounds - 1;
        }


        updateComponents();

        try {
            this.lastComponentsUpdateTime = tournament.getCurrentTournamentTime();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrPublish.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void updateComponents() {
        try {
            this.updatePnlContents();
            updatePnlPar();
            updatePnlPub();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrPublish.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void updatePnlPub() throws RemoteException {
        this.spnRoundNumber.setValue(this.processedRoundNumber + 1);

        this.btnPublishGL.setText(locale.format("publish.publish_games", processedRoundNumber + 1));
        this.btnPrintRS.setText(locale.format("publish.publish_print_result_sheets", processedRoundNumber + 1));
        this.btnPrintNPP.setText(locale.format("publish.publish_print_non_playing_players", processedRoundNumber + 1));
        this.btnPublishSt.setText(locale.format("publish.publish_standings", processedRoundNumber + 1));
        this.btnPublishML.setText(locale.format("publish.publish_team_matches", processedRoundNumber + 1));
        this.btnPublishTS.setText(locale.format("publish.publish_team_standings", processedRoundNumber + 1));

        int nbTeams = tournament.teamsList().size();
        boolean bT = false;
        if (nbTeams > 0) {
            bT = true;
        }
        Component[] tabComp = this.pnlTeams.getComponents();
        for (Component comp : tabComp) {
            comp.setEnabled(bT);
        }
    }

    private void updatePnlContents() throws RemoteException {
        DPParameterSet dpps = tournament.getTournamentParameterSet().getDPParameterSet();
        if (dpps.getPlayerSortType() == PlayerComparator.NAME_ORDER) {
            this.rdbSortByName.setSelected(true);
        } else {
            this.rdbSortByGrade.setSelected(true);
        }
        if (dpps.getGameFormat() == DPParameterSet.DP_GAME_FORMAT_FULL) {
            this.rdbGameFormatFull.setSelected(true);
        } else {
            this.rdbGameFormatShort.setSelected(true);
        }

        this.ckbShowPlayerGrade.setSelected(dpps.isShowPlayerGrade());
        this.ckbShowPlayerCountry.setSelected(dpps.isShowPlayerCountry());
        this.ckbShowPlayerClub.setSelected(dpps.isShowPlayerClub());

        this.ckbDisplayNPPlayers.setSelected(dpps.isDisplayNPPlayers());

        this.ckbDisplayNumCol.setSelected(dpps.isDisplayNumCol());
        this.ckbDisplayPlCol.setSelected(dpps.isDisplayPlCol());
        this.ckbDisplayCoCol.setSelected(dpps.isDisplayCoCol());
        this.ckbDisplayClCol.setSelected(dpps.isDisplayClCol());
        this.ckbShowByePlayer.setSelected(dpps.isShowByePlayer());
        this.ckbShowNotPairedPlayers.setSelected(dpps.isShowNotPairedPlayers());
        this.ckbShowNotParticipatingPlayers.setSelected(dpps.isShowNotParticipatingPlayers());
        this.ckbShowNotFinallyRegisteredPlayers.setSelected(dpps.isShowNotFinallyRegisteredPlayers());
        this.ckbDisplayIndGames.setSelected(dpps.isDisplayIndGamesInMatches());
    }

    private void updatePnlPar() throws RemoteException {
        TournamentParameterSet tps = tournament.getTournamentParameterSet();
        GeneralParameterSet gps = tps.getGeneralParameterSet();
        PublishParameterSet pubPS = tps.getPublishParameterSet();

        this.ckbPrint.setSelected(pubPS.isPrint());
        this.ckbExportToLocalFile.setSelected(pubPS.isExportToLocalFile());

        this.ckbHTMLAutoscroll.setSelected(pubPS.isHtmlAutoScroll());

    }

    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnExportPlayersCSV;
    private javax.swing.JButton btnExportRLAGA;
    private javax.swing.JButton btnExportRLEGF;
    private javax.swing.JButton btnExportRLFFG;
    private javax.swing.JButton btnHelp;
    private javax.swing.JButton btnPrintNPP;
    private javax.swing.JButton btnPrintRS;
    private javax.swing.JButton btnPrintTP;
    private javax.swing.JButton btnPublishGL;
    private javax.swing.JButton btnPublishML;
    private javax.swing.JButton btnPublishPL;
    private javax.swing.JButton btnPublishSt;
    private javax.swing.JButton btnPublishTL;
    private javax.swing.JButton btnPublishTS;
    private javax.swing.JCheckBox ckbDisplayClCol;
    private javax.swing.JCheckBox ckbDisplayCoCol;
    private javax.swing.JCheckBox ckbDisplayIndGames;
    private javax.swing.JCheckBox ckbDisplayNPPlayers;
    private javax.swing.JCheckBox ckbDisplayNumCol;
    private javax.swing.JCheckBox ckbDisplayPlCol;
    private javax.swing.JCheckBox ckbExportToLocalFile;
    private javax.swing.JCheckBox ckbHTMLAutoscroll;
    private javax.swing.JCheckBox ckbPrint;
    private javax.swing.JCheckBox ckbShowByePlayer;
    private javax.swing.JCheckBox ckbShowNotFinallyRegisteredPlayers;
    private javax.swing.JCheckBox ckbShowNotPairedPlayers;
    private javax.swing.JCheckBox ckbShowNotParticipatingPlayers;
    private javax.swing.JCheckBox ckbShowPlayerClub;
    private javax.swing.JCheckBox ckbShowPlayerCountry;
    private javax.swing.JCheckBox ckbShowPlayerGrade;
    private javax.swing.ButtonGroup grpGameFormat;
    private javax.swing.ButtonGroup grpRemote;
    private javax.swing.ButtonGroup grpSortType;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel pnlActions;
    private javax.swing.JPanel pnlContents;
    private javax.swing.JPanel pnlGL;
    private javax.swing.JPanel pnlML;
    private javax.swing.JPanel pnlNPP;
    private javax.swing.JPanel pnlPL;
    private javax.swing.JPanel pnlPar;
    private javax.swing.JPanel pnlPub;
    private javax.swing.JPanel pnlSt;
    private javax.swing.JPanel pnlTeams;
    private javax.swing.JRadioButton rdbGameFormatFull;
    private javax.swing.JRadioButton rdbGameFormatShort;
    private javax.swing.JRadioButton rdbSortByGrade;
    private javax.swing.JRadioButton rdbSortByName;
    private javax.swing.JRadioButton rdbSortByRank;
    private javax.swing.JSpinner spnRoundNumber;
    private javax.swing.JTabbedPane tpnPublish;
}
