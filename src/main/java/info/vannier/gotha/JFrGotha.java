/*
 * jFrGotha.java
 *
 */
package info.vannier.gotha;

import net.miginfocom.swing.MigLayout;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static ru.gofederation.gotha.model.PlayerRegistrationStatus.PRELIMINARY;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.*;

import ru.gofederation.gotha.ui.RgfTournamentExportDialog;
import ru.gofederation.gotha.ui.RgfTournamentImportDialog;
import ru.gofederation.gotha.ui.TournamentOpener;
import ru.gofederation.gotha.util.GothaLocale;

/**
 *
 * @author Luc Vannier
 */
public class JFrGotha extends javax.swing.JFrame implements TournamentOpener {

    private static final long REFRESH_DELAY = 2000;
    private long lastComponentsUpdateTime = 0;
    public static final int BIG_FRAME_WIDTH = 1000;
    public static final int BIG_FRAME_HEIGHT = 600;
    public static final int MEDIUM_FRAME_WIDTH = 796;
    public static final int MEDIUM_FRAME_HEIGHT = 553;
    public static final int SMALL_FRAME_WIDTH = 540;
    public static final int SMALL_FRAME_HEIGHT = 350;
    private static final int NUM_COL = 0;
    private static final int PL_COL = 1;
    private static final int NAME_COL = 2;
    private static final int GRADE_COL = 3;
    private static final int COUNTRY_COL = GRADE_COL + 1;
    private static final int CLUB_COL = COUNTRY_COL + 1;
    private static final int NBW_COL = CLUB_COL + 1;
    private static final int ROUND0_RESULT_COL = NBW_COL + 1;
    private static final int CRIT0_COL = ROUND0_RESULT_COL + Gotha.MAX_NUMBER_OF_ROUNDS;
    
    private static final int TEAM_PL_COL = 0;
    private static final int TEAM_NAME_COL = 1;
    private static final int TEAM_ROUND0_RESULT_COL = 2;
    private static final int TEAM_CRIT0_COL = TEAM_ROUND0_RESULT_COL + Gotha.MAX_NUMBER_OF_ROUNDS;
    // Teams Panel constants
    protected static final int TM_NUMBER_OF_COLS = 8;
    protected static final int TM_TEAM_NUMBER_COL = 0;
    protected static final int TM_TEAM_NAME_COL = 1;
    protected static final int TM_BOARD_NUMBER_COL = 2;
    protected static final int TM_PL_NAME_COL = 3;
    protected static final int TM_PL_COUNTRY_COL = 4;
    protected static final int TM_PL_CLUB_COL = 5;
    protected static final int TM_PL_RATING_COL = 6;
    protected static final int TM_PL_ROUNDS_COL = 7;
    /**
     * should stay between 0 and 9
     */
    private static final int MAX_NUMBER_OF_RECENT_TOURNAMENTS = 6;
    private int displayedRoundNumber = 0;
    private boolean bDisplayTemporaryParameterSet = false;
    private PlacementCriterion[] displayedCriteria = new PlacementCriterion[PlacementParameterSet.PLA_MAX_NUMBER_OF_CRITERIA];
    private int displayedTeamRoundNumber = 0;
    private boolean bDisplayTemporaryTeamParameterSet = false;
    private TeamPlacementCriterion[] displayedTeamCriteria = new TeamPlacementCriterion[TeamPlacementParameterSet.TPL_MAX_NUMBER_OF_CRITERIA];
    /**
     * current Tournament
     */
    private TournamentInterface tournament = null;
    private long lastDisplayedStandingsUpdateTime = 0;
    private long lastDisplayedTeamsStandingsUpdateTime = 0;
    private ControlPanelTableCellRenderer cpTableCellRenderer = new ControlPanelTableCellRenderer();
    private TeamsPanelTableCellRenderer tpTableCellRenderer = new TeamsPanelTableCellRenderer();

	private GothaLocale locale;

    /**
     * Creates new form jFrGotha
     * @param tournament
     * @throws java.rmi.RemoteException
     */
    public JFrGotha(TournamentInterface tournament) throws RemoteException {
		this.locale = GothaLocale.getCurrentLocale();

        this.tournament = tournament;

        initComponents();

        if (Gotha.runningMode == Gotha.RUNNING_MODE_SAL || Gotha.runningMode == Gotha.RUNNING_MODE_SRV) {
            ArrayList<String> alRT = getRecentTournamentsList();
            if (alRT.size() >= 1) {
                File f = new File(alRT.get(0));
                if (f.canRead()) {
                    try {
                        openTournament(f);
                    } catch (Exception ex) {
                        System.out.println("Problem opening file : " + f.getName());
                    }
                } else {
                    System.out.println("" + f.getName() + " cannot be read");
                }
            }
        }
        customInitComponents();
        setVisible(true);

        setupRefreshTimer();
    }

    private void setupRefreshTimer() {
        ActionListener taskPerformer = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (tournament == null) {
                    return;
                }
                try {
                    boolean b = tournament.clockIn(Gotha.clientName);
                    if (!b && Gotha.runningMode == Gotha.RUNNING_MODE_CLI) {
                        JOptionPane.showMessageDialog(null, locale.getString("error.client_connection_tournament_reset"),
                                locale.getString("alert.warning"), JOptionPane.ERROR_MESSAGE);
                        exitOpenGotha();

                    }
                    if (tournament.getLastTournamentModificationTime() > lastComponentsUpdateTime) {
                        updateAllViews();
                        // save tournament to work file
                       saveTournamentToAWorkFile();
                    }
                } catch (RemoteException ex) {
                    JOptionPane.showMessageDialog(null, locale.getString("error.client_connection_reset"),
                            locale.getString("alert.warning"), JOptionPane.ERROR_MESSAGE);
                    exitOpenGotha();
                }
            }
        };
        new javax.swing.Timer((int) REFRESH_DELAY, taskPerformer).start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    private void initComponents() {

        grpSystem = new javax.swing.ButtonGroup();
        dlgNew = new javax.swing.JDialog();
        pnlSystem = new javax.swing.JPanel();
        rdbMcMahon = new javax.swing.JRadioButton();
        rdbSwiss = new javax.swing.JRadioButton();
        rdbSwissCat = new javax.swing.JRadioButton();
        jLabel13 = new javax.swing.JLabel();
        txfNumberOfRounds = new javax.swing.JTextField();
        lblRecommended = new javax.swing.JLabel();
        pnlTournamentDetails = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        txfShortName = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txfName = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txfLocation = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txfBeginDate = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        txfEndDate = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        txfDirector = new javax.swing.JTextField();
        btnDlgNewOK = new javax.swing.JButton();
        btnDlgNewCancel = new javax.swing.JButton();
        btnHelp = new javax.swing.JButton();
        grpPS = new javax.swing.ButtonGroup();
        dlgImportXML = new javax.swing.JDialog();
        btnDlgImportXMLOK = new javax.swing.JButton();
        btnDlgImportXMLCancel = new javax.swing.JButton();
        pnlObjectsToImport = new javax.swing.JPanel();
        chkPlayers = new javax.swing.JCheckBox();
        chkGames = new javax.swing.JCheckBox();
        chkTournamentParameters = new javax.swing.JCheckBox();
        chkTeams = new javax.swing.JCheckBox();
        chkClubsGroups = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        grpTeamPS = new javax.swing.ButtonGroup();
        tpnGotha = new javax.swing.JTabbedPane();
        pnlWelcome = new javax.swing.JPanel();
        lblTournamentPicture = new javax.swing.JLabel();
        lblFlowChart = new javax.swing.JLabel();
        pnlControlPanel = new javax.swing.JPanel();
        scpControlPanel = new javax.swing.JScrollPane();
        tblControlPanel = new javax.swing.JTable();
        lblWarningPRE = new javax.swing.JLabel();
        pnlStandings = new javax.swing.JPanel();
        lblStandingsAfter = new javax.swing.JLabel();
        pnlPS = new javax.swing.JPanel();
        rdbCurrentPS = new javax.swing.JRadioButton();
        rdbTemporaryPS = new javax.swing.JRadioButton();
        cbxCrit1 = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        cbxCrit2 = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        cbxCrit3 = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        cbxCrit4 = new javax.swing.JComboBox<>();
        scpStandings = new javax.swing.JScrollPane();
        tblStandings = new javax.swing.JTable();
        btnPrintStandings = new javax.swing.JButton();
        lblUpdateTime = new javax.swing.JLabel();
        spnRoundNumber = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        txfSearchPlayer = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        pnlTeamsPanel = new javax.swing.JPanel();
        scpTeamsPanel = new javax.swing.JScrollPane();
        tblTeamsPanel = new javax.swing.JTable();
        pnlTeamsStandings = new javax.swing.JPanel();
        lblTeamsStandingsAfter = new javax.swing.JLabel();
        pnlTeamPS = new javax.swing.JPanel();
        rdbCurrentTeamPS = new javax.swing.JRadioButton();
        rdbTemporaryTeamPS = new javax.swing.JRadioButton();
        jLabel9 = new javax.swing.JLabel();
        cbxTeamCrit1 = new javax.swing.JComboBox<>();
        jLabel14 = new javax.swing.JLabel();
        cbxTeamCrit2 = new javax.swing.JComboBox<>();
        jLabel15 = new javax.swing.JLabel();
        cbxTeamCrit3 = new javax.swing.JComboBox<>();
        jLabel16 = new javax.swing.JLabel();
        cbxTeamCrit4 = new javax.swing.JComboBox<>();
        jLabel17 = new javax.swing.JLabel();
        cbxTeamCrit5 = new javax.swing.JComboBox<>();
        jLabel18 = new javax.swing.JLabel();
        cbxTeamCrit6 = new javax.swing.JComboBox<>();
        scpTeamsStandings = new javax.swing.JScrollPane();
        tblTeamsStandings = new javax.swing.JTable();
        lblTeamUpdateTime = new javax.swing.JLabel();
        spnTeamRoundNumber = new javax.swing.JSpinner();
        btnPrintTeamsStandings = new javax.swing.JButton();
        mnuMain = new javax.swing.JMenuBar();
        mnuTournament = new javax.swing.JMenu();
        mniNew = new javax.swing.JMenuItem();
        mniOpen = new javax.swing.JMenuItem();
        mnuOpenRecent = new javax.swing.JMenu();
        mniSave = new javax.swing.JMenuItem();
        mniSaveAs = new javax.swing.JMenuItem();
        mniSaveACopy = new javax.swing.JMenuItem();
        mniClose = new javax.swing.JMenuItem();
        mnuImport = new javax.swing.JMenu();
        mniImportH9 = new javax.swing.JMenuItem();
        mniImportTou = new javax.swing.JMenuItem();
        mniImportWallist = new javax.swing.JMenuItem();
        mniImportVBS = new javax.swing.JMenuItem();
        mniImportXML = new javax.swing.JMenuItem();
        mniImportRgf = new javax.swing.JMenuItem();
        mniExport = new javax.swing.JMenuItem();
        mniExit = new javax.swing.JMenuItem();
        mniBuildTestTournament = new javax.swing.JMenuItem();
        mnuPlayers = new javax.swing.JMenu();
        mniPlayersManager = new javax.swing.JMenuItem();
        mniPlayersQuickCheck = new javax.swing.JMenuItem();
        mniUpdateRatings = new javax.swing.JMenuItem();
        mniMMGroups = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        mniTeamsManager = new javax.swing.JMenuItem();
        mnuGames = new javax.swing.JMenu();
        mniPair = new javax.swing.JMenuItem();
        mniResults = new javax.swing.JMenuItem();
        mniRR = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        mniTeamsPairing = new javax.swing.JMenuItem();
        mnuPublish = new javax.swing.JMenu();
        mniPublish = new javax.swing.JMenuItem();
        mniPublishRGF = new javax.swing.JMenuItem();
        mnuOptions = new javax.swing.JMenu();
        mniTournamentOptions = new javax.swing.JMenuItem();
        mniGamesOptions = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        mniPreferences = new javax.swing.JMenuItem();
        mnuTools = new javax.swing.JMenu();
        mniDiscardRounds = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        mniRMI = new javax.swing.JMenuItem();
        mniMemory = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        mniExperimentalTools = new javax.swing.JMenuItem();
        mnuHelp = new javax.swing.JMenu();
        mniOpenGothaHelp = new javax.swing.JMenuItem();
        mniHelpAbout = new javax.swing.JMenuItem();

        dlgNew.getContentPane().setLayout(null);

        pnlSystem.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("tournament.system")));
        pnlSystem.setLayout(null);

        grpPS.add(rdbMcMahon);
        rdbMcMahon.setFont(new java.awt.Font("Tahoma", 0, 10));
        rdbMcMahon.setText(locale.getString("tournament.system.mcmahon"));
        rdbMcMahon.setToolTipText(locale.getString("tournament.system.mcmahon_tooltip"));
        rdbMcMahon.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rdbMcMahon.setMargin(new java.awt.Insets(0, 0, 0, 0));
        pnlSystem.add(rdbMcMahon);
        rdbMcMahon.setBounds(20, 30, 170, 13);

        grpPS.add(rdbSwiss);
        rdbSwiss.setFont(new java.awt.Font("Tahoma", 0, 10));
        rdbSwiss.setText(locale.getString("tournament.system.swiss"));
        rdbSwiss.setToolTipText(locale.getString("tournament.system.swiss_tooltip"));
        rdbSwiss.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rdbSwiss.setMargin(new java.awt.Insets(0, 0, 0, 0));
        pnlSystem.add(rdbSwiss);
        rdbSwiss.setBounds(20, 60, 170, 13);

        grpPS.add(rdbSwissCat);
        rdbSwissCat.setFont(new java.awt.Font("Tahoma", 0, 10));
        rdbSwissCat.setText(locale.getString("tournament.system.swiss_cat"));
        rdbSwissCat.setToolTipText(locale.getString("tournament.system.swiss_cat_tooltip"));
        rdbSwissCat.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rdbSwissCat.setMargin(new java.awt.Insets(0, 0, 0, 0));
        pnlSystem.add(rdbSwissCat);
        rdbSwissCat.setBounds(20, 90, 170, 13);

        jLabel13.setFont(new java.awt.Font("Tahoma", 0, 10));
        jLabel13.setText(locale.getString("tournament.number_of_rounds"));
        pnlSystem.add(jLabel13);
        jLabel13.setBounds(20, 140, 120, 13);

        txfNumberOfRounds.setText("0");
        pnlSystem.add(txfNumberOfRounds);
        txfNumberOfRounds.setBounds(160, 140, 30, 20);

        lblRecommended.setFont(new java.awt.Font("Tahoma", 0, 10));
        lblRecommended.setText(locale.getString("tournament.system.swiss_cat_recommended"));
        pnlSystem.add(lblRecommended);
        lblRecommended.setBounds(30, 100, 240, 13);

        dlgNew.getContentPane().add(pnlSystem);
        pnlSystem.setBounds(410, 10, 300, 220);

        pnlTournamentDetails.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("tournament.details")));
        pnlTournamentDetails.setLayout(null);

        jLabel8.setText(locale.getString("tournament.short_name"));
        pnlTournamentDetails.add(jLabel8);
        jLabel8.setBounds(10, 60, 80, 14);

        txfShortName.setText("tournamentshortname");
        txfShortName.setToolTipText(locale.getString("tournament.short_name.tooltip"));
        pnlTournamentDetails.add(txfShortName);
        txfShortName.setBounds(100, 60, 180, 20);

        jLabel10.setText(locale.getString("tournament.name"));
        pnlTournamentDetails.add(jLabel10);
        jLabel10.setBounds(10, 30, 80, 14);

        txfName.setText("Tournament name");
        txfName.setToolTipText("tournament name as shown in headers and titles");
        pnlTournamentDetails.add(txfName);
        txfName.setBounds(100, 30, 180, 20);

        jLabel11.setText(locale.getString("tournament.location"));
        pnlTournamentDetails.add(jLabel11);
        jLabel11.setBounds(10, 90, 80, 14);

        txfLocation.setText("Location name");
        pnlTournamentDetails.add(txfLocation);
        txfLocation.setBounds(100, 90, 180, 20);

        jLabel12.setText(locale.getString("tournament.begin_date"));
        pnlTournamentDetails.add(jLabel12);
        jLabel12.setBounds(10, 160, 80, 14);

        txfBeginDate.setText("yyyy-mm-dd");
        pnlTournamentDetails.add(txfBeginDate);
        txfBeginDate.setBounds(100, 160, 110, 20);

        jLabel19.setText(locale.getString("tournament.end_date"));
        pnlTournamentDetails.add(jLabel19);
        jLabel19.setBounds(10, 180, 80, 14);

        txfEndDate.setText("yyyy-mm-dd");
        pnlTournamentDetails.add(txfEndDate);
        txfEndDate.setBounds(100, 180, 110, 20);

        jLabel20.setText(locale.getString("tournament.director"));
        pnlTournamentDetails.add(jLabel20);
        jLabel20.setBounds(10, 120, 80, 14);

        txfDirector.setText("Director name");
        pnlTournamentDetails.add(txfDirector);
        txfDirector.setBounds(100, 120, 180, 20);

        dlgNew.getContentPane().add(pnlTournamentDetails);
        pnlTournamentDetails.setBounds(100, 10, 300, 220);

        btnDlgNewOK.setText(locale.getString("btn.ok"));
        btnDlgNewOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDlgNewOKActionPerformed(evt);
            }
        });
        dlgNew.getContentPane().add(btnDlgNewOK);
        btnDlgNewOK.setBounds(250, 260, 290, 30);

        btnDlgNewCancel.setText(locale.getString("btn.cancel"));
        btnDlgNewCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDlgNewCancelActionPerformed(evt);
            }
        });
        dlgNew.getContentPane().add(btnDlgNewCancel);
        btnDlgNewCancel.setBounds(560, 260, 130, 30);

        btnHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/info/vannier/gotha/gothalogo16.jpg")));
        btnHelp.setText(locale.getString("btn.help"));
        btnHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHelpActionPerformed(evt);
            }
        });
        dlgNew.getContentPane().add(btnHelp);
        btnHelp.setBounds(100, 260, 130, 30);

        dlgImportXML.getContentPane().setLayout(null);

        btnDlgImportXMLOK.setText("OK");
        btnDlgImportXMLOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDlgImportXMLOKActionPerformed(evt);
            }
        });
        dlgImportXML.getContentPane().add(btnDlgImportXMLOK);
        btnDlgImportXMLOK.setBounds(130, 280, 120, 23);

        btnDlgImportXMLCancel.setText("Cancel");
        btnDlgImportXMLCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDlgImportXMLCancelActionPerformed(evt);
            }
        });
        dlgImportXML.getContentPane().add(btnDlgImportXMLCancel);
        btnDlgImportXMLCancel.setBounds(290, 280, 120, 23);

        pnlObjectsToImport.setBorder(javax.swing.BorderFactory.createTitledBorder("Objects to Import"));
        pnlObjectsToImport.setLayout(null);

        chkPlayers.setFont(new java.awt.Font("Tahoma", 0, 10));
        chkPlayers.setSelected(true);
        chkPlayers.setText("Players");
        pnlObjectsToImport.add(chkPlayers);
        chkPlayers.setBounds(20, 20, 190, 21);

        chkGames.setFont(new java.awt.Font("Tahoma", 0, 10));
        chkGames.setSelected(true);
        chkGames.setText("Games");
        pnlObjectsToImport.add(chkGames);
        chkGames.setBounds(20, 50, 190, 21);

        chkTournamentParameters.setFont(new java.awt.Font("Tahoma", 0, 10));
        chkTournamentParameters.setText("Tournament Parameters");
        pnlObjectsToImport.add(chkTournamentParameters);
        chkTournamentParameters.setBounds(20, 80, 190, 21);

        chkTeams.setFont(new java.awt.Font("Arial", 0, 11));
        chkTeams.setText("Teams and team parameters");
        pnlObjectsToImport.add(chkTeams);
        chkTeams.setBounds(20, 110, 190, 23);

        chkClubsGroups.setFont(new java.awt.Font("Arial", 0, 11));
        chkClubsGroups.setSelected(true);
        chkClubsGroups.setText("Clubs Groups");
        pnlObjectsToImport.add(chkClubsGroups);
        chkClubsGroups.setBounds(20, 140, 190, 23);

        dlgImportXML.getContentPane().add(pnlObjectsToImport);
        pnlObjectsToImport.setBounds(140, 40, 260, 180);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Importation will merge information from xml file with information in current tournament.");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        dlgImportXML.getContentPane().add(jLabel1);
        jLabel1.setBounds(10, 230, 520, 14);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Before proceeding, make sure to have a good backup of your current tournament.");
        jLabel2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        dlgImportXML.getContentPane().add(jLabel2);
        jLabel2.setBounds(10, 250, 520, 14);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Gotha");
        setIconImage(getIconImage());
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new MigLayout());

        tpnGotha.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tpnGothaStateChanged(evt);
            }
        });

        //////// Welcome

        pnlWelcome.setLayout(new MigLayout("flowy", "push[align center]push", "push[]unrel[]push"));

        lblTournamentPicture.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTournamentPicture.setIcon(new javax.swing.ImageIcon(getClass().getResource("/info/vannier/gotha/claira242X311.png")));
        pnlWelcome.add(lblTournamentPicture);

        lblFlowChart.setBackground(new java.awt.Color(255, 255, 255));
        lblFlowChart.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblFlowChart.setIcon(new javax.swing.ImageIcon(getClass().getResource("/info/vannier/gotha/flowchart.jpg")));
        pnlWelcome.add(lblFlowChart);

        tpnGotha.addTab(locale.getString("welcome"), pnlWelcome);

        //////// Control panel

        pnlControlPanel.setLayout(new MigLayout("flowy", "push[align center]push", "30lp:push[]30lp[]30lp:push"));

        tblControlPanel.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "Round", "Participants", "Assigned players", "Entered results"
            }
        ));
        tblControlPanel.setEnabled(false);
        tblControlPanel.setRowSelectionAllowed(false);
        scpControlPanel.setViewportView(tblControlPanel);

        pnlControlPanel.add(scpControlPanel);

        lblWarningPRE.setForeground(new java.awt.Color(255, 0, 0));
        pnlControlPanel.add(lblWarningPRE);

        tpnGotha.addTab(locale.getString("control_panel"), pnlControlPanel);

        //////// Standings

        pnlStandings.setLayout(new MigLayout("flowy", "[]unrel[]", "[]unrel:push[]unrel:push[]unrel:push[][][]unrel:push[]"));

        lblStandingsAfter.setText(locale.getString("standings.after_round"));
        pnlStandings.add(lblStandingsAfter, "split 2, flowx");

        spnRoundNumber.addChangeListener(this::spnRoundNumberStateChanged);
        pnlStandings.add(spnRoundNumber, "wmin 40lp, hmin 30lp, gapleft push");

        pnlPS.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), locale.getString("standings.pps")));
        pnlPS.setLayout(new MigLayout());

        Font psFont = rdbCurrentPS.getFont().deriveFont(rdbCurrentPS.getFont().getSize() * 0.85f);

        grpPS.add(rdbCurrentPS);
        rdbCurrentPS.setFont(psFont);
        rdbCurrentPS.setText(locale.getString("standings.pps.current"));
        rdbCurrentPS.addActionListener(this::rdbCurrentPSActionPerformed);
        pnlPS.add(rdbCurrentPS, "span, wrap 0");

        grpPS.add(rdbTemporaryPS);
        rdbTemporaryPS.setFont(psFont);
        rdbTemporaryPS.setText(locale.getString("standings.pps.temporary"));
        rdbTemporaryPS.addActionListener(this::rdbTemporaryPSActionPerformed);
        pnlPS.add(rdbTemporaryPS, "span, wrap unrel");

        jLabel3.setFont(psFont);
        jLabel3.setText(locale.getString("standings.pps.crit1"));
        pnlPS.add(jLabel3);

        cbxCrit1.setEnabled(false);
        cbxCrit1.addActionListener(this::cbxCritActionPerformed);
        pnlPS.add(cbxCrit1, "wrap");

        jLabel4.setFont(psFont);
        jLabel4.setText(locale.getString("standings.pps.crit2"));
        pnlPS.add(jLabel4);

        cbxCrit2.setEnabled(false);
        cbxCrit2.addActionListener(this::cbxCritActionPerformed);
        pnlPS.add(cbxCrit2, "wrap");

        jLabel5.setFont(psFont);
        jLabel5.setText(locale.getString("standings.pps.crit3"));
        pnlPS.add(jLabel5);

        cbxCrit3.setEnabled(false);
        cbxCrit3.addActionListener(this::cbxCritActionPerformed);
        pnlPS.add(cbxCrit3, "wrap");

        jLabel6.setFont(psFont);
        jLabel6.setText(locale.getString("standings.pps.crit4"));
        pnlPS.add(jLabel6);

        cbxCrit4.setEnabled(false);
        cbxCrit4.addActionListener(this::cbxCritActionPerformed);
        pnlPS.add(cbxCrit4, "wrap");

        pnlStandings.add(pnlPS, "sgx lp");

        lblUpdateTime.setText(locale.getString("standings.update_time"));
        pnlStandings.add(lblUpdateTime, "sgx lp");

        jLabel7.setText(locale.getString("player.search"));
        pnlStandings.add(jLabel7);
        pnlStandings.add(txfSearchPlayer, "sgx lp");

        btnSearch.setText(locale.getString("player.btn_search"));
        btnSearch.addActionListener(this::btnSearchActionPerformed);
        pnlStandings.add(btnSearch, "sgx lp");

        btnPrintStandings.setText(locale.getString("btn.print"));
        btnPrintStandings.addActionListener(this::btnPrintStandingsActionPerformed);
        pnlStandings.add(btnPrintStandings, "sgx lp, wrap");

        tblStandings.setEnabled(false);
        tblStandings.setRowSelectionAllowed(false);
        scpStandings.setViewportView(tblStandings);

        pnlStandings.add(scpStandings, "grow, push, spany");

        tpnGotha.addTab(locale.getString("standings"), pnlStandings);

        //////// Teams

        pnlTeamsPanel.setLayout(new MigLayout(null, "push[]push", "[push, grow]"));

        tblTeamsPanel.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] { "Nr", "Team name", "Board", "Player name", "Co", "Club", "Rating", "Rounds" }
        ));
        tblTeamsPanel.setEnabled(false);
        tblTeamsPanel.setRowSelectionAllowed(false);
        scpTeamsPanel.setViewportView(tblTeamsPanel);

        pnlTeamsPanel.add(scpTeamsPanel, "grow");

        tpnGotha.addTab(locale.getString("teams_panel"), pnlTeamsPanel);

        //////// Teams standings

        pnlTeamsStandings.setLayout(new MigLayout("flowy", "[]unrel[]", "[]unrel:push[]unrel:push[]unrel:push[]"));

        lblTeamsStandingsAfter.setText(locale.getString("standings.after_round"));
        pnlTeamsStandings.add(lblTeamsStandingsAfter, "split 2, flowx");

        spnTeamRoundNumber.addChangeListener(this::spnTeamRoundNumberStateChanged);
        pnlTeamsStandings.add(spnTeamRoundNumber, "wmin 40lp, hmin 30lp, gapleft push");

        pnlTeamPS.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), locale.getString("standings.tpps")));
        pnlTeamPS.setLayout(new MigLayout());

        grpTeamPS.add(rdbCurrentTeamPS);
        rdbCurrentTeamPS.setFont(psFont);
        rdbCurrentTeamPS.setText(locale.getString("standings.pps.current"));
        rdbCurrentTeamPS.addActionListener(this::rdbCurrentTeamPSActionPerformed);
        pnlTeamPS.add(rdbCurrentTeamPS, "span, wrap 0");

        grpTeamPS.add(rdbTemporaryTeamPS);
        rdbTemporaryTeamPS.setFont(psFont);
        rdbTemporaryTeamPS.setText(locale.getString("standings.pps.temporary"));
        rdbTemporaryTeamPS.addActionListener(this::rdbTemporaryTeamPSActionPerformed);
        pnlTeamPS.add(rdbTemporaryTeamPS, "span, wrap unrel");

        jLabel9.setFont(psFont);
        jLabel9.setText(locale.getString("standings.pps.crit1"));
        pnlTeamPS.add(jLabel9);

        cbxTeamCrit1.setEnabled(false);
        cbxTeamCrit1.addActionListener(this::cbxTeamCritActionPerformed);
        pnlTeamPS.add(cbxTeamCrit1, "wrap");

        jLabel14.setFont(psFont);
        jLabel14.setText(locale.getString("standings.pps.crit2"));
        pnlTeamPS.add(jLabel14);

        cbxTeamCrit2.setEnabled(false);
        cbxTeamCrit2.addActionListener(this::cbxTeamCritActionPerformed);
        pnlTeamPS.add(cbxTeamCrit2, "wrap");

        jLabel15.setFont(psFont);
        jLabel15.setText(locale.getString("standings.pps.crit3"));
        pnlTeamPS.add(jLabel15);

        cbxTeamCrit3.setEnabled(false);
        cbxTeamCrit3.addActionListener(this::cbxTeamCritActionPerformed);
        pnlTeamPS.add(cbxTeamCrit3, "wrap");

        jLabel16.setFont(psFont);
        jLabel16.setText(locale.getString("standings.pps.crit4"));
        pnlTeamPS.add(jLabel16);

        cbxTeamCrit4.setEnabled(false);
        cbxTeamCrit4.addActionListener(this::cbxTeamCritActionPerformed);
        pnlTeamPS.add(cbxTeamCrit4, "wrap");

        jLabel17.setFont(psFont);
        jLabel17.setText(locale.getString("standings.pps.crit5"));
        pnlTeamPS.add(jLabel17);

        cbxTeamCrit5.setEnabled(false);
        cbxTeamCrit5.addActionListener(this::cbxTeamCritActionPerformed);
        pnlTeamPS.add(cbxTeamCrit5, "wrap");

        jLabel18.setFont(psFont);
        jLabel18.setText(locale.getString("standings.pps.crit6"));
        pnlTeamPS.add(jLabel18);

        cbxTeamCrit6.setEnabled(false);
        cbxTeamCrit6.addActionListener(this::cbxTeamCritActionPerformed);
        pnlTeamPS.add(cbxTeamCrit6);

        pnlTeamsStandings.add(pnlTeamPS, "sgx lp");

        lblTeamUpdateTime.setText(locale.getString("standings.update_time"));
        pnlTeamsStandings.add(lblTeamUpdateTime, "sgx lp");

        btnPrintTeamsStandings.setText(locale.getString("btn.print"));
        btnPrintTeamsStandings.addActionListener(this::btnPrintTeamsStandingsActionPerformed);
        pnlTeamsStandings.add(btnPrintTeamsStandings, "sgx lp, wrap");

        tblTeamsStandings.setEnabled(false);
        tblTeamsStandings.setRowSelectionAllowed(false);
        scpTeamsStandings.setViewportView(tblTeamsStandings);

        pnlTeamsStandings.add(scpTeamsStandings, "grow, push, spany");

        tpnGotha.addTab(locale.getString("team_standings"), pnlTeamsStandings);

        getContentPane().add(tpnGotha, "dock center");

        //////// Menu

        mnuTournament.setText(locale.getString("menu.tournament"));

        mniNew.setAccelerator(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        mniNew.setText(locale.getString("menu.tournament.new"));
        mniNew.addActionListener(this::mniNewActionPerformed);
        mnuTournament.add(mniNew);

        mniOpen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        mniOpen.setText(locale.getString("menu.tournament.open"));
        mniOpen.addActionListener(this::mniOpenActionPerformed);
        mnuTournament.add(mniOpen);

        mnuOpenRecent.setText(locale.getString("menu.tournament.recent"));
        mnuTournament.add(mnuOpenRecent);

        mniSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        mniSave.setText(locale.getString("menu.tournament.save"));
        mniSave.addActionListener(this::mniSaveActionPerformed);
        mnuTournament.add(mniSave);

        mniSaveAs.setAccelerator(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
        mniSaveAs.setText(locale.getString("menu.tournament.save_as"));
        mniSaveAs.addActionListener(this::mniSaveAsActionPerformed);
        mnuTournament.add(mniSaveAs);

        mniSaveACopy.setText(locale.getString("menu.tournament.save_copy"));
        mniSaveACopy.addActionListener(this::mniSaveACopyActionPerformed);
        mnuTournament.add(mniSaveACopy);

        mniClose.setText(locale.getString("menu.tournament.close"));
        mniClose.addActionListener(this::mniCloseActionPerformed);
        mnuTournament.add(mniClose);
        mnuTournament.add(new JSeparator());

        mnuImport.setText(locale.getString("menu.tournament.import"));

        mniImportH9.setText(locale.getString("menu.tournament.import.h9"));
        mniImportH9.addActionListener(this::mniImportH9ActionPerformed);
        mnuImport.add(mniImportH9);

        mniImportTou.setText(locale.getString("menu.tournament.import.tou"));
        mniImportTou.addActionListener(this::mniImportTouActionPerformed);
        mnuImport.add(mniImportTou);

        mniImportWallist.setText(locale.getString("menu.tournament.import.wallist"));
        mniImportWallist.addActionListener(this::mniImportWallistActionPerformed);
        mnuImport.add(mniImportWallist);

        mniImportVBS.setText(locale.getString("menu.tournament.import.vbar"));
        mniImportVBS.addActionListener(this::mniImportVBSActionPerformed);
        mnuImport.add(mniImportVBS);

        mniImportXML.setText(locale.getString("menu.tournament.import.xml"));
        mniImportXML.addActionListener(this::mniImportXMLActionPerformed);
        mnuImport.add(mniImportXML);

        mniImportRgf.setText(locale.getString("menu.tournament.import.rgf"));
        mniImportRgf.addActionListener(this::mniImportRgfActionPerformed);
        mnuImport.add(mniImportRgf);

        mnuTournament.add(mnuImport);

        mniExport.setText(locale.getString("menu.tournament.export"));
        mniExport.addActionListener(this::mniExportActionPerformed);
        mnuTournament.add(mniExport);
        mnuTournament.add(new JSeparator());

        mniExit.setText(locale.getString("menu.tournament.exit"));
        mniExit.addActionListener(this::mniExitActionPerformed);
        mnuTournament.add(mniExit);
        mnuTournament.add(new JSeparator());

        mniBuildTestTournament.setText(locale.getString("menu.tournament.build_test"));
        mniBuildTestTournament.addActionListener(this::mniBuildTestTournamentActionPerformed);
        mnuTournament.add(mniBuildTestTournament);

        mnuMain.add(mnuTournament);

        mnuPlayers.setText(locale.getString("menu.players"));

        mniPlayersManager.setAccelerator(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK));
        mniPlayersManager.setText(locale.getString("menu.players.manager"));
        mniPlayersManager.addActionListener(this::mniPlayersManagerActionPerformed);
        mnuPlayers.add(mniPlayersManager);

        mniPlayersQuickCheck.setAccelerator(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        mniPlayersQuickCheck.setText(locale.getString("menu.players.check"));
        mniPlayersQuickCheck.addActionListener(this::mniPlayersQuickCheckActionPerformed);
        mnuPlayers.add(mniPlayersQuickCheck);

        mniUpdateRatings.setText(locale.getString("menu.players.update_ratings"));
        mniUpdateRatings.addActionListener(this::mniUpdateRatingsActionPerformed);
        mnuPlayers.add(mniUpdateRatings);

        mniMMGroups.setText(locale.getString("menu.players.mcmahon_groups"));
        mniMMGroups.addActionListener(this::mniMMGroupsActionPerformed);
        mnuPlayers.add(mniMMGroups);
        mnuPlayers.add(jSeparator5);

        mniTeamsManager.setText(locale.getString("menu.players.teams_manager"));
        mniTeamsManager.addActionListener(this::mniTeamsManagerActionPerformed);
        mnuPlayers.add(mniTeamsManager);

        mnuMain.add(mnuPlayers);

        mnuGames.setText(locale.getString("menu.games"));

        mniPair.setAccelerator(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
        mniPair.setText(locale.getString("menu.games.pair"));
        mniPair.addActionListener(this::mniPairActionPerformed);
        mnuGames.add(mniPair);

        mniResults.setAccelerator(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        mniResults.setText(locale.getString("menu.games.results"));
        mniResults.addActionListener(this::mniResultsActionPerformed);
        mnuGames.add(mniResults);

        mniRR.setText(locale.getString("menu.games.round_robin"));
        mniRR.addActionListener(this::mniRRActionPerformed);
        mnuGames.add(mniRR);
        mnuGames.add(jSeparator6);

        mniTeamsPairing.setText(locale.getString("menu.games.teams_pairing"));
        mniTeamsPairing.addActionListener(this::mniTeamsPairingActionPerformed);
        mnuGames.add(mniTeamsPairing);

        mnuMain.add(mnuGames);

        mnuPublish.setText(locale.getString("menu.publish"));

        mniPublish.setText(locale.getString("menu.publish.publish"));
        mniPublish.addActionListener(this::mniPublishActionPerformed);
        mnuPublish.add(mniPublish);

        mniPublishRGF.setText(locale.getString("menu.publish.publish_rgf"));
        mniPublishRGF.addActionListener(this::mniPublishRGFActionPerformed);
        mnuPublish.add(mniPublishRGF);

        mnuMain.add(mnuPublish);

        mnuOptions.setText(locale.getString("menu.options"));

        mniTournamentOptions.setText(locale.getString("menu.options.tournament"));
        mniTournamentOptions.addActionListener(this::mniTournamentOptionsActionPerformed);
        mnuOptions.add(mniTournamentOptions);

        mniGamesOptions.setText(locale.getString("menu.options.games"));
        mniGamesOptions.addActionListener(this::mniGamesOptionsActionPerformed);
        mnuOptions.add(mniGamesOptions);
        mnuOptions.add(jSeparator7);

        mniPreferences.setText(locale.getString("menu.options.preferences"));
        mniPreferences.addActionListener(this::mniPreferencesActionPerformed);
        mnuOptions.add(mniPreferences);

        mnuMain.add(mnuOptions);

        mnuTools.setText(locale.getString("menu.tools"));

        mniDiscardRounds.setText(locale.getString("menu.tools.discard_rounds"));
        mniDiscardRounds.addActionListener(this::mniDiscardRoundsActionPerformed);
        mnuTools.add(mniDiscardRounds);
        mnuTools.add(jSeparator3);

        mniRMI.setText(locale.getString("menu.tools.rmi"));
        mniRMI.addActionListener(this::mniRMIActionPerformed);
        mnuTools.add(mniRMI);

        mniMemory.setText(locale.getString("menu.tools.memory"));
        mniMemory.addActionListener(this::mniMemoryActionPerformed);
        mnuTools.add(mniMemory);
        mnuTools.add(jSeparator8);

        mniExperimentalTools.setText(locale.getString("menu.tools.experimental"));
        mniExperimentalTools.addActionListener(this::mniExperimentalToolsActionPerformed);
        mnuTools.add(mniExperimentalTools);

        mnuMain.add(mnuTools);

        mnuHelp.setText(locale.getString("menu.help"));

        mniOpenGothaHelp.setAccelerator(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK));
        mniOpenGothaHelp.setText(locale.getString("menu.help.help"));
        mniOpenGothaHelp.addActionListener(this::mniOpenGothaHelpActionPerformed);
        mnuHelp.add(mniOpenGothaHelp);

        mniHelpAbout.setText(locale.getString("menu.help.about"));
        mniHelpAbout.addActionListener(this::mniHelpAboutActionPerformed);
        mnuHelp.add(mniHelpAbout);

        mnuMain.add(mnuHelp);

        setJMenuBar(mnuMain);

        pack();
    }

    private void mniGamesOptionsActionPerformed(java.awt.event.ActionEvent evt) {
        if (tournament == null) {
            JOptionPane.showMessageDialog(this, locale.getString("error.no_open_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            JFrame jfr = new JFrGamesOptions(tournament);
            this.displayFrame(jfr, MEDIUM_FRAME_WIDTH, MEDIUM_FRAME_HEIGHT);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void btnPrintStandingsActionPerformed(java.awt.event.ActionEvent evt) {
        TournamentParameterSet tps = null;
        try {
            tps = tournament.getTournamentParameterSet();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
        TournamentParameterSet printTPS = new TournamentParameterSet(tps);
        PlacementParameterSet printPPS = printTPS.getPlacementParameterSet();
        printPPS.setPlaCriteria(displayedCriteria);
        TournamentPrinting.printStandings(tournament, printTPS, this.displayedRoundNumber);
    }

    private void mniMMGroupsActionPerformed(java.awt.event.ActionEvent evt) {
        if (tournament == null) {
            JOptionPane.showMessageDialog(this, locale.getString("error.no_open_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            if (tournament.tournamentType() != TournamentParameterSet.TYPE_MCMAHON) {
                JOptionPane.showMessageDialog(this, locale.getString("error.mcmahon_groups_only_relevant_to_mcmahon_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
                return;

            }
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            JFrame jfr = new JFrPlayersMMG(tournament);
            this.displayFrame(jfr, BIG_FRAME_WIDTH, BIG_FRAME_HEIGHT);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void mniBuildTestTournamentActionPerformed(java.awt.event.ActionEvent evt) {
        if (tournament == null) {
            JOptionPane.showMessageDialog(this, locale.getString("error.no_open_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            JFrame jfr = new JFrBuildTestTournament(tournament);
            this.displayFrame(jfr, MEDIUM_FRAME_WIDTH, MEDIUM_FRAME_HEIGHT);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void mniCloseActionPerformed(java.awt.event.ActionEvent evt) {
        if (tournament == null) {
            JOptionPane.showMessageDialog(this, locale.getString("error.no_open_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Before carrying on, is there something to save ?
        if (!saveCurrentTournamentIfNecessary()) {
            return;
        }
        closeTournament();
    }

    private void mniImportTouActionPerformed(java.awt.event.ActionEvent evt) {
        this.importPlainFile("tou");
    }

    private void btnDlgNewOKActionPerformed(java.awt.event.ActionEvent evt) {
        int system = TournamentParameterSet.TYPE_MCMAHON;
        if (this.rdbMcMahon.isSelected()) {
            system = TournamentParameterSet.TYPE_MCMAHON;
        }
        if (this.rdbSwiss.isSelected()) {
            system = TournamentParameterSet.TYPE_SWISS;
        }
        if (this.rdbSwissCat.isSelected()) {
            system = TournamentParameterSet.TYPE_SWISSCAT;
        }

        TournamentParameterSet tps = new TournamentParameterSet();

        Date beginDate = new Date();
        try {
            beginDate = new SimpleDateFormat("yyyy-MM-dd").parse(this.txfBeginDate.getText());
        } catch (ParseException ex) {
            beginDate = new java.util.Date();
        }
        Date endDate = new Date();
        try {
            endDate = new SimpleDateFormat("yyyy-MM-dd").parse(this.txfEndDate.getText());
        } catch (ParseException ex) {
            endDate = new java.util.Date();
        }

        int nbRounds = 5;
        try {
            nbRounds = Integer.parseInt(this.txfNumberOfRounds.getText());
        } catch (NumberFormatException ex) {
            nbRounds = 5;
        }
        if (nbRounds < 0) {
            nbRounds = 1;
        }
        if (nbRounds > Gotha.MAX_NUMBER_OF_ROUNDS) {
            nbRounds = Gotha.MAX_NUMBER_OF_ROUNDS;
        }

        tps.initBase(this.txfShortName.getText(), this.txfName.getText(),
                this.txfLocation.getText(), this.txfDirector.getText(),
                beginDate, endDate,
                nbRounds, 1); // numberOfCategories will be set by initForXX

        switch (system) {
            case TournamentParameterSet.TYPE_MCMAHON:
                tps.initForMM();
                break;
            case TournamentParameterSet.TYPE_SWISS:
                tps.initForSwiss();
                break;
            case TournamentParameterSet.TYPE_SWISSCAT:
                tps.initForSwissCat();
                break;
            default:
                tps.initForMM();

        }

        TeamTournamentParameterSet ttps = new TeamTournamentParameterSet();
        ttps.init();

        // close previous Tournament if necessary
        closeTournament();

        try {
            tournament = new Tournament();
            tournament.setTournamentParameterSet(tps);
            tournament.setTeamTournamentParameterSet(ttps);
            this.lastDisplayedStandingsUpdateTime = 0;
            this.lastDisplayedTeamsStandingsUpdateTime = 0;
            this.tournamentChanged();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }

        // If we are in Server mode, then rebind tournament
        if (Gotha.runningMode == Gotha.RUNNING_MODE_SRV) {
            GothaRMIServer.addTournament(tournament);
        }

        dlgNew.dispose();
    }

    private void btnDlgNewCancelActionPerformed(java.awt.event.ActionEvent evt) {
        dlgNew.dispose();
    }

    private void mniTournamentOptionsActionPerformed(java.awt.event.ActionEvent evt) {
        if (tournament == null) {
            JOptionPane.showMessageDialog(this, "No currently open tournament", "Message", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            JFrame jfr = new JFrTournamentOptions(tournament);
            this.displayFrame(jfr, BIG_FRAME_WIDTH, BIG_FRAME_HEIGHT);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void mniNewActionPerformed(java.awt.event.ActionEvent evt) {
        // Before carrying on, is there something to save ?
        if (!saveCurrentTournamentIfNecessary()) {
            return;
        }
        dlgNew.setTitle(locale.getString("tournament.create"));
        this.displayFrame(dlgNew, MEDIUM_FRAME_WIDTH, SMALL_FRAME_HEIGHT);

        this.rdbMcMahon.setSelected(true);
        this.txfNumberOfRounds.setText("5");
        this.txfBeginDate.setText(new java.util.Date().toString());
        this.txfBeginDate.setText((new SimpleDateFormat("yyyy-MM-dd")).format(new java.util.Date()));
        this.txfEndDate.setText(new java.util.Date().toString());
        this.txfEndDate.setText((new SimpleDateFormat("yyyy-MM-dd")).format(new java.util.Date()));

        this.txfName.selectAll();
        this.txfShortName.selectAll();
        this.txfLocation.selectAll();
        this.txfBeginDate.selectAll();
        this.txfNumberOfRounds.selectAll();

        dlgNew.setVisible(true);

    }

    private void tpnGothaStateChanged(javax.swing.event.ChangeEvent evt) {
        updateAllViews();
    }

    private void cbxCritActionPerformed(java.awt.event.ActionEvent evt) {
        // In order to avoid useless updates, ...
        if (!this.cbxCrit1.isEnabled()) {
            return;
        }

        try {
            updateDisplayCriteria();
            updateStandingsComponents();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void rdbTemporaryPSActionPerformed(java.awt.event.ActionEvent evt) {
        updateAllViews();

    }

    private void rdbCurrentPSActionPerformed(java.awt.event.ActionEvent evt) {
        updateAllViews();

    }

    private void mniExitActionPerformed(java.awt.event.ActionEvent evt) {
        exitOpenGotha();
    }

    private void mniResultsActionPerformed(java.awt.event.ActionEvent evt) {
        if (tournament == null) {
            JOptionPane.showMessageDialog(this, locale.getString("error.no_open_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            JFrame jfr = new JFrGamesResults(tournament);   
            displayFrame(jfr, JFrGotha.BIG_FRAME_WIDTH, JFrGotha.BIG_FRAME_HEIGHT);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void mniPairActionPerformed(java.awt.event.ActionEvent evt) {
        if (tournament == null) {
            JOptionPane.showMessageDialog(this, locale.getString("error.no_open_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            JFrame jfr = new JFrGamesPair(tournament);
            displayFrame(jfr, JFrGotha.MEDIUM_FRAME_WIDTH, JFrGotha.MEDIUM_FRAME_HEIGHT);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void mniPlayersQuickCheckActionPerformed(java.awt.event.ActionEvent evt) {
        if (tournament == null) {
            JOptionPane.showMessageDialog(this, locale.getString("error.no_open_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            JFrame jfr = new JFrPlayersQuickCheck(tournament);
            displayFrame(jfr, JFrGotha.MEDIUM_FRAME_WIDTH, JFrGotha.MEDIUM_FRAME_HEIGHT);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void customInitComponents() {
        updateTitle();

        switch (Gotha.runningMode) {
            case Gotha.RUNNING_MODE_SAL:
                updateOpenRecentMenu();
                break;
            case Gotha.RUNNING_MODE_SRV:
                updateOpenRecentMenu();
                break;
            case Gotha.RUNNING_MODE_CLI:
                this.mniSaveAs.setVisible(false);
                this.mniSaveAs.setVisible(false);
                this.mniNew.setVisible(false);
                this.mniOpen.setVisible(false);
                this.mnuOpenRecent.setVisible(false);
                this.mniClose.setVisible(false);
                this.mnuImport.setVisible(false);
                this.mniBuildTestTournament.setVisible(false);
                this.mnuTools.setVisible(false);
                this.mniRMI.setVisible(false);
                break;
        }

        try {
            initCriteriaAndStandingsComponents();
            initControlPanelComponents();
            initTeamsPanelComponents();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
        getRootPane().setDefaultButton(btnSearch);
        
        if (tournament == null){
            this.tpnGotha.setSelectedComponent(this.pnlWelcome);
        }
        else{
            this.tpnGotha.setSelectedComponent(this.pnlControlPanel);
        }
    }

    /**
     * Get recent tournaments list from Preferences. Recent tournaments names are
     * supposed to look like "recentTournamentx" where x = 0 to 9
     */
    private ArrayList<String> getRecentTournamentsList() {
        Preferences prefsRoot = Preferences.userRoot();
        Preferences gothaPrefs = prefsRoot.node(Gotha.strPreferences);

        ArrayList<String> alS = new ArrayList<String>();
        for (int numRT = 0; numRT < MAX_NUMBER_OF_RECENT_TOURNAMENTS; numRT++) {
            String strK = "recentTournament" + numRT;
            String strRT = gothaPrefs.get(strK, "");
            if (strRT.compareTo("") != 0) {
                alS.add(strRT);
            }
        }
        return alS;
    }

    private void removeAllRecentTournament() {
        Preferences prefsRoot = Preferences.userRoot();
        Preferences gothaPrefs = prefsRoot.node(Gotha.strPreferences);
        int nbRT = MAX_NUMBER_OF_RECENT_TOURNAMENTS;
        for (int numRT = 0; numRT < nbRT; numRT++) {
            String strK = "recentTournament" + numRT;
            gothaPrefs.remove(strK);
        }
        this.updateOpenRecentMenu();
    }

    /**
     * Insert file name into Preferences
     */
    private void addRecentTournament(String strRecentTournamentFileName) {
        ArrayList<String> alS = getRecentTournamentsList();
        // avoid double
        alS.remove(strRecentTournamentFileName);
        alS.add(0, strRecentTournamentFileName);
        // avoid null
        alS.remove("null");

        Preferences prefsRoot = Preferences.userRoot();
        Preferences gothaPrefs = prefsRoot.node(Gotha.strPreferences);
        int nbRT = Math.min(alS.size(), MAX_NUMBER_OF_RECENT_TOURNAMENTS);
        removeAllRecentTournament();
        for (int numRT = 0; numRT < nbRT; numRT++) {
            String strK = "recentTournament" + numRT;
            String strRT = alS.get(numRT);
            gothaPrefs.put(strK, strRT);
        }

        this.updateOpenRecentMenu();
    }

    private void updateOpenRecentMenu() {
        ArrayList<String> alRT = this.getRecentTournamentsList();
        this.mnuOpenRecent.removeAll();
        mnuOpenRecent.setEnabled(true);
        if (alRT.isEmpty()) {
            mnuOpenRecent.setEnabled(false);
        }
        for (int numRT = 0; numRT < alRT.size(); numRT++) {
            final String strFile = alRT.get(numRT);
            JMenuItem mni = new JMenuItem(strFile);
            mni.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    File f = null;
                    try {
                        f = new File(strFile);
                        openTournament(f);
                    } catch (FileNotFoundException ex) {
                        JOptionPane.showMessageDialog(null, locale.getString("error.file_not_found"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
                        // Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (Exception ex) {
                        String strMessage = locale.format("error.file_reading_error", f.getName(), Gotha.GOTHA_DATA_VERSION);
                        JOptionPane.showMessageDialog(JFrGotha.this, strMessage, locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
                        JFrGotha.this.removeAllRecentTournament();
                    }
                }
            });
            this.mnuOpenRecent.add(mni);
        }
    }

    private void initCriteriaAndStandingsComponents() throws RemoteException {
        cbxCrit1.setModel(new DefaultComboBoxModel<String>(PlacementParameterSet.criteriaLongNames()));
        cbxCrit2.setModel(new DefaultComboBoxModel<String>(PlacementParameterSet.criteriaLongNames()));
        cbxCrit3.setModel(new DefaultComboBoxModel<String>(PlacementParameterSet.criteriaLongNames()));
        cbxCrit4.setModel(new DefaultComboBoxModel<String>(PlacementParameterSet.criteriaLongNames()));

        cbxTeamCrit1.setModel(new DefaultComboBoxModel<String>(TeamPlacementParameterSet.criteriaLongNames()));
        cbxTeamCrit2.setModel(new DefaultComboBoxModel<String>(TeamPlacementParameterSet.criteriaLongNames()));
        cbxTeamCrit3.setModel(new DefaultComboBoxModel<String>(TeamPlacementParameterSet.criteriaLongNames()));
        cbxTeamCrit4.setModel(new DefaultComboBoxModel<String>(TeamPlacementParameterSet.criteriaLongNames()));
        cbxTeamCrit5.setModel(new DefaultComboBoxModel<String>(TeamPlacementParameterSet.criteriaLongNames()));
        cbxTeamCrit6.setModel(new DefaultComboBoxModel<String>(TeamPlacementParameterSet.criteriaLongNames()));

        if (tournament == null) {
            return;
        }
        try {
            displayedRoundNumber = tournament.presumablyCurrentRoundNumber();
            displayedTeamRoundNumber = tournament.presumablyCurrentRoundNumber();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }

        updateDisplayCriteria();
        updateDisplayTeamCriteria();
        DefaultTableModel model = (DefaultTableModel) tblStandings.getModel();
        model.setColumnCount(ROUND0_RESULT_COL + Gotha.MAX_NUMBER_OF_ROUNDS + PlacementParameterSet.PLA_MAX_NUMBER_OF_CRITERIA);
        model = (DefaultTableModel) tblTeamsStandings.getModel();
        model.setColumnCount(TEAM_ROUND0_RESULT_COL + Gotha.MAX_NUMBER_OF_ROUNDS + TeamPlacementParameterSet.TPL_MAX_NUMBER_OF_CRITERIA);

        // Set the renderer for tblStandings
        tblStandings.setDefaultRenderer(Object.class, new StandingsTableCellRenderer());
        updateStandingsComponents();
        // Set the renderer for tblTeamsStandings
        tblTeamsStandings.setDefaultRenderer(Object.class, new StandingsTableCellRenderer());
        updateTeamsStandingsComponents();

    }

    private void updateTitle() {
        String strTitle = Gotha.getGothaReleaseVersion() + " ";
        switch (Gotha.runningMode) {
            case Gotha.RUNNING_MODE_SRV:
                strTitle += locale.getString("start.server") + ".  ";
                break;
            case Gotha.RUNNING_MODE_CLI:
                strTitle += locale.getString("start.client") + ".  ";
        }

        if (tournament != null) {
            try {
                strTitle += tournament.getTournamentParameterSet().getGeneralParameterSet().getName();
            } catch (RemoteException ex) {
                Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        setTitle(strTitle);
    }

    private void initControlPanelComponents() throws RemoteException {
        // Widths
        TableColumnModel tcm = this.tblControlPanel.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(30);
        tcm.getColumn(1).setPreferredWidth(80);
        tcm.getColumn(2).setPreferredWidth(80);
        tcm.getColumn(3).setPreferredWidth(100);

        // Headers
        JFrGotha.formatHeader(this.tblControlPanel, 0, locale.getString("game.round"), JLabel.RIGHT);
        JFrGotha.formatHeader(this.tblControlPanel, 1, locale.getString("player.participants"), JLabel.CENTER);
        JFrGotha.formatHeader(this.tblControlPanel, 2, locale.getString("control_panel.assigned"), JLabel.CENTER);
        JFrGotha.formatHeader(this.tblControlPanel, 3, locale.getString("control_panel.entered_results"), JLabel.CENTER);

        // Set the renderer for tblControlPanel
        tblControlPanel.setDefaultRenderer(Object.class, this.cpTableCellRenderer);

        updateControlPanel();
    }

    private void initTeamsPanelComponents() throws RemoteException {
        // Widths
        TableColumnModel tcm = this.tblTeamsPanel.getColumnModel();
        tcm.getColumn(TM_TEAM_NUMBER_COL).setPreferredWidth(20);
        tcm.getColumn(TM_TEAM_NAME_COL).setPreferredWidth(100);
        tcm.getColumn(TM_BOARD_NUMBER_COL).setPreferredWidth(20);
        tcm.getColumn(TM_PL_NAME_COL).setPreferredWidth(120);
        tcm.getColumn(TM_PL_COUNTRY_COL).setPreferredWidth(30);
        tcm.getColumn(TM_PL_CLUB_COL).setPreferredWidth(30);
        tcm.getColumn(TM_PL_RATING_COL).setPreferredWidth(40);
        tcm.getColumn(TM_PL_ROUNDS_COL).setPreferredWidth(120);

        // Headers
        JFrGotha.formatHeader(this.tblTeamsPanel, TM_TEAM_NUMBER_COL, "Nr", JLabel.RIGHT);
        JFrGotha.formatHeader(this.tblTeamsPanel, TM_TEAM_NAME_COL, locale.getString("player.teams.name"), JLabel.LEFT);
        JFrGotha.formatHeader(this.tblTeamsPanel, TM_BOARD_NUMBER_COL, locale.getString("game.board"), JLabel.RIGHT);
        JFrGotha.formatHeader(this.tblTeamsPanel, TM_PL_NAME_COL, locale.getString("player.teams.player_name"), JLabel.LEFT);
        JFrGotha.formatHeader(this.tblTeamsPanel, TM_PL_COUNTRY_COL, locale.getString("player.country"), JLabel.CENTER);
        JFrGotha.formatHeader(this.tblTeamsPanel, TM_PL_CLUB_COL, locale.getString("player.club"), JLabel.CENTER);
        JFrGotha.formatHeader(this.tblTeamsPanel, TM_PL_RATING_COL, locale.getString("player.rating"), JLabel.CENTER);
        JFrGotha.formatHeader(this.tblTeamsPanel, TM_PL_ROUNDS_COL, locale.getString("game.rounds"), JLabel.LEFT);

        // Set the renderer for tblControlPanel
        tblTeamsPanel.setDefaultRenderer(Object.class, this.tpTableCellRenderer);

        updateTeamsPanel();
    }

    private void updateDisplayCriteria() throws RemoteException {
        // update bDisplayTemporaryParameterSet
        bDisplayTemporaryParameterSet = (this.grpPS.getSelection() == this.rdbTemporaryPS.getModel());

        if (bDisplayTemporaryParameterSet) {
            Arrays.fill(displayedCriteria, PlacementCriterion.NUL);
            displayedCriteria[0] = PlacementCriterion.fromLongName((String) cbxCrit1.getModel().getSelectedItem());
            displayedCriteria[1] = PlacementCriterion.fromLongName((String) cbxCrit2.getModel().getSelectedItem());
            displayedCriteria[2] = PlacementCriterion.fromLongName((String) cbxCrit3.getModel().getSelectedItem());
            displayedCriteria[3] = PlacementCriterion.fromLongName((String) cbxCrit4.getModel().getSelectedItem());
        } else {
            if (tournament != null) {
                PlacementParameterSet displayedPPS = tournament.getTournamentParameterSet().getPlacementParameterSet();
                Arrays.fill(displayedCriteria, PlacementCriterion.NUL);
                displayedCriteria[0] = displayedPPS.getPlaCriteria()[0];
                displayedCriteria[1] = displayedPPS.getPlaCriteria()[1];
                displayedCriteria[2] = displayedPPS.getPlaCriteria()[2];
                displayedCriteria[3] = displayedPPS.getPlaCriteria()[3];
            }
        }
    }

    private void updateDisplayTeamCriteria() throws RemoteException {
        // update bDisplayTemporaryTeamParameterSet
        bDisplayTemporaryTeamParameterSet = (this.grpTeamPS.getSelection() == this.rdbTemporaryTeamPS.getModel());

        if (bDisplayTemporaryTeamParameterSet) {
            displayedTeamCriteria[0] = TeamPlacementCriterion.fromLongName((String) cbxTeamCrit1.getModel().getSelectedItem());
            displayedTeamCriteria[1] = TeamPlacementCriterion.fromLongName((String) cbxTeamCrit2.getModel().getSelectedItem());
            displayedTeamCriteria[2] = TeamPlacementCriterion.fromLongName((String) cbxTeamCrit3.getModel().getSelectedItem());
            displayedTeamCriteria[3] = TeamPlacementCriterion.fromLongName((String) cbxTeamCrit4.getModel().getSelectedItem());
            displayedTeamCriteria[4] = TeamPlacementCriterion.fromLongName((String) cbxTeamCrit5.getModel().getSelectedItem());
            displayedTeamCriteria[5] = TeamPlacementCriterion.fromLongName((String) cbxTeamCrit6.getModel().getSelectedItem());
        } else {
            if (tournament != null) {
                TeamPlacementParameterSet displayedTeamPPS = tournament.getTeamTournamentParameterSet().getTeamPlacementParameterSet();
                displayedTeamCriteria[0] = displayedTeamPPS.getPlaCriteria()[0];
                displayedTeamCriteria[1] = displayedTeamPPS.getPlaCriteria()[1];
                displayedTeamCriteria[2] = displayedTeamPPS.getPlaCriteria()[2];
                displayedTeamCriteria[3] = displayedTeamPPS.getPlaCriteria()[3];
                displayedTeamCriteria[4] = displayedTeamPPS.getPlaCriteria()[4];
                displayedTeamCriteria[5] = displayedTeamPPS.getPlaCriteria()[5];
            }
        }
    }

    private void updateStandingsComponents() throws RemoteException {
        if (tournament == null) {
            return;
        }

        if (this.tpnGotha.getSelectedComponent() != pnlStandings) {
            return;
        }
        int nbRounds = tournament.getTournamentParameterSet().getGeneralParameterSet().getNumberOfRounds();
        if (displayedRoundNumber > nbRounds - 1) {
            displayedRoundNumber = nbRounds - 1;
        }
        this.spnRoundNumber.setValue(displayedRoundNumber + 1);

        this.rdbCurrentPS.getModel().setSelected(!bDisplayTemporaryParameterSet);
        this.cbxCrit1.setEnabled(bDisplayTemporaryParameterSet);
        this.cbxCrit2.setEnabled(bDisplayTemporaryParameterSet);
        this.cbxCrit3.setEnabled(bDisplayTemporaryParameterSet);
        this.cbxCrit4.setEnabled(bDisplayTemporaryParameterSet);

        this.cbxCrit1.getModel().setSelectedItem(displayedCriteria[0].getLongName());
        this.cbxCrit2.getModel().setSelectedItem(displayedCriteria[1].getLongName());
        this.cbxCrit3.getModel().setSelectedItem(displayedCriteria[2].getLongName());
        this.cbxCrit4.getModel().setSelectedItem(displayedCriteria[3].getLongName());

        // Define displayedTPS
        TournamentParameterSet tps = tournament.getTournamentParameterSet();
        TournamentParameterSet displayedTPS = new TournamentParameterSet(tps);
        PlacementParameterSet displayedPPS = displayedTPS.getPlacementParameterSet();
        displayedPPS.setPlaCriteria(displayedCriteria);

        int gameFormat = tps.getDPParameterSet().getGameFormat();

        lastDisplayedStandingsUpdateTime = tournament.getCurrentTournamentTime();
        ArrayList<ScoredPlayer> alOrderedScoredPlayers = new ArrayList<ScoredPlayer>();
        try {
            alOrderedScoredPlayers = tournament.orderedScoredPlayersList(displayedRoundNumber, displayedTPS.getPlacementParameterSet());

            DPParameterSet dpps = tps.getDPParameterSet();
            if (!dpps.isDisplayNPPlayers()){
                // Eliminate non-players
                for (Iterator<ScoredPlayer> it = alOrderedScoredPlayers.iterator(); it.hasNext();) {
                    ScoredPlayer sP = it.next();
                    if (!tournament.isPlayerImplied(sP)) {
                        it.remove();
                    }
                }
            }

        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
        boolean bFull = true;
        if (gameFormat == DPParameterSet.DP_GAME_FORMAT_SHORT) {
            bFull = false;
        }
        String[][] hG = ScoredPlayer.halfGamesStrings(alOrderedScoredPlayers, displayedRoundNumber, displayedTPS, bFull);

        tblStandings.clearSelection();
        tblStandings.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        DefaultTableColumnModel columnModel = (DefaultTableColumnModel) tblStandings.getColumnModel();

        String strNumHeader = "Num";
        if (!tps.getDPParameterSet().isDisplayNumCol()) {
            strNumHeader = "";
        }
        columnModel.getColumn(NUM_COL).setHeaderValue(strNumHeader);

        String strPlHeader = "Pl";
        if (!tps.getDPParameterSet().isDisplayPlCol()) {
            strPlHeader = "";
        }
        columnModel.getColumn(PL_COL).setHeaderValue(strPlHeader);
        
        columnModel.getColumn(NAME_COL).setHeaderValue("Name");
        columnModel.getColumn(GRADE_COL).setHeaderValue("Gr");
        String strCoHeader = "Co";
        if (!tps.getDPParameterSet().isDisplayCoCol()) {
            strCoHeader = "";
        }
        columnModel.getColumn(COUNTRY_COL).setHeaderValue(strCoHeader);

        String strClHeader = "Cl";
        if (!tps.getDPParameterSet().isDisplayClCol()) {
            strClHeader = "";
        }
        columnModel.getColumn(CLUB_COL).setHeaderValue(strClHeader);
        
        columnModel.getColumn(NBW_COL).setHeaderValue("NBW");

        for (int r = 0; r < Gotha.MAX_NUMBER_OF_ROUNDS; r++) {
            columnModel.getColumn(ROUND0_RESULT_COL + r).setHeaderValue("R" + (r + 1));
        }
        for (int c = 0; c < PlacementParameterSet.PLA_MAX_NUMBER_OF_CRITERIA; c++) {
            columnModel.getColumn(CRIT0_COL + c).setHeaderValue(displayedCriteria[c].getShortName());
        }
        int numWidth = 30;
        if (!tps.getDPParameterSet().isDisplayNumCol()) {
            numWidth = 0;
        }
        columnModel.getColumn(NUM_COL).setPreferredWidth(numWidth);
        int plWidth = 30;
        if (!tps.getDPParameterSet().isDisplayPlCol()) {
            plWidth = 0;
        }
        columnModel.getColumn(PL_COL).setPreferredWidth(plWidth);
        int coWidth = 20;
        if (!tps.getDPParameterSet().isDisplayCoCol()) {
            coWidth = 0;
        }
        columnModel.getColumn(COUNTRY_COL).setPreferredWidth(coWidth);
        int clWidth = 30;
        if (!tps.getDPParameterSet().isDisplayClCol()) {
            clWidth = 0;
        }
        columnModel.getColumn(CLUB_COL).setPreferredWidth(clWidth);

        columnModel.getColumn(NAME_COL).setPreferredWidth(110);
        columnModel.getColumn(GRADE_COL).setPreferredWidth(30);
        columnModel.getColumn(NBW_COL).setPreferredWidth(20);
        for (int r = 0; r <= displayedRoundNumber; r++) {
            int roundColWidth = 55;
            if (gameFormat == DPParameterSet.DP_GAME_FORMAT_SHORT) {
                roundColWidth = 35;
            }
            columnModel.getColumn(ROUND0_RESULT_COL + r).setPreferredWidth(roundColWidth);
        }
        for (int r = displayedRoundNumber + 1; r <= Gotha.MAX_NUMBER_OF_ROUNDS; r++) {
            columnModel.getColumn(ROUND0_RESULT_COL + r).setMinWidth(0);
            columnModel.getColumn(ROUND0_RESULT_COL + r).setPreferredWidth(0);
        }

        for (int c = 0; c < PlacementParameterSet.PLA_MAX_NUMBER_OF_CRITERIA; c++) {
            if (displayedPPS.getPlaCriteria()[c] == PlacementCriterion.NUL) {
                columnModel.getColumn(CRIT0_COL + c).setMinWidth(0);
                columnModel.getColumn(CRIT0_COL + c).setPreferredWidth(0);
            } else {
                columnModel.getColumn(CRIT0_COL + c).setPreferredWidth(40);
            }
        }

        DefaultTableModel model = (DefaultTableModel) tblStandings.getModel();
        model.setRowCount(alOrderedScoredPlayers.size());
        String[] strPlace = ScoredPlayer.catPositionStrings(alOrderedScoredPlayers, displayedRoundNumber, displayedTPS);
        for (int iSP = 0; iSP < alOrderedScoredPlayers.size(); iSP++) {
            int iCol = 0;
            ScoredPlayer sp = alOrderedScoredPlayers.get(iSP);
            String strNum = "" + (iSP + 1);
            if (!tps.getDPParameterSet().isDisplayNumCol()) {
                strNum = "";
            }
            model.setValueAt(strNum, iSP, iCol++);
            
            String strPl = "" + strPlace[iSP];
            if (!tps.getDPParameterSet().isDisplayPlCol()) {
                strPl = "";
            }
            model.setValueAt("" + strPl, iSP, iCol++);
  
            model.setValueAt(sp.fullName(), iSP, iCol++);                       
            model.setValueAt(sp.getStrGrade(), iSP, iCol++);

            String strCo = sp.getCountry();
            if (!tps.getDPParameterSet().isDisplayCoCol()) {
                strCo = "";
            }
            model.setValueAt(strCo, iSP, iCol++);
            
            String strCl = sp.getClub();
            if (!tps.getDPParameterSet().isDisplayClCol()) {
                strCl = "";
            }
            model.setValueAt(strCl, iSP, iCol++);
           
            
            model.setValueAt(sp.formatScore(PlacementCriterion.NBW, this.displayedRoundNumber), iSP, iCol++);
            for (int r = 0; r <= displayedRoundNumber; r++) {
                model.setValueAt((hG[r][iSP]), iSP, iCol++);
            }
            for (int r = displayedRoundNumber + 1; r < Gotha.MAX_NUMBER_OF_ROUNDS; r++) {
                model.setValueAt("", iSP, iCol++);
            }
            for (int c = 0; c < displayedCriteria.length; c++) {
                model.setValueAt(sp.formatScore(displayedCriteria[c], this.displayedRoundNumber), iSP, iCol++);
            }
        }

        java.util.Date dh = new java.util.Date(lastDisplayedStandingsUpdateTime);
		lblUpdateTime.setText(this.locale.format("standings.update_time", dh));
    }

    private void updateTeamsStandingsComponents() throws RemoteException {
        if (tournament == null) {
            return;
        }
        if (tournament.teamsList().isEmpty()) {
            return;
        }

        if (this.tpnGotha.getSelectedComponent() != pnlTeamsStandings) {
            return;
        }
        int nbRounds = tournament.getTournamentParameterSet().getGeneralParameterSet().getNumberOfRounds();
        if (this.displayedTeamRoundNumber > nbRounds - 1) {
            displayedTeamRoundNumber = nbRounds - 1;
        }
        this.spnTeamRoundNumber.setValue(displayedTeamRoundNumber + 1);

        this.rdbCurrentTeamPS.getModel().setSelected(!bDisplayTemporaryTeamParameterSet);
        this.cbxTeamCrit1.setEnabled(bDisplayTemporaryTeamParameterSet);
        this.cbxTeamCrit2.setEnabled(bDisplayTemporaryTeamParameterSet);
        this.cbxTeamCrit3.setEnabled(bDisplayTemporaryTeamParameterSet);
        this.cbxTeamCrit4.setEnabled(bDisplayTemporaryTeamParameterSet);
        this.cbxTeamCrit5.setEnabled(bDisplayTemporaryTeamParameterSet);
        this.cbxTeamCrit6.setEnabled(bDisplayTemporaryTeamParameterSet);

        this.cbxTeamCrit1.getModel().setSelectedItem(displayedTeamCriteria[0].getLongName());
        this.cbxTeamCrit2.getModel().setSelectedItem(displayedTeamCriteria[1].getLongName());
        this.cbxTeamCrit3.getModel().setSelectedItem(displayedTeamCriteria[2].getLongName());
        this.cbxTeamCrit4.getModel().setSelectedItem(displayedTeamCriteria[3].getLongName());
        this.cbxTeamCrit5.getModel().setSelectedItem(displayedTeamCriteria[4].getLongName());
        this.cbxTeamCrit6.getModel().setSelectedItem(displayedTeamCriteria[5].getLongName());

        // Define displayedTeamTPS
        TeamTournamentParameterSet ttps = tournament.getTeamTournamentParameterSet();
        TeamTournamentParameterSet displayedTeamTPS = new TeamTournamentParameterSet(ttps);
        TeamPlacementParameterSet displayedTeamPPS = displayedTeamTPS.getTeamPlacementParameterSet();
        displayedTeamPPS.setPlaCriteria(displayedTeamCriteria);


        lastDisplayedTeamsStandingsUpdateTime = tournament.getCurrentTournamentTime();
        ScoredTeamsSet sts = tournament.getAnUpToDateScoredTeamsSet(displayedTeamPPS, displayedTeamRoundNumber);
        ArrayList<ScoredTeam> alOrderedScoredTeams = sts.getOrderedScoredTeamsList();

        this.tblTeamsStandings.clearSelection();
        tblTeamsStandings.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        DefaultTableColumnModel columnModel = (DefaultTableColumnModel) tblTeamsStandings.getColumnModel();

        ((DefaultTableCellRenderer) tblTeamsStandings.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        columnModel.getColumn(TEAM_PL_COL).setHeaderValue("PL.");
        columnModel.getColumn(TEAM_NAME_COL).setHeaderValue("Name");

        for (int r = 0; r < Gotha.MAX_NUMBER_OF_ROUNDS; r++) {
            columnModel.getColumn(TEAM_ROUND0_RESULT_COL + r).setHeaderValue("R" + (r + 1));
        }
        for (int c = 0; c < TeamPlacementParameterSet.TPL_MAX_NUMBER_OF_CRITERIA; c++) {
            String strCrit = displayedTeamCriteria[c].getShortName();
            if (displayedTeamCriteria[c] == TeamPlacementCriterion.NUL) {
                strCrit = "";
            }
            TableColumn tc = columnModel.getColumn(TEAM_CRIT0_COL + c);
            tc.setHeaderValue(strCrit);
        }
        columnModel.getColumn(TEAM_PL_COL).setPreferredWidth(30);
        columnModel.getColumn(TEAM_NAME_COL).setPreferredWidth(110);

        for (int r = 0; r <= displayedTeamRoundNumber; r++) {
            columnModel.getColumn(TEAM_ROUND0_RESULT_COL + r).setPreferredWidth(40);
        }
        for (int r = displayedTeamRoundNumber + 1; r <= Gotha.MAX_NUMBER_OF_ROUNDS; r++) {
            columnModel.getColumn(TEAM_ROUND0_RESULT_COL + r).setMinWidth(0);
            columnModel.getColumn(TEAM_ROUND0_RESULT_COL + r).setPreferredWidth(0);
        }

        for (int c = 0; c < TeamPlacementParameterSet.TPL_MAX_NUMBER_OF_CRITERIA; c++) {
            if (displayedTeamPPS.getPlaCriteria()[c] == TeamPlacementCriterion.NUL) {
                columnModel.getColumn(TEAM_CRIT0_COL + c).setMinWidth(0);
                columnModel.getColumn(TEAM_CRIT0_COL + c).setPreferredWidth(0);
            } else {
                columnModel.getColumn(TEAM_CRIT0_COL + c).setPreferredWidth(40);
            }
        }

        DefaultTableModel model = (DefaultTableModel) tblTeamsStandings.getModel();
        int nbTeams = alOrderedScoredTeams.size();
        model.setRowCount(nbTeams);
        for (int ist = 0; ist < nbTeams; ist++) {
            int iCol = 0;
            ScoredTeam st = alOrderedScoredTeams.get(ist);
            model.setValueAt("" + (ist + 1), ist, iCol++);
            model.setValueAt(st.getTeamName(), ist, iCol++);
            for (int r = 0; r <= displayedTeamRoundNumber; r++) {
                model.setValueAt(sts.getHalfMatchString(st, r), ist, iCol++);
            }
            for (int r = displayedTeamRoundNumber + 1; r < Gotha.MAX_NUMBER_OF_ROUNDS; r++) {
                model.setValueAt("", ist, iCol++);
            }
            for (int ic = 0; ic < this.displayedTeamCriteria.length; ic++) {
                int crit = this.displayedTeamCriteria[ic].getUid();
                int coef = TeamPlacementParameterSet.criterionCoef(crit);
                String strCritValue = Gotha.formatFractNumber(st.getCritValue(ic), coef);
                model.setValueAt(strCritValue, ist, TEAM_CRIT0_COL + ic);
            }
        }

        java.util.Date dh = new java.util.Date(lastDisplayedTeamsStandingsUpdateTime);
		lblTeamUpdateTime.setText(this.locale.format("standings.update_time", dh));
    }
    
    private void updateWelcomePanel() throws RemoteException {        
        
    }
 
    private void updateControlPanel() throws RemoteException {
        if (tournament == null) {
            return;
        }

        TournamentParameterSet tps = tournament.getTournamentParameterSet();
        DefaultTableModel model = (DefaultTableModel) tblControlPanel.getModel();
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
        ArrayList<Player> alPlayers = tournament.playersList();

        for (int r = 0; r < tps.getGeneralParameterSet().getNumberOfRounds(); r++) {
            // Number of participants
            int nbParticipants = 0;
            for (Player p : alPlayers) {
                if (p.getParticipating()[r]) {
                    nbParticipants++;
                    // Assigned players, games, etc.
                }
            }
            ArrayList<Game> alGames = tournament.gamesList(r);
            int nbGames = alGames.size();
            int nbAssignedPlayers = 2 * nbGames;
            if (tournament.getByePlayer(r) != null) {
                nbAssignedPlayers++;
            }
            int nbEntResults = 0;
            for (Game g : alGames) {
                int result = g.getResult();
                if (result != Game.RESULT_UNKNOWN) {
                    nbEntResults++;
                }
            }

            Vector<String> row = new Vector<String>();
            row.add("" + (r + 1));

            row.add("" + nbParticipants);

            row.add("" + nbAssignedPlayers);
            if (nbAssignedPlayers != nbParticipants) {
                this.cpTableCellRenderer.cpWarning[r][2] = true;
            } else {
                this.cpTableCellRenderer.cpWarning[r][2] = false;
            }

            row.add("" + nbEntResults + "/" + nbGames);
            if (nbEntResults != nbGames) {
                this.cpTableCellRenderer.cpWarning[r][3] = true;
            } else {
                this.cpTableCellRenderer.cpWarning[r][3] = false;
            }

            model.addRow(row);
        }
        tblControlPanel.clearSelection();
        tblControlPanel.changeSelection(1, 2, true, false);
        tblControlPanel.changeSelection(5, 1, true, false);

        this.lblWarningPRE.setText("");

        int nbPreliminary = 0;
        for (Player p : alPlayers) {
            if (p.getRegisteringStatus() == PRELIMINARY) {
                nbPreliminary++;

            }
        }
        if (nbPreliminary >= 1) {
            lblWarningPRE.setText(locale.format("control_panel.players_preliminary", nbPreliminary));
        }
    }

    // TODO : UpdateTeamsPanel should use TeamMemberStrings (See TournamentPrinting or ExternalDocument.generateTeamsListHTMLFile 
    private void updateTeamsPanel() throws RemoteException {
        DefaultTableModel model = (DefaultTableModel) this.tblTeamsPanel.getModel();
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
        if (tournament == null) {
            return;
        }

        ArrayList<Team> alDisplayedTeams = tournament.teamsList();

        int teamSize = 0;
        try {
            teamSize = tournament.getTeamSize();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrTeamsManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        TeamComparator teamComparator = new TeamComparator(TeamComparator.TEAM_NUMBER_ORDER, teamSize);
        Collections.sort(alDisplayedTeams, teamComparator);


        for (Team t : alDisplayedTeams) {
            Object[] row = new Object[TM_NUMBER_OF_COLS];
            row[TM_TEAM_NUMBER_COL] = "" + (t.getTeamNumber() + 1);
            row[TM_TEAM_NAME_COL] = t.getTeamName();
            row[TM_BOARD_NUMBER_COL] = "";
            row[TM_PL_NAME_COL] = "";
            row[TM_PL_COUNTRY_COL] = "";
            row[TM_PL_CLUB_COL] = "";
            row[TM_PL_RATING_COL] = "";
            row[TM_PL_ROUNDS_COL] = "";

            model = (DefaultTableModel) this.tblTeamsPanel.getModel();
            model.addRow(row);

            for (int ib = 0; ib < teamSize; ib++) {
                ArrayList<Player> alP = tournament.playersList(t, ib);
                if (alP.isEmpty()) {
                    alP.add(null);
                }
                for (Player p : alP) {
                    row = new Object[TM_NUMBER_OF_COLS];
                    row[TM_TEAM_NUMBER_COL] = "";
                    row[TM_TEAM_NAME_COL] = "";
                    row[TM_BOARD_NUMBER_COL] = "" + (ib + 1);
                    if (p == null) {
                        row[TM_PL_NAME_COL] = "";
                        row[TM_PL_COUNTRY_COL] = "";
                        row[TM_PL_CLUB_COL] = "";
                        row[TM_PL_RATING_COL] = "";
                    } else {
                        row[TM_PL_NAME_COL] = p.getName() + " " + p.getFirstName();
                        row[TM_PL_COUNTRY_COL] = p.getCountry();
                        row[TM_PL_CLUB_COL] = p.getClub();
                        row[TM_PL_RATING_COL] = p.getRating();
                    }

                    if (p == null) {
                        row[TM_PL_ROUNDS_COL] = "";
                    } else {
                        int numberOfRounds = 0;
                        try {
                            numberOfRounds = tournament.getTournamentParameterSet().getGeneralParameterSet().getNumberOfRounds();
                        } catch (RemoteException ex) {
                            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        boolean[] bM = tournament.membership(p, t, ib);
                        String str = "";
                        for (int r = 0; r < numberOfRounds; r++) {
                            str += bM[r] ? "+" : "-";
                        }
                        row[TM_PL_ROUNDS_COL] = str;
                    }

                    model = (DefaultTableModel) this.tblTeamsPanel.getModel();
                    model.addRow(row);
                }
            }

        }
    }

    /**
     * if necessary, saves the current tournament
     *
     * @return false if operation has been cancelled
     */
    private boolean saveCurrentTournamentIfNecessary() {
        try {
            if (Gotha.runningMode == Gotha.RUNNING_MODE_CLI) {
                return true;
            }
            if (tournament == null) {
                return true;
            }           
            if (!tournament.isChangeSinceLastSave()) {
                return true;
            }

            int response = JOptionPane.showConfirmDialog(this, locale.getString("tournament.confirm_save"),
                    locale.getString("alert.message"), JOptionPane.YES_NO_CANCEL_OPTION);
            if (response == JOptionPane.CANCEL_OPTION) {
                return false;
            }
            if (response == JOptionPane.YES_OPTION) {
                File f =  this.chooseASaveFile(this.getDefaultSaveAsFileName());
                updateShortNameFromFile(f);
                this.saveTournament(f);

                tournament.setChangeSinceLastSaveAsFalse();
                tournament.setHasBeenSavedOnce(true);
                this.addRecentTournament("" + f);
                this.tournamentChanged();

                return true;
            }
            return true;
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private void saveTournamentToAWorkFile(){
        String shortName = "shortName";
        try {
            shortName = tournament.getShortName();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
        File snFile = new File(Gotha.runningDirectory + "/tournamentfiles", shortName + "_work.xml"); 
        saveTournament(snFile);
        
    }
        
    private File saveTournament(File f) {
        TournamentInterface t = tournament;
        return saveTournament(t, f);
    }

    private File saveTournament(TournamentInterface t, File f) {
        if (tournament == null) {
            JOptionPane.showMessageDialog(this, locale.getString("error.no_open_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return null;
        }
        // if current extension is not .xml, add .xml
        String suffix = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 && i < s.length() - 1) {
            suffix = s.substring(i + 1).toLowerCase();
        }
        try {
            if (suffix == null || !suffix.equals("xml")) {
                f = new File(f.getCanonicalPath() + ".xml");
            }
        } catch (IOException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }

        ExternalDocument.generateXMLFile(t, f);
        
        return f;
        
    }

    private void mniHelpAboutActionPerformed(java.awt.event.ActionEvent evt) {
        javax.swing.JTextArea txa = new javax.swing.JTextArea(Gotha.getCopyLeftText() + Gotha.getThanksToText());
        txa.setFont(new Font("Tahoma", Font.PLAIN, 11));
        JOptionPane.showMessageDialog(this, txa, "OpenGotha",
                JOptionPane.INFORMATION_MESSAGE, new ImageIcon(Gotha.getIconImage()));
    }

    private void mniPlayersManagerActionPerformed(java.awt.event.ActionEvent evt) {
        if (tournament == null) {
            JOptionPane.showMessageDialog(this, locale.getString("error.no_open_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            JFrame jfr = new JFrPlayersManager(tournament);
            this.displayFrame(jfr, BIG_FRAME_WIDTH, BIG_FRAME_HEIGHT);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void mniSaveAsActionPerformed(java.awt.event.ActionEvent evt) {
        if (tournament == null) {
            JOptionPane.showMessageDialog(this, locale.getString("error.no_open_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        File f =  this.chooseASaveFile(this.getDefaultSaveAsFileName());
        if (f == null) return;
        
        updateShortNameFromFile(f);
        
        // Make actual save
        this.saveTournament(f);
        try {
            tournament.setChangeSinceLastSaveAsFalse();
            tournament.setHasBeenSavedOnce(true);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }               
        this.addRecentTournament("" + f);
        
        this.tournamentChanged();
    }
    
    // Manages the JFileChooser Dialog and makes actual save
    File chooseASaveFile(String fileName){
        File defFile = new File(fileName);
        
        File dir = defFile.getParentFile();
        String fn = defFile.getName();
        
        JFileChooser fileChoice = new JFileChooser(dir);
        fileChoice.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChoice.setDialogType(JFileChooser.SAVE_DIALOG);
      
        fileChoice.setSelectedFile(new File(fn));

        MyFileFilter mff = new MyFileFilter(new String[]{"xml"},
                "Gotha Files(*.xml)");
        fileChoice.addChoosableFileFilter(mff);
        int result = fileChoice.showSaveDialog(this);
        File f = null;
        if (result == JFileChooser.CANCEL_OPTION) {
            f = null;
        } else {
            f = fileChoice.getSelectedFile();
        }
        if (f == null) {
            return null;
        }
        return f;
    }
     
    void updateShortNameFromFile(File f){
        String fileName = "" + f;
        int indLastSep = 0;
        // drop path and extension 
        for (int i = 0; i < fileName.length(); i++){
            if (fileName.charAt(i) == '/') indLastSep = i;
            if (fileName.charAt(i) == '\\') indLastSep = i;
        }
        String snExt = fileName.substring(indLastSep + 1);
        
        int indLastPoint = snExt.length();
        for (int i = 0; i < snExt.length(); i++){
            if (snExt.charAt(i) == '.') indLastPoint = i;
        }
        String sn = snExt.substring(0, indLastPoint);
        try {
            tournament.setShortName(sn);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

        
    /** 
     * Used to know what is the default Tournament File Name for saving
     * if hasBeenSavedOnce = false, the default is based on runningDirectory + "/tournamentfile/" sand shortName
     * else default is the °th recent tournament file
     * if no recent tournament file, default is based on runningDirectory and shortName
     * @return 
     */
    String getDefaultSaveAsFileName(){
        boolean bHBSO = false;
        try {
            bHBSO = tournament.isHasBeenSavedOnce();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String shortName = "shortName";
        try {
            shortName = tournament.getShortName();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
        File snFile = new File(Gotha.runningDirectory + "/tournamentfiles", shortName + ".xml"); 
        String snFN;
        snFN = "" + snFile;

        String rtFN = "";
        ArrayList<String> alRT = this.getRecentTournamentsList();
        if (alRT != null && alRT.size() > 0) rtFN = alRT.get(0);

        if (!bHBSO) return snFN;
        if (rtFN.length() < 1) return snFN;
        return rtFN;
    }
 
private void mniRRActionPerformed(java.awt.event.ActionEvent evt) {
    if (tournament == null) {
        JOptionPane.showMessageDialog(this, locale.getString("error.no_open_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
        return;
    }
    try {
        JFrame jfr = new JFrGamesRR(tournament);
        this.displayFrame(jfr, MEDIUM_FRAME_WIDTH, MEDIUM_FRAME_HEIGHT);
    } catch (RemoteException ex) {
        Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
    }

}

private void mniRMIActionPerformed(java.awt.event.ActionEvent evt) {
    JFrame jfr = new JFrToolsRMI();
    this.displayFrame(jfr, MEDIUM_FRAME_WIDTH, MEDIUM_FRAME_HEIGHT);
}

private void mniOpenGothaHelpActionPerformed(java.awt.event.ActionEvent evt) {
    Gotha.displayGothaHelp("Starting OpenGotha");
}

private void mniImportXMLActionPerformed(java.awt.event.ActionEvent evt) {
    if (tournament == null) {
        JOptionPane.showMessageDialog(this, locale.getString("error.no_open_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
        return;
    }

    int w = JFrGotha.SMALL_FRAME_WIDTH;
    int h = JFrGotha.SMALL_FRAME_HEIGHT;
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    dlgImportXML.setBounds((dim.width - w) / 2, (dim.height - h) / 2, w, h);
    dlgImportXML.setTitle("Import from XML File");
    dlgImportXML.setIconImage(Gotha.getIconImage());

    dlgImportXML.setVisible(true);


}

private void btnDlgImportXMLOKActionPerformed(java.awt.event.ActionEvent evt) {
    dlgImportXML.dispose();

    // From what file shall we import ?
    File f = chooseAFile(Gotha.runningDirectory, "xml");
    if (f == null) {
        return;
    }

    String strReport = ExternalDocument.importTournamentFromXMLFile(f, tournament,
            this.chkPlayers.isSelected(), this.chkGames.isSelected(), this.chkTournamentParameters.isSelected(), this.chkTeams.isSelected(), this.chkClubsGroups.isSelected());

    this.tournamentChanged();
    JOptionPane.showMessageDialog(this, strReport, locale.getString("alert.message"), JOptionPane.INFORMATION_MESSAGE);
}

private void btnDlgImportXMLCancelActionPerformed(java.awt.event.ActionEvent evt) {
    this.dlgImportXML.dispose();
}

private void spnRoundNumberStateChanged(javax.swing.event.ChangeEvent evt) {
    int demandedRN = (Integer) (spnRoundNumber.getValue()) - 1;
    this.demandedDisplayedRoundNumberHasChanged(demandedRN);
}

private void btnHelpActionPerformed(java.awt.event.ActionEvent evt) {
    Gotha.displayGothaHelp("Create a new tournament");
}

private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {
    String strSearchPlayer = this.txfSearchPlayer.getText().toLowerCase();
    if (strSearchPlayer.length() == 0) {
        tblStandings.clearSelection();
        return;
    }
    TableModel model = tblStandings.getModel();

    int rowNumber = -1;
    int startRow = tblStandings.getSelectedRow() + 1;
    int nbRows = model.getRowCount();
    for (int iR = 0; iR < nbRows; iR++) {
        int row = (startRow + iR) % nbRows;
        String str = (String) model.getValueAt(row, NAME_COL);
        str = str.toLowerCase();
        if (!str.contains(strSearchPlayer)) {
            continue;
        }
        // OK! Found
        rowNumber = row;
        break;
    }

    tblStandings.clearSelection();
    if (rowNumber == -1) {
        JOptionPane.showMessageDialog(this,
                locale.getString("player.search.not_found"),
                locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
    } else {
        tblStandings.setRowSelectionAllowed(true);
        tblStandings.clearSelection();
        tblStandings.addRowSelectionInterval(rowNumber, rowNumber);

        Rectangle rect = tblStandings.getCellRect(rowNumber, 0, true);
        tblStandings.scrollRectToVisible(rect);
    }

    tblStandings.repaint();
}

private void mniImportH9ActionPerformed(java.awt.event.ActionEvent evt) {
    this.importPlainFile("h9");
}

    /**
     * imports players and games from a plain file
     *
     * @param importType either "h9", "tou", or "wallist"
     */
    private void importPlainFile(String importType) {
        if (tournament == null) {
            JOptionPane.showMessageDialog(this, locale.getString("error.no_open_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        String strExt = "txt";
        if (importType.equals("tou")) {
            strExt = "tou";
        } else if (importType.equals("h9")) {
            strExt = "h9";
        }
        File f = chooseAFile(Gotha.runningDirectory, strExt);
        if (f == null) {
            return;
        }
        ArrayList<Player> alPlayers = new ArrayList<Player>();
        ArrayList<Game> alGames = new ArrayList<Game>();
        try {
            ExternalDocument.importPlayersAndGamesFromPlainFile(f, importType, alPlayers, alGames);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, locale.format("error.import_error", f.getName()), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        int nbErrors = 0;
        for (Player p : alPlayers) {
            try {
                tournament.addPlayer(p);
            } catch (RemoteException ex) {
                Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TournamentException te) {
                nbErrors++;
                if (nbErrors <= 3) {
                    JOptionPane.showMessageDialog(this, te.getMessage(), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
                }
                if (nbErrors == 4) {
                    JOptionPane.showMessageDialog(this, locale.getString("error.import_more_than_3_errors"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        if (nbErrors > 0) {
            JOptionPane.showMessageDialog(this, locale.getString("error.games_not_imported_because_errors_on_players"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
        } else {
            for (Game g : alGames) {
                try {
                    tournament.addGame(g);
                } catch (RemoteException ex) {
                    Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
                } catch (TournamentException ex) {
                    Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        try {
            tournament.updateNumberOfRoundsIfNecesary();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.tournamentChanged();
    }

    /**
     * imports players from a vBar separated file
     */
    private void importVBSFile() {
        if (tournament == null) {
            JOptionPane.showMessageDialog(this, locale.getString("error.no_open_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        String strExt = "txt";
        File f = chooseAFile(Gotha.runningDirectory, strExt);
        if (f == null) {
            return;
        }
        ArrayList<Player> alPlayers = new ArrayList<Player>();
        ArrayList<Game> alGames = new ArrayList<Game>();
        try {
            ExternalDocument.importPlayersFromVBSFile(f, alPlayers);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, locale.format("error.import_error", f.getName()), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        int nbErrors = 0;
        for (Player p : alPlayers) {
            try {
                tournament.addPlayer(p);
            } catch (RemoteException ex) {
                Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TournamentException te) {
                nbErrors++;
                if (nbErrors <= 3) {
                    JOptionPane.showMessageDialog(this, te.getMessage(), "Message", JOptionPane.ERROR_MESSAGE);
                }
                if (nbErrors == 4) {
                    JOptionPane.showMessageDialog(this, locale.getString("error.import_more_than_3_errors"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        if (nbErrors > 0) {
            JOptionPane.showMessageDialog(this, locale.getString("error.games_not_imported_because_errors_on_players"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
        }

        this.tournamentChanged();
    }

private void mniPreferencesActionPerformed(java.awt.event.ActionEvent evt) {
    JFrame jfr = new JFrPreferencesOptions();
    this.displayFrame(jfr, MEDIUM_FRAME_WIDTH, MEDIUM_FRAME_HEIGHT);
}

private void mniImportWallistActionPerformed(java.awt.event.ActionEvent evt) {
    this.importPlainFile("wallist");
}

private void mniTeamsManagerActionPerformed(java.awt.event.ActionEvent evt) {
    if (tournament == null) {
        JOptionPane.showMessageDialog(this, locale.getString("error.no_open_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
        return;
    }
    try {
        JFrame jfr = new JFrTeamsManager(tournament);
        this.displayFrame(jfr, MEDIUM_FRAME_WIDTH, MEDIUM_FRAME_HEIGHT);
    } catch (RemoteException ex) {
        Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
    }
}

private void rdbCurrentTeamPSActionPerformed(java.awt.event.ActionEvent evt) {
    updateAllViews();
}

private void rdbTemporaryTeamPSActionPerformed(java.awt.event.ActionEvent evt) {
    updateAllViews();
}

private void spnTeamRoundNumberStateChanged(javax.swing.event.ChangeEvent evt) {
    int demandedTeamRN = (Integer) (spnTeamRoundNumber.getValue()) - 1;
    this.demandedDisplayedTeamRoundNumberHasChanged(demandedTeamRN);

}

private void cbxTeamCritActionPerformed(java.awt.event.ActionEvent evt) {
    // In order to avoid useless updates, ...
    if (!this.cbxTeamCrit1.isEnabled()) {
        return;
    }

    try {
        updateDisplayTeamCriteria();
        updateTeamsStandingsComponents();
    } catch (RemoteException ex) {
        Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
    }
}

private void mniTeamsPairingActionPerformed(java.awt.event.ActionEvent evt) {
    if (tournament == null) {
        JOptionPane.showMessageDialog(this, locale.getString("error.no_open_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
        return;
    }
    try {
        JFrame jfr = new JFrTeamsPairing(tournament);
        this.displayFrame(jfr, BIG_FRAME_WIDTH, BIG_FRAME_HEIGHT);
    } catch (RemoteException ex) {
        Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
    }

}

private void btnPrintTeamsStandingsActionPerformed(java.awt.event.ActionEvent evt) {
    try {
        TournamentParameterSet printedTPS = tournament.getTournamentParameterSet();
        TeamTournamentParameterSet ttps = tournament.getTeamTournamentParameterSet();
        TeamTournamentParameterSet printedTeamTPS = new TeamTournamentParameterSet(ttps);
        TeamPlacementParameterSet printedTeamPPS = printedTeamTPS.getTeamPlacementParameterSet();
        printedTeamPPS.setPlaCriteria(displayedTeamCriteria);
        TournamentPrinting.printTeamsStandings(tournament, printedTPS, printedTeamTPS, this.displayedTeamRoundNumber);
    } catch (RemoteException ex) {
        Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
    }
}


private void mniOpenActionPerformed(java.awt.event.ActionEvent evt) {
    File f = chooseAFile(new File(Gotha.runningDirectory, "tournamentfiles"), "xml");
    if (f == null) {
        return;
    }
    try {
        openTournament(f);
        // update Preferences
        this.addRecentTournament(f.getAbsolutePath());
    } catch (FileNotFoundException ex) {
        JOptionPane.showMessageDialog(this, locale.getString("error.file_not_found"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
    } catch (Exception ex) {
        String strMessage = locale.format("error.file_reading_error", f.getName(), Gotha.GOTHA_DATA_VERSION);
        JOptionPane.showMessageDialog(JFrGotha.this, strMessage, locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
        JFrGotha.this.removeAllRecentTournament();
    }

}

private void mniImportVBSActionPerformed(java.awt.event.ActionEvent evt) {
    this.importVBSFile();
}

private void formWindowClosing(java.awt.event.WindowEvent evt) {
    exitOpenGotha();
}

private void mniUpdateRatingsActionPerformed(java.awt.event.ActionEvent evt) {
    if (tournament == null) {
        JOptionPane.showMessageDialog(this, locale.getString("error.no_open_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
        return;
    }

    try {
        JFrame jfr = new JFrUpdateRatings(tournament);
        displayFrame(jfr, JFrGotha.MEDIUM_FRAME_WIDTH, JFrGotha.MEDIUM_FRAME_HEIGHT);
    } catch (RemoteException ex) {
        Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
    }
}

private void mniExperimentalToolsActionPerformed(java.awt.event.ActionEvent evt) {
    JFrame jfr = null;
    try {
        jfr = new JFrExperimentalTools(tournament);
        this.displayFrame(jfr, MEDIUM_FRAME_WIDTH, MEDIUM_FRAME_HEIGHT);
    } catch (RemoteException ex) {
        Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
    }
}

private void mniMemoryActionPerformed(java.awt.event.ActionEvent evt) {
    JFrame jfr = new JFrToolsMemory();
    this.displayFrame(jfr, SMALL_FRAME_WIDTH, SMALL_FRAME_HEIGHT);
}

    private void mniDiscardRoundsActionPerformed(java.awt.event.ActionEvent evt) {
        if (tournament == null) {
            JOptionPane.showMessageDialog(this, locale.getString("error.no_open_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            JFrame jfr = new JFrDiscardRounds(tournament);
            this.displayFrame(jfr, SMALL_FRAME_WIDTH, SMALL_FRAME_HEIGHT);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void mniSaveACopyActionPerformed(java.awt.event.ActionEvent evt) {
        if (tournament == null) {
            JOptionPane.showMessageDialog(this, locale.getString("error.no_open_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        Preferences prefsRoot = Preferences.userRoot();
        Preferences gothaPrefs = prefsRoot.node(Gotha.strPreferences);
        String strTC = gothaPrefs.get("tournamentCopy", "");
        if (strTC.length() < 1){
            String shortName = "shortName";
            try {
                shortName = tournament.getShortName();
            } catch (RemoteException ex) {
                Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
            }
            File tcFile = new File(Gotha.runningDirectory + "/tournamentfiles/copies", shortName + "_Copy.xml");
            strTC = "" + tcFile;
        }
            
        File f = this.chooseASaveFile(strTC);
        if (f == null) return;
        this.updateShortNameFromFile(f);
        saveTournament(f);
        gothaPrefs.put("tournamentCopy", "" + f);
     
    }

    private void mniPublishActionPerformed(java.awt.event.ActionEvent evt) {
        if (tournament == null) {
            JOptionPane.showMessageDialog(this, locale.getString("error.no_open_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            JFrame jfr = new JFrPublish(tournament);
        this.displayFrame(jfr, MEDIUM_FRAME_WIDTH, MEDIUM_FRAME_HEIGHT);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void mniExportActionPerformed(java.awt.event.ActionEvent evt) {
        JOptionPane.showMessageDialog(this, locale.getString("error.html_exports_in_publish_menu"), locale.getString("alert.message"), JOptionPane.INFORMATION_MESSAGE);
    }

    private void mniSaveActionPerformed(java.awt.event.ActionEvent evt) {
        if (tournament == null) {
            JOptionPane.showMessageDialog(this, locale.getString("error.no_open_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        File f =  new File (this.getDefaultSaveAsFileName());
        
        updateShortNameFromFile(f);
        
        // Make actual save
        this.saveTournament(f);
                
       try {
            tournament.setChangeSinceLastSaveAsFalse();
            tournament.setHasBeenSavedOnce(true);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.addRecentTournament("" + f);
        
        this.tournamentChanged();

    }

    private void mniImportRgfActionPerformed(java.awt.event.ActionEvent evt) {
        RgfTournamentImportDialog importPane = new RgfTournamentImportDialog(this, locale.getString("tournament.rgf.import.window_title"), true, this);
        importPane.setVisible(true);
    }

    private void mniPublishRGFActionPerformed(java.awt.event.ActionEvent evt) {
        if (null == tournament) {
            JOptionPane.showMessageDialog(this, locale.getString("error.no_open_tournament"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (tournament.teamsList().size() > 0) {
                JOptionPane.showMessageDialog(this, locale.getString("tournament.rgf.publish.teams_not_implemented"), locale.getString("alert.message"), JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (RemoteException e) {
            // TODO
            return;
        }

        RgfTournamentExportDialog exportPane = new RgfTournamentExportDialog(this, locale.getString("tournament.rgf.publish.window_title"), true, tournament);
        exportPane.setVisible(true);
    }

    private File chooseAFile(File path, String extension) {
        JFileChooser fileChoice = new JFileChooser(path);
        fileChoice.setFileSelectionMode(JFileChooser.FILES_ONLY);
        MyFileFilter mff = new MyFileFilter(new String[]{extension}, "*." + extension);
        fileChoice.addChoosableFileFilter(mff);
        int result = fileChoice.showOpenDialog(this);
        if (result == JFileChooser.CANCEL_OPTION) {
            return null;
        } else {
            return fileChoice.getSelectedFile();
        }
    }

    private void openTournament(File f) throws Exception {
        LogElements.incrementElement("tournament.open", f.getName());
        if (!saveCurrentTournamentIfNecessary()) {
            return;
        }
        
        // check whether a more recent work file has been saved for this tournament
        // Is there a work tournament ?

        String strDir = Gotha.runningDirectory + "/tournamentfiles";
        String strNE = f.getName();
        String strFN = strNE.substring(0, strNE.indexOf("."));
        String strWorkNE= strFN + "_work.xml";
        String strDNE = strDir + "/" + strWorkNE;
        File fW = new File(strDNE);
        
        long timeF = f.lastModified();
        long timeFW = fW.lastModified();
        if (timeFW - timeF > 2 * JFrGotha.REFRESH_DELAY){
            String strMes = locale.getString("error.confirm_recover_work_file");
            int rep = JOptionPane.showConfirmDialog(this, locale.getString("error.confirm_recover_work_file"), locale.format("error.confirm_recover", strNE), JOptionPane.OK_CANCEL_OPTION);
            if (rep == JOptionPane.OK_OPTION) Files.copy(fW.toPath(), f.toPath(), REPLACE_EXISTING);  
            else Files.delete(fW.toPath());
        }
        
        
        TournamentInterface t = Gotha.getTournamentFromFile(f);
        if (t == null) {
            String strMessage = "Some problem occured with file : " + f.getName();
            strMessage += "\nThe corresponding tournament has not been opened";
            System.out.println(strMessage);
            return;
        }

        openTournament(t);
    }

    @Override
    public void openTournament(TournamentInterface t) throws RemoteException {
        // Check if a tournament with same name exists (Server mode only)
        if (Gotha.runningMode == Gotha.RUNNING_MODE_SRV) {
            String tKN = null;
            try {
                tKN = t.getShortName();
            } catch (RemoteException ex) {
                Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
            }
            TournamentInterface oldT = GothaRMIServer.getTournament(tKN);
            if (oldT != null) {
                String strMessage = locale.format("error.tournament_already_opened_on_server", tKN);
                JOptionPane.showMessageDialog(this, strMessage, locale.getString("alert.message"), JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        if (tournament != null) {
            closeTournament();
        }
        tournament = t;
        tournament.setChangeSinceLastSaveAsFalse();
        tournament.setHasBeenSavedOnce(true);

        // If we are in Server mode, then worry about adding it to registry
        if (Gotha.runningMode == Gotha.RUNNING_MODE_SRV) {
            GothaRMIServer.addTournament(tournament);
        }

        try {
            this.displayedRoundNumber = tournament.presumablyCurrentRoundNumber();
            this.tournamentChanged();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void closeTournament() {
        if (tournament == null) {
            return;
        }
        try {
            tournament.close();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.tournamentChanged();
        tournament = null;
        this.tournamentChanged();
        
        Preferences prefsRoot = Preferences.userRoot();
        Preferences gothaPrefs = prefsRoot.node(Gotha.strPreferences);
        gothaPrefs.put("tournamentCopy", "");
    }

    private void tournamentChanged() {
        updateTitle();
        if (tournament == null) {
            try {
                this.updateDisplayCriteria();
                this.updateStandingsComponents();
                this.updateControlPanel();
            } catch (RemoteException ex) {
                Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
        }
        try {
            tournament.setLastTournamentModificationTime(tournament.getCurrentTournamentTime());
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }

        updateAllViews();
    }

    private void updateAllViews() {
        if (tournament != null) {
            try {
                if (Gotha.runningMode == Gotha.RUNNING_MODE_CLI && !tournament.isOpen()) {
                    dispose();
                }

                this.lastComponentsUpdateTime = tournament.getCurrentTournamentTime();
                if (tournament.tournamentType() == TournamentParameterSet.TYPE_MCMAHON) {
                    this.mniMMGroups.setEnabled(true);
                } else {
                    this.mniMMGroups.setEnabled(false);
                }
            } catch (RemoteException ex) {
                Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        updateTitle();

        int idxTP = tpnGotha.indexOfComponent(pnlTeamsPanel);
        int idxTS = tpnGotha.indexOfComponent(pnlTeamsStandings);
        if (idxTP > 0) {
            this.tpnGotha.setEnabledAt(idxTP, false);
        }
        if (idxTS > 0) {
            this.tpnGotha.setEnabledAt(idxTS, false);
        }

        try {
            if (tournament != null && !tournament.teamsList().isEmpty()) {
                if (idxTP > 0) {
                    this.tpnGotha.setEnabledAt(idxTP, true);
                }
                if (idxTS > 0) {
                    this.tpnGotha.setEnabledAt(idxTS, true);
                }
            }
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (this.tpnGotha.getSelectedComponent() == pnlWelcome) {
            try {
                this.updateWelcomePanel();
            } catch (RemoteException ex) {
                Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (this.tpnGotha.getSelectedComponent() == pnlStandings) {
            try {
                this.updateDisplayCriteria();
                this.updateStandingsComponents();

            } catch (RemoteException ex) {
                Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (this.tpnGotha.getSelectedComponent() == pnlControlPanel) {
            try {
                this.updateControlPanel();
            } catch (RemoteException ex) {
                Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (this.tpnGotha.getSelectedComponent() == pnlTeamsPanel) {
            try {
                this.updateTeamsPanel();
            } catch (RemoteException ex) {
                Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (this.tpnGotha.getSelectedComponent() == this.pnlTeamsStandings) {
            try {
                this.updateDisplayTeamCriteria();
                this.updateTeamsStandingsComponents();
            } catch (RemoteException ex) {
                Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }

    private void demandedDisplayedRoundNumberHasChanged(int demandedRN) {
        int numberOfRounds = 0;
        try {
            numberOfRounds = tournament.getTournamentParameterSet().getGeneralParameterSet().getNumberOfRounds();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (demandedRN < 0 || demandedRN >= numberOfRounds) {
            spnRoundNumber.setValue(displayedRoundNumber + 1);
            return;
        }
        if (demandedRN == displayedRoundNumber) {
            return;
        }

        displayedRoundNumber = demandedRN;
        try {
            updateStandingsComponents();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void demandedDisplayedTeamRoundNumberHasChanged(int demandedRN) {
        int numberOfRounds = 0;
        try {
            numberOfRounds = tournament.getTournamentParameterSet().getGeneralParameterSet().getNumberOfRounds();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (demandedRN < 0 || demandedRN >= numberOfRounds) {
            spnTeamRoundNumber.setValue(displayedTeamRoundNumber + 1);
            return;
        }
        if (demandedRN == displayedTeamRoundNumber) {
            return;
        }

        displayedTeamRoundNumber = demandedRN;
        try {
            updateTeamsStandingsComponents();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGotha.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void exitOpenGotha() {
        if (!saveCurrentTournamentIfNecessary()) {
            return;
        }
        if (Gotha.isJournalingReportEnabled()) {
            LogElements.sendLogElements();
        }
        System.exit(0);
    }

    /**
     * formats column and cell of a given JTable. If a specific TableRenderer
     * class is used for this table, the behaviour of formatColumn will be
     * overridden by the specific method This method
     *
     * @param tbl Jtable
     * @param col column number
     * @param str header String
     * @param width comumn width
     * @param bodyAlign horizontal alignment for column cells
     * @param headerAlign horizontal alignment for haeder
     */
    public static void formatColumn(JTable tbl, int col, String str, int width, int bodyAlign, int headerAlign) {
        formatHeader(tbl, col, str, headerAlign);
        formatColumnBody(tbl, col, width, bodyAlign);
    }

    public static void formatHeader(JTable tbl, int col, String str, int align) {
        JTableHeader th = tbl.getTableHeader();
        th.getColumnModel().getColumn(col).setHeaderValue(str);
        th.repaint();
        TableColumn tc = tbl.getColumnModel().getColumn(col);
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        dtcr.setBackground(Color.LIGHT_GRAY);
        dtcr.setHorizontalAlignment(align);
        tc.setHeaderRenderer(dtcr);
    }

    private static void formatColumnBody(JTable tbl, int col, int width, int align) {
        TableColumnModel tcm = tbl.getColumnModel();

        tcm.getColumn(col).setPreferredWidth(width);
        
        // Alignment
        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
        dtcr.setHorizontalAlignment(align);
        tcm.getColumn(col).setCellRenderer(dtcr);
    }
    
    private void displayFrame(Window win, int w, int h){
        Rectangle newRect = this.getBounds();
        win.setLocation(newRect.x + 10, newRect.y + 60);
        win.setSize(w, h);
        win.setVisible(true);
        win.setIconImage(Gotha.getIconImage());
    }

    private javax.swing.JButton btnDlgImportXMLCancel;
    private javax.swing.JButton btnDlgImportXMLOK;
    private javax.swing.JButton btnDlgNewCancel;
    private javax.swing.JButton btnDlgNewOK;
    private javax.swing.JButton btnHelp;
    private javax.swing.JButton btnPrintStandings;
    private javax.swing.JButton btnPrintTeamsStandings;
    private javax.swing.JButton btnSearch;
    private javax.swing.JComboBox<String> cbxCrit1;
    private javax.swing.JComboBox<String> cbxCrit2;
    private javax.swing.JComboBox<String> cbxCrit3;
    private javax.swing.JComboBox<String> cbxCrit4;
    private javax.swing.JComboBox<String> cbxTeamCrit1;
    private javax.swing.JComboBox<String> cbxTeamCrit2;
    private javax.swing.JComboBox<String> cbxTeamCrit3;
    private javax.swing.JComboBox<String> cbxTeamCrit4;
    private javax.swing.JComboBox<String> cbxTeamCrit5;
    private javax.swing.JComboBox<String> cbxTeamCrit6;
    private javax.swing.JCheckBox chkClubsGroups;
    private javax.swing.JCheckBox chkGames;
    private javax.swing.JCheckBox chkPlayers;
    private javax.swing.JCheckBox chkTeams;
    private javax.swing.JCheckBox chkTournamentParameters;
    private javax.swing.JDialog dlgImportXML;
    private javax.swing.JDialog dlgNew;
    private javax.swing.ButtonGroup grpPS;
    private javax.swing.ButtonGroup grpSystem;
    private javax.swing.ButtonGroup grpTeamPS;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JLabel lblFlowChart;
    private javax.swing.JLabel lblRecommended;
    private javax.swing.JLabel lblStandingsAfter;
    private javax.swing.JLabel lblTeamUpdateTime;
    private javax.swing.JLabel lblTeamsStandingsAfter;
    private javax.swing.JLabel lblTournamentPicture;
    private javax.swing.JLabel lblUpdateTime;
    private javax.swing.JLabel lblWarningPRE;
    private javax.swing.JMenuItem mniBuildTestTournament;
    private javax.swing.JMenuItem mniClose;
    private javax.swing.JMenuItem mniDiscardRounds;
    private javax.swing.JMenuItem mniExit;
    private javax.swing.JMenuItem mniExperimentalTools;
    private javax.swing.JMenuItem mniExport;
    private javax.swing.JMenuItem mniGamesOptions;
    private javax.swing.JMenuItem mniHelpAbout;
    private javax.swing.JMenuItem mniImportH9;
    private javax.swing.JMenuItem mniImportRgf;
    private javax.swing.JMenuItem mniImportTou;
    private javax.swing.JMenuItem mniImportVBS;
    private javax.swing.JMenuItem mniImportWallist;
    private javax.swing.JMenuItem mniImportXML;
    private javax.swing.JMenuItem mniMMGroups;
    private javax.swing.JMenuItem mniMemory;
    private javax.swing.JMenuItem mniNew;
    private javax.swing.JMenuItem mniOpen;
    private javax.swing.JMenuItem mniOpenGothaHelp;
    private javax.swing.JMenuItem mniPair;
    private javax.swing.JMenuItem mniPlayersManager;
    private javax.swing.JMenuItem mniPlayersQuickCheck;
    private javax.swing.JMenuItem mniPreferences;
    private javax.swing.JMenuItem mniPublish;
    private javax.swing.JMenuItem mniPublishRGF;
    private javax.swing.JMenuItem mniRMI;
    private javax.swing.JMenuItem mniRR;
    private javax.swing.JMenuItem mniResults;
    private javax.swing.JMenuItem mniSave;
    private javax.swing.JMenuItem mniSaveACopy;
    private javax.swing.JMenuItem mniSaveAs;
    private javax.swing.JMenuItem mniTeamsManager;
    private javax.swing.JMenuItem mniTeamsPairing;
    private javax.swing.JMenuItem mniTournamentOptions;
    private javax.swing.JMenuItem mniUpdateRatings;
    private javax.swing.JMenu mnuGames;
    private javax.swing.JMenu mnuHelp;
    private javax.swing.JMenu mnuImport;
    private javax.swing.JMenuBar mnuMain;
    private javax.swing.JMenu mnuOpenRecent;
    private javax.swing.JMenu mnuOptions;
    private javax.swing.JMenu mnuPlayers;
    private javax.swing.JMenu mnuPublish;
    private javax.swing.JMenu mnuTools;
    private javax.swing.JMenu mnuTournament;
    private javax.swing.JPanel pnlControlPanel;
    private javax.swing.JPanel pnlObjectsToImport;
    private javax.swing.JPanel pnlPS;
    private javax.swing.JPanel pnlStandings;
    private javax.swing.JPanel pnlSystem;
    private javax.swing.JPanel pnlTeamPS;
    private javax.swing.JPanel pnlTeamsPanel;
    private javax.swing.JPanel pnlTeamsStandings;
    private javax.swing.JPanel pnlTournamentDetails;
    private javax.swing.JPanel pnlWelcome;
    private javax.swing.JRadioButton rdbCurrentPS;
    private javax.swing.JRadioButton rdbCurrentTeamPS;
    private javax.swing.JRadioButton rdbMcMahon;
    private javax.swing.JRadioButton rdbSwiss;
    private javax.swing.JRadioButton rdbSwissCat;
    private javax.swing.JRadioButton rdbTemporaryPS;
    private javax.swing.JRadioButton rdbTemporaryTeamPS;
    private javax.swing.JScrollPane scpControlPanel;
    private javax.swing.JScrollPane scpStandings;
    private javax.swing.JScrollPane scpTeamsPanel;
    private javax.swing.JScrollPane scpTeamsStandings;
    private javax.swing.JSpinner spnRoundNumber;
    private javax.swing.JSpinner spnTeamRoundNumber;
    private javax.swing.JTable tblControlPanel;
    private javax.swing.JTable tblStandings;
    private javax.swing.JTable tblTeamsPanel;
    private javax.swing.JTable tblTeamsStandings;
    private javax.swing.JTabbedPane tpnGotha;
    private javax.swing.JTextField txfBeginDate;
    private javax.swing.JTextField txfDirector;
    private javax.swing.JTextField txfEndDate;
    private javax.swing.JTextField txfLocation;
    private javax.swing.JTextField txfName;
    private javax.swing.JTextField txfNumberOfRounds;
    private javax.swing.JTextField txfSearchPlayer;
    private javax.swing.JTextField txfShortName;
}

class MyFileFilter extends FileFilter {

    String[] suffixes;
    String description;

    public MyFileFilter(String[] suffixes, String description) {
        for (int i = 0; i < suffixes.length; i++) {
            this.suffixes = suffixes;
            this.description = description;
        }
    }

    boolean belongs(String suffix) {
        for (int i = 0; i < suffixes.length; i++) {
            if (suffix.equals(suffixes[i])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String suffix = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 && i < s.length() - 1) {
            suffix = s.substring(i + 1).toLowerCase();
        }
        return suffix != null && belongs(suffix);
    }

    @Override
    public String getDescription() {
        return description;
    }
}

class StandingsTableCellRenderer extends JLabel implements TableCellRenderer {
    // This method is called each time a cell in a column
    // using this renderer needs to be rendered.

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int rowIndex, int colIndex) {
        TableModel model = table.getModel();
        setText("" + model.getValueAt(rowIndex, colIndex));
        if (isSelected) {
            setFont(this.getFont().deriveFont(Font.BOLD));
        } else {
            setFont(this.getFont().deriveFont(Font.PLAIN));
        }
        return this;
    }
}

class ControlPanelTableCellRenderer extends JLabel implements TableCellRenderer {
    // Assigned players will be in column 2; Entered results will be in column 3;

    protected boolean[][] cpWarning = new boolean[Gotha.MAX_NUMBER_OF_ROUNDS][4];

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int rowIndex, int colIndex) {
        TableModel model = table.getModel();
        setText("" + model.getValueAt(rowIndex, colIndex));
        if (cpWarning[rowIndex][colIndex]) {
            this.setForeground(Color.red);
        } else {
            setForeground(Color.black);
        }
        this.setHorizontalAlignment(JLabel.CENTER);
        if (colIndex == 0) { //
            this.setHorizontalAlignment(JLabel.RIGHT);
        }
        return this;
    }
}

class TeamsPanelTableCellRenderer extends JLabel implements TableCellRenderer {
    // Assigned players will be in column 2; Entered results will be in column 3;

    private Font defaultFont = this.getFont();
    protected boolean[][] cpWarning = new boolean[Gotha.MAX_NUMBER_OF_ROUNDS][4];

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int rowIndex, int colIndex) {
        TableModel model = table.getModel();
        setText("" + model.getValueAt(rowIndex, colIndex));
        this.setHorizontalAlignment(JLabel.CENTER);
        if (colIndex == JFrGotha.TM_TEAM_NUMBER_COL
                || colIndex == JFrGotha.TM_BOARD_NUMBER_COL) { //
            this.setHorizontalAlignment(JLabel.RIGHT);
        }
        if (colIndex == JFrGotha.TM_TEAM_NAME_COL
                || colIndex == JFrGotha.TM_PL_NAME_COL) { //
            this.setHorizontalAlignment(JLabel.LEFT);
        }
        if (colIndex == JFrGotha.TM_PL_ROUNDS_COL) { //
            Font f = new Font("Courier New", Font.BOLD, 16);
            setFont(f);
        } else {
//          setFont(this.getFont().deriveFont(12.0F));
            setFont(defaultFont);

        }

        return this;
    }
}
