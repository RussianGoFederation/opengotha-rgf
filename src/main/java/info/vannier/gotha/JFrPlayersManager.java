/*
 * JFrPlayersManager.java
 */
package info.vannier.gotha;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import ru.gofederation.gotha.model.RatingListFactory;
import ru.gofederation.gotha.model.RatingListType;
import ru.gofederation.gotha.ui.PlayerEditor;
import ru.gofederation.gotha.ui.PlayerList;
import ru.gofederation.gotha.ui.RatingListControls;
import ru.gofederation.gotha.ui.SmmsByHand;
import ru.gofederation.gotha.util.GothaLocale;

import static ru.gofederation.gotha.model.PlayerRegistrationStatus.FINAL;
import static ru.gofederation.gotha.model.PlayerRegistrationStatus.PRELIMINARY;

/**
 *
 * @author  Luc Vannier
 */
public class JFrPlayersManager extends javax.swing.JFrame implements RatingListControls.Listener, PlayerList.PlayerDoubleClickListener, PlayerEditor.Listener {
    private static final long REFRESH_DELAY = 2000;
    private long lastComponentsUpdateTime = 0;
    private int playersSortType = PlayerComparator.NAME_ORDER;
    private final static int PLAYER_MODE_NEW = 1;
    private final static int PLAYER_MODE_MODIF = 2;
    private int playerMode = PLAYER_MODE_NEW;
    private Player playerInModification = null;
    private static final int REG_COL = 0;
    private static final int NAME_COL = 1;
    private static final int FIRSTNAME_COL = 2;
    private static final int COUNTRY_COL = 3;
    private static final int CLUB_COL = 4;
    private static final int RANK_COL = 5;
    private static final int RATING_COL = 6;
    private static final int GRADE_COL = 7;
    /**  current Tournament */
    private TournamentInterface tournament;
    /** Rating List */
    private RatingList ratingList = new RatingList();

    private final PlayerEditor playerEditor;

    private GothaLocale locale;

    /**
     * Creates new form JFrPlayersManager
     */
    public JFrPlayersManager(TournamentInterface tournament) throws RemoteException {
        this.locale = GothaLocale.getCurrentLocale();
        this.tournament = tournament;
        this.playerEditor = new PlayerEditor(tournament, this);

        initComponents();
        customInitComponents();
        setupRefreshTimer();

        playerList.setTournament(tournament);
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

    /** This method is called from within the constructor to
     * initialize the form.
     * Unlike initComponents, customInitComponents is editable
     */
    private void customInitComponents() throws RemoteException {
        pnlPlayer.add(playerEditor);
        playerEditor.setBounds(8, 230, 480, 320);

        ratingListControls.addListener(this);
        playerList.setPlayerDoubleClickListener(this);
        playerList.addContextMenuItem(locale.getString("player.menu.remove"), (this::removePlayer));
        playerList.addContextMenuItem(locale.getString("player.menu.modify"), (this::onPlayerDoubleClicked));

        AutoCompletion.enable(cbxRatingList);

        getRootPane().setDefaultButton(btnRegister);

        resetRatingListControls();
        this.playerEditor.resetForm();

        this.updateAllViews();
        onRatingListSelected(ratingListControls.getSelectedRatingListType());
    }

    private void updatePnlRegisteredPlayers(ArrayList<Player> playersList) {
        int nbPreliminary = 0;
        int nbFinal = 0;
        for (Player p : playersList) {
            if (p.getRegisteringStatus() == PRELIMINARY) {
                nbPreliminary++;
            }
            if (p.getRegisteringStatus() == FINAL) {
                nbFinal++;
            }
        }
        txfNbPlPre.setText("" + nbPreliminary);
        txfNbPlFin.setText("" + nbFinal);
        lblPlPre.setText(locale.format("player.players.registered_preliminary", nbPreliminary));
        lblPlFin.setText(locale.format("player.players.registered_final", nbFinal));
    }

    private void resetRatingListControls() {
        this.rdbRankFromGoR.setVisible(false);
        this.rdbRankFromGrade.setVisible(false);

        if (ratingList.getRatingListType() == RatingListType.EGF) {
            this.rdbRankFromGoR.setVisible(true);
            this.rdbRankFromGrade.setVisible(true);
        }
        if (ratingList.getRatingListType() == RatingListType.FFG) {
            this.rdbRankFromGoR.setSelected(true);
        }
        if (ratingList.getRatingListType() == RatingListType.AGA) {
            this.rdbRankFromGoR.setSelected(true);
        }

        if (ratingList.getRatingListType() == RatingListType.UND) {
            cbxRatingList.setEnabled(false);
            cbxRatingList.setVisible(true);
            txfPlayerNameChoice.setEnabled(false);
            txfPlayerNameChoice.setVisible(false);
            scpPlayerNameChoice.setEnabled(false);
            scpPlayerNameChoice.setVisible(false);
            lstPlayerNameChoice.setEnabled(false);
            lstPlayerNameChoice.setVisible(false);
        } else {
            if (rdbFirstCharacters.isSelected()) {
                resetControlsForFirstCharactersSearching();
            } else {
                resetControlsForLevenshteinSearching();
            }

        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {

        grpAlgo = new javax.swing.ButtonGroup();
        grpSetRank = new javax.swing.ButtonGroup();
        pnlPlayer = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        btnRegister = new javax.swing.JButton();
        lblRatingList = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        rdbFirstCharacters = new javax.swing.JRadioButton();
        rdbLevenshtein = new javax.swing.JRadioButton();
        cbxRatingList = new javax.swing.JComboBox<>();
        txfPlayerNameChoice = new java.awt.TextField();
        scpPlayerNameChoice = new javax.swing.JScrollPane();
        lstPlayerNameChoice = new javax.swing.JList<>();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        rdbRankFromGoR = new javax.swing.JRadioButton();
        rdbRankFromGrade = new javax.swing.JRadioButton();
        btnSearchId = new javax.swing.JButton();
        txfSearchId = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        ratingListControls = new ru.gofederation.gotha.ui.RatingListControls();
        jLabel14 = new javax.swing.JLabel();
        pnlPlayersList = new javax.swing.JPanel();
        lblPlFin = new javax.swing.JLabel();
        lblPlPre = new javax.swing.JLabel();
        txfNbPlFin = new javax.swing.JTextField();
        txfNbPlPre = new javax.swing.JTextField();
        btnPrint = new javax.swing.JButton();
        playerList = new ru.gofederation.gotha.ui.PlayerList();
        btnClose = new javax.swing.JButton();
        btnHelp = new javax.swing.JButton();
        btnSmms = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Players Manager");
        setIconImage(getIconImage());
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });
        getContentPane().setLayout(null);

        pnlPlayer.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("player"))); // NOI18N
        pnlPlayer.setLayout(null);

        lblRatingList.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        lblRatingList.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblRatingList.setText("No rating list has been loaded yet");
        pnlPlayer.add(lblRatingList);
        lblRatingList.setBounds(260, 10, 220, 14);

        grpAlgo.add(rdbFirstCharacters);
        rdbFirstCharacters.setSelected(true);
        rdbFirstCharacters.setText("Compare first characters");
        rdbFirstCharacters.setEnabled(false);
        rdbFirstCharacters.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbFirstCharactersActionPerformed(evt);
            }
        });
        pnlPlayer.add(rdbFirstCharacters);
        rdbFirstCharacters.setBounds(20, 140, 220, 20);

        grpAlgo.add(rdbLevenshtein);
        rdbLevenshtein.setText("Use Levenshtein algorithm");
        rdbLevenshtein.setEnabled(false);
        rdbLevenshtein.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbLevenshteinActionPerformed(evt);
            }
        });
        pnlPlayer.add(rdbLevenshtein);
        rdbLevenshtein.setBounds(20, 160, 220, 20);

        cbxRatingList.setMaximumRowCount(9);
        cbxRatingList.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "" }));
        cbxRatingList.setToolTipText("");
        cbxRatingList.setEnabled(false);
        cbxRatingList.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxRatingListItemStateChanged(evt);
            }
        });
        pnlPlayer.add(cbxRatingList);
        cbxRatingList.setBounds(260, 30, 220, 20);

        txfPlayerNameChoice.setText("Enter approximate name and firstname");
        txfPlayerNameChoice.addTextListener(new java.awt.event.TextListener() {
            public void textValueChanged(java.awt.event.TextEvent evt) {
                txfPlayerNameChoiceTextValueChanged(evt);
            }
        });
        txfPlayerNameChoice.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txfPlayerNameChoiceKeyPressed(evt);
            }
        });
        pnlPlayer.add(txfPlayerNameChoice);
        txfPlayerNameChoice.setBounds(260, 30, 220, 30);

        lstPlayerNameChoice.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstPlayerNameChoiceValueChanged(evt);
            }
        });
        scpPlayerNameChoice.setViewportView(lstPlayerNameChoice);

        pnlPlayer.add(scpPlayerNameChoice);
        scpPlayerNameChoice.setBounds(260, 60, 220, 160);

        grpSetRank.add(rdbRankFromGoR);
        rdbRankFromGoR.setSelected(true);
        rdbRankFromGoR.setText("set Rank from rating (GoR)");
        rdbRankFromGoR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbRankFromGoRActionPerformed(evt);
            }
        });
        pnlPlayer.add(rdbRankFromGoR);
        rdbRankFromGoR.setBounds(20, 185, 220, 20);

        grpSetRank.add(rdbRankFromGrade);
        rdbRankFromGrade.setText("set Rank from Grade");
        rdbRankFromGrade.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbRankFromGradeActionPerformed(evt);
            }
        });
        pnlPlayer.add(rdbRankFromGrade);
        rdbRankFromGrade.setBounds(20, 205, 220, 20);

        btnSearchId.setText("Search by Id");
        btnSearchId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchIdActionPerformed(evt);
            }
        });
        pnlPlayer.add(btnSearchId);
        btnSearchId.setBounds(260, 200, 120, 20);
        pnlPlayer.add(txfSearchId);
        txfSearchId.setBounds(390, 200, 90, 20);

        pnlPlayer.add(ratingListControls);
        ratingListControls.setBounds(10, 20, 240, 120);

        getContentPane().add(pnlPlayer);
        pnlPlayer.setBounds(10, 0, 494, 560);

        pnlPlayersList.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("player.players"))); // NOI18N
        pnlPlayersList.setLayout(null);

        lblPlFin.setText(locale.getString("player.players.registered_final")); // NOI18N
        pnlPlayersList.add(lblPlFin);
        lblPlFin.setBounds(60, 50, 250, 20);

        lblPlPre.setText(locale.getString("player.players.registered_preliminary")); // NOI18N
        pnlPlayersList.add(lblPlPre);
        lblPlPre.setBounds(60, 30, 250, 20);

        txfNbPlFin.setEditable(false);
        txfNbPlFin.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        pnlPlayersList.add(txfNbPlFin);
        txfNbPlFin.setBounds(10, 50, 40, 20);

        txfNbPlPre.setEditable(false);
        txfNbPlPre.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        pnlPlayersList.add(txfNbPlPre);
        txfNbPlPre.setBounds(10, 30, 40, 20);

        btnPrint.setText(locale.getString("btn.print")); // NOI18N
        btnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrintActionPerformed(evt);
            }
        });
        pnlPlayersList.add(btnPrint);
        btnPrint.setBounds(10, 480, 450, 30);
        pnlPlayersList.add(playerList);
        playerList.setBounds(10, 80, 450, 390);

        getContentPane().add(pnlPlayersList);
        pnlPlayersList.setBounds(510, 0, 470, 520);

        btnClose.setText(locale.getString("btn.close")); // NOI18N
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });
        getContentPane().add(btnClose);
        btnClose.setBounds(870, 530, 100, 30);

        btnHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/info/vannier/gotha/gothalogo16.jpg"))); // NOI18N
        btnHelp.setText(locale.getString("btn.help")); // NOI18N
        btnHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHelpActionPerformed(evt);
            }
        });
        getContentPane().add(btnHelp);
        btnHelp.setBounds(520, 530, 110, 30);

        btnSmms.setText(locale.getString("tournament.btn_set_smms_for_all_players")); // NOI18N
        btnSmms.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSmmsActionPerformed(evt);
            }
        });
        getContentPane().add(btnSmms);
        btnSmms.setBounds(640, 530, 220, 30);

        pack();
    }

    @Override
    public void onPlayerDoubleClicked(Player player) {
        resetRatingListControls();
        this.playerEditor.resetForm();
        this.playerMode = JFrPlayersManager.PLAYER_MODE_MODIF;
        playerEditor.setMode(PlayerEditor.Mode.MODIFY);
        playerEditor.setPlayer(player);
        playerInModification = player;
        this.playerEditor.setPlayer(player);
        this.btnRegister.setText(locale.getString("player.btn_save"));
    }

    private void removePlayer(Player playerToRemove) {
        try {
            String strMessage = locale.format("player.confirm_remove", playerToRemove.fullName());
            int rep = JOptionPane.showConfirmDialog(this, strMessage, "Message", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (rep == JOptionPane.YES_OPTION) {
                boolean b = tournament.removePlayer(playerToRemove);
                if (b) {
                    resetRatingListControls();
                    this.playerEditor.resetForm();
                    this.tournamentChanged();
                } else {
                    strMessage = locale.format("player.could_not_be_removed", playerToRemove.fullName());
                    JOptionPane.showMessageDialog(this, strMessage, "Message", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (TournamentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Message", JOptionPane.ERROR_MESSAGE);
        } catch (RemoteException e) {
            Logger.getLogger(JFrPlayersManager.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {
        this.cleanClose();
    }

    private void cleanClose(){
        running = false;
        dispose();
    }

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {
        TournamentPrinting.printPlayersList(tournament, playersSortType);
    }

    private void txfPlayerNameChoiceKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_UP) {
            lstPlayerNameChoice.requestFocusInWindow();
        }
        if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            lstPlayerNameChoice.requestFocusInWindow();
        }
    }

    private void lstPlayerNameChoiceValueChanged(javax.swing.event.ListSelectionEvent evt) {
        String strItem = (String) lstPlayerNameChoice.getSelectedValue();
        if (strItem == null) {
            this.playerEditor.resetForm();
            return;
        }
        String strNumber = strItem.substring(3, 8).trim();
        int number = Integer.parseInt(strNumber);
        RatedPlayer player = ratingList.getALRatedPlayers().get(number);
        this.playerEditor.setPlayer(player, rdbRankFromGrade.isSelected());
    }

    private void txfPlayerNameChoiceTextValueChanged(java.awt.event.TextEvent evt) {
        String str = txfPlayerNameChoice.getText().toLowerCase();
        if (str.length() == 0) {
            this.playerEditor.resetForm();
            return;
        }
        int pos = str.indexOf(" ");
        String str1;
        String str2;
        if (pos < 0) {
            str1 = str;
            str2 = "";
        } else {
            str1 = str.substring(0, pos);
            if (str.length() <= pos + 1) {
                str2 = "";
            } else {
                str2 = str.substring(pos + 1, str.length());
            }
        }

        Vector<String> vS = new Vector<String>();

        for (int iRP = 0; iRP < ratingList.getALRatedPlayers().size(); iRP++) {
            RatedPlayer rP = ratingList.getALRatedPlayers().get(iRP);
            String strName = rP.getName().toLowerCase();
            String strFirstName = rP.getFirstName().toLowerCase();
            int dn1 = RatedPlayer.distance_Levenshtein(str1, strName);
            int df1 = RatedPlayer.distance_Levenshtein(str2, strFirstName);
            int dn2 = RatedPlayer.distance_Levenshtein(str2, strName);
            int df2 = RatedPlayer.distance_Levenshtein(str1, strFirstName);
            int d = Math.min(dn1 + df1, dn2 + df2);
            int threshold = 9;
            if (d <= threshold) {
                String strNumber = "" + iRP;
                while (strNumber.length() < 5) {
                    strNumber = " " + strNumber;
                }
                vS.addElement("(" + d + ")" + strNumber + " " + rP.getName() + " " + rP.getFirstName() + " " +
                        rP.getCountry() + " " + rP.getClub() + " " + rP.getStrRawRating());
            }
        }
        if (vS.isEmpty()) {
            this.playerEditor.resetForm();
        } else {
            Collections.sort(vS);
            lstPlayerNameChoice.setListData(vS);
            lstPlayerNameChoice.setVisible(true);
            scpPlayerNameChoice.setVisible(true);
            lstPlayerNameChoice.setSelectedIndex(0);
        }
    }

    private void rdbLevenshteinActionPerformed(java.awt.event.ActionEvent evt) {
        this.resetControlsForLevenshteinSearching();

    }

    private void rdbFirstCharactersActionPerformed(java.awt.event.ActionEvent evt) {
        this.resetControlsForFirstCharactersSearching();
    }

    private void cbxRatingListItemStateChanged(java.awt.event.ItemEvent evt) {
        int index = cbxRatingList.getSelectedIndex();
        if (index <= 0) {
            this.playerEditor.resetForm();
        } else {
            RatedPlayer player = ratingList.getALRatedPlayers().get(index - 1);
            this.playerEditor.setPlayer(player, this.rdbRankFromGrade.isSelected());
        }
    }

    @Override
    public void onRatingListSelected(RatingListType rlType) {
        boolean useRL = rlType != RatingListType.UND;
        this.rdbFirstCharacters.setVisible(useRL);
        this.rdbLevenshtein.setVisible(useRL);

        boolean searchId = rlType == RatingListType.AGA;
        this.btnSearchId.setVisible(searchId);
        this.txfSearchId.setVisible(searchId);

        useRatingList(rlType);

        this.rdbRankFromGoR.setVisible(false);
        this.rdbRankFromGrade.setVisible(false);

        if (ratingList.getRatingListType() == RatingListType.EGF) {
            this.rdbRankFromGoR.setVisible(true);
            this.rdbRankFromGrade.setVisible(true);
        }
        if (ratingList.getRatingListType() == RatingListType.FFG) {
            this.rdbRankFromGoR.setSelected(true);
        }
        if (ratingList.getRatingListType() == RatingListType.AGA) {
            this.rdbRankFromGoR.setSelected(true);
        }
        if (ratingList.getRatingListType() == RatingListType.RGF) {
            this.rdbRankFromGoR.setSelected(true);
        }

        if (ratingList.getRatingListType() == RatingListType.UND) {
            cbxRatingList.setEnabled(false);
            cbxRatingList.setVisible(true);
            txfPlayerNameChoice.setEnabled(false);
            txfPlayerNameChoice.setVisible(false);
            scpPlayerNameChoice.setEnabled(false);
            scpPlayerNameChoice.setVisible(false);
            lstPlayerNameChoice.setEnabled(false);
            lstPlayerNameChoice.setVisible(false);
        } else {
            if (rdbFirstCharacters.isSelected()) {
                resetControlsForFirstCharactersSearching();
            } else {
                resetControlsForLevenshteinSearching();
            }
        }
    }

    // See also JFrRatings.useRatingList, which should stay a clone
    private void useRatingList(RatingListType typeRatingList) {
        switch (typeRatingList) {
            case EGF:
            case FFG:
            case AGA:
            case RGF:
                lblRatingList.setText(locale.format("rating_list.searching", locale.getString(typeRatingList.getL10nKey())));
                try {
                    ratingList = RatingListFactory.instance().loadDefaultFile(typeRatingList);
                } catch (IOException e) {
                    // TODO log error
                    e.printStackTrace();
                    ratingList = new RatingList();
                }
                break;
            default:
                ratingList = new RatingList();
        }
        int nbPlayersInRL = ratingList.getALRatedPlayers().size();
        cbxRatingList.removeAllItems();
        cbxRatingList.addItem("");
        for (RatedPlayer rP : ratingList.getALRatedPlayers()) {
            cbxRatingList.addItem(this.ratingList.getRatedPlayerString(rP));        
            
        }
        if (nbPlayersInRL == 0) {
            ratingList.setRatingListType(RatingListType.UND);
            lblRatingList.setText("No rating list has been loaded yet");
        } else {
            String strType = "";
            this.rdbFirstCharacters.setEnabled(true);
            this.rdbLevenshtein.setEnabled(true);

            switch (ratingList.getRatingListType()) {
                case EGF:
                case FFG:
                case AGA:
                case RGF:
                    strType = locale.format("rating_list.name", locale.getString(ratingList.getRatingListType().getL10nKey()));
                    break;
            }
            lblRatingList.setText(strType + " " +
                    ratingList.getStrPublicationDate() +
                    " " + nbPlayersInRL + " players");
        }
    }

    @Override
    public void onPlayerChanged(Player player) {
        if (this.playerEditor.getMode() == PlayerEditor.Mode.NEW) {
            try {
                tournament.addPlayer(player);

                resetRatingListControls();
                this.playerEditor.resetForm();
                this.tournamentChanged();
            } catch (TournamentException te) {
                JOptionPane.showMessageDialog(this, te.getMessage(), "Message", JOptionPane.ERROR_MESSAGE);
                resetRatingListControls();
                return;
            } catch (RemoteException ex) {
                resetRatingListControls();
                return;
            }

        } else if (this.playerMode == JFrPlayersManager.PLAYER_MODE_MODIF) {
            try {
                if (tournament.isPlayerImplied(player)){
                    player.setRegisteringStatus(FINAL);
                }
                tournament.modifyPlayer(playerInModification, player);
                resetRatingListControls();
            } catch (RemoteException ex) {
                resetRatingListControls();
                Logger.getLogger(JFrPlayersManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TournamentException ex) {
                resetRatingListControls();
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Message", JOptionPane.ERROR_MESSAGE);
                return;
            }
            this.tournamentChanged();
            this.playerEditor.resetForm();
        }
    }

    private void btnHelpActionPerformed(java.awt.event.ActionEvent evt) {
        Gotha.displayGothaHelp("Players Manager frame");
    }

    private void formWindowClosed(java.awt.event.WindowEvent evt) {
        this.ratingList = null;
        Runtime.getRuntime().gc();
    }

    private void rdbRankFromGradeActionPerformed(java.awt.event.ActionEvent evt) {
        resetRatingListControls();
    }

    private void rdbRankFromGoRActionPerformed(java.awt.event.ActionEvent evt) {
        resetRatingListControls();
    }

    private void btnSearchIdActionPerformed(java.awt.event.ActionEvent evt) {
        String strId = this.txfSearchId.getText();
        strId = strId.trim();
        int iRP = this.ratingList.getRatedPlayerByAGAID(strId);
        if (iRP < 0) return;
        RatedPlayer player = ratingList.getALRatedPlayers().get(iRP);
        this.playerEditor.setPlayer(player, this.rdbRankFromGrade.isSelected());
        
        
    }

    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        cleanClose();
    }

    private void txfRatingActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void btnSmmsActionPerformed(java.awt.event.ActionEvent evt) {
		JDialog dialog = new JDialog(this, locale.getString("tournament.setup_smms_by_hand.window_title"), true);
        SmmsByHand listPane = new SmmsByHand(tournament);
        dialog.setContentPane(listPane);
        dialog.pack();
        dialog.setVisible(true);
    }

    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnHelp;
    private javax.swing.JButton btnPrint;
    private javax.swing.JButton btnRegister;
    private javax.swing.JButton btnSearchId;
    private javax.swing.JButton btnSmms;
    private javax.swing.JComboBox<String> cbxRatingList;
    private javax.swing.ButtonGroup grpAlgo;
    private javax.swing.ButtonGroup grpSetRank;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel lblPlFin;
    private javax.swing.JLabel lblPlPre;
    private javax.swing.JLabel lblRatingList;
    private javax.swing.JList<String> lstPlayerNameChoice;
    private ru.gofederation.gotha.ui.PlayerList playerList;
    private javax.swing.JPanel pnlPlayer;
    private javax.swing.JPanel pnlPlayersList;
    private ru.gofederation.gotha.ui.RatingListControls ratingListControls;
    private javax.swing.JRadioButton rdbFirstCharacters;
    private javax.swing.JRadioButton rdbLevenshtein;
    private javax.swing.JRadioButton rdbRankFromGoR;
    private javax.swing.JRadioButton rdbRankFromGrade;
    private javax.swing.JScrollPane scpPlayerNameChoice;
    private javax.swing.JTextField txfNbPlFin;
    private javax.swing.JTextField txfNbPlPre;
    private java.awt.TextField txfPlayerNameChoice;
    private javax.swing.JTextField txfSearchId;

    public void resetControlsForFirstCharactersSearching() {
        this.txfPlayerNameChoice.setVisible(false);
        this.scpPlayerNameChoice.setVisible(false);
        this.lstPlayerNameChoice.setVisible(false);
        this.cbxRatingList.setVisible(true);
        this.cbxRatingList.setEnabled(true);
        cbxRatingList.setSelectedIndex(0);
        cbxRatingList.requestFocusInWindow();
    }

    public void resetControlsForLevenshteinSearching() {
        this.cbxRatingList.setVisible(false);
        this.txfPlayerNameChoice.setVisible(true);
        this.txfPlayerNameChoice.setEnabled(true);
        this.lstPlayerNameChoice.setVisible(true);
        this.lstPlayerNameChoice.setEnabled(true);
        String strInvite = "Enter approximate name and first name";
        this.txfPlayerNameChoice.setText(strInvite);
        txfPlayerNameChoice.selectAll();
        txfPlayerNameChoice.requestFocusInWindow();
    }

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
            setTitle("Players Manager. " + tournament.getFullName());
            updatePnlRegisteredPlayers(tournament.playersList());
            this.playerEditor.setNumberOfRounds(tournament.getTournamentParameterSet().getGeneralParameterSet().getNumberOfRounds());
            playerList.onTournamentUpdated();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrPlayersManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
