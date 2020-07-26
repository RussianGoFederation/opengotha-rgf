/*
 * JFrPlayersManager.java
 */
package info.vannier.gotha;

import net.miginfocom.swing.MigLayout;
import ru.gofederation.gotha.model.Player;
import ru.gofederation.gotha.model.RatingListFactory;
import ru.gofederation.gotha.model.RatingListType;
import ru.gofederation.gotha.presenter.PlayersQuickCheckTableModel;
import ru.gofederation.gotha.printing.PlayerListPrinter;
import ru.gofederation.gotha.ui.Dialog;
import ru.gofederation.gotha.ui.PlayerEditor;
import ru.gofederation.gotha.ui.PlayerList;
import ru.gofederation.gotha.ui.PrinterSettings;
import ru.gofederation.gotha.ui.RatingListControls;
import ru.gofederation.gotha.ui.RatingListSearch;
import ru.gofederation.gotha.ui.SmmsByHand;
import ru.gofederation.gotha.util.GothaLocale;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ru.gofederation.gotha.model.PlayerRegistrationStatus.FINAL;
import static ru.gofederation.gotha.model.PlayerRegistrationStatus.PRELIMINARY;

/**
 *
 * @author  Luc Vannier
 */
public class JFrPlayersManager extends javax.swing.JFrame implements RatingListControls.Listener, PlayerList.PlayerDoubleClickListener, PlayerEditor.Listener, RatingListSearch.Listener {
    private static final long REFRESH_DELAY = 2000;
    private long lastComponentsUpdateTime = 0;
    private int playersSortType = PlayerComparator.NAME_ORDER;
    private final static int PLAYER_MODE_NEW = 1;
    private final static int PLAYER_MODE_MODIF = 2;
    private int playerMode = PLAYER_MODE_NEW;
    private Player.Builder playerInModification = null;
    /**  current Tournament */
    private TournamentInterface tournament;
    /** Rating List */
    private RatingList ratingList = new RatingList();

    private final PlayerEditor playerEditor;
    private final RatingListSearch ratingListSearch;

    private GothaLocale locale;

    /**
     * Creates new form JFrPlayersManager
     */
    public JFrPlayersManager(TournamentInterface tournament) throws RemoteException {
        this.locale = GothaLocale.getCurrentLocale();
        this.tournament = tournament;
        this.playerEditor = new PlayerEditor(tournament, this);
        this.ratingListSearch = new RatingListSearch();
        ratingListSearch.setListener(this);

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
        ratingListControls.addListener(this);
        playerList.setPlayerDoubleClickListener(this);
        playerList.addContextMenuItem(locale.getString("player.menu.remove"), (this::removePlayer));
        playerList.addContextMenuItem(locale.getString("player.menu.modify"), (this::onPlayerDoubleClicked));

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
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {

        grpSetRank = new javax.swing.ButtonGroup();
        pnlPlayer = new javax.swing.JPanel();
        btnRegister = new javax.swing.JButton();
        rdbRankFromGoR = new javax.swing.JRadioButton();
        rdbRankFromGrade = new javax.swing.JRadioButton();
        btnSearchId = new javax.swing.JButton();
        txfSearchId = new javax.swing.JTextField();
        ratingListControls = new ru.gofederation.gotha.ui.RatingListControls();
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
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });
        getContentPane().setLayout(new MigLayout("flowy", "[]unrel[push, grow]", "[push, grow]unrel[]"));

        pnlPlayer.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("player"))); // NOI18N
        pnlPlayer.setLayout(new MigLayout("insets panel, flowy"));

        pnlPlayer.add(ratingListControls, "spany 2");

        grpSetRank.add(rdbRankFromGoR);
        rdbRankFromGoR.setSelected(true);
        rdbRankFromGoR.setText("set Rank from rating (GoR)");
        rdbRankFromGoR.addActionListener(this::rdbRankFromGoRActionPerformed);
        pnlPlayer.add(rdbRankFromGoR, "hidemode 3");

        grpSetRank.add(rdbRankFromGrade);
        rdbRankFromGrade.setText("set Rank from Grade");
        rdbRankFromGrade.addActionListener(this::rdbRankFromGradeActionPerformed);
        pnlPlayer.add(rdbRankFromGrade, "hidemode 3");

        pnlPlayer.add(playerEditor, "spanx 2, growx, wrap");

        pnlPlayer.add(ratingListSearch, "ay top");

        pnlPlayer.add(txfSearchId, "split 2, growx, flowx, ay top");

        btnSearchId.setText("Search by Id");
        btnSearchId.addActionListener(this::btnSearchIdActionPerformed);
        pnlPlayer.add(btnSearchId, "ay top");

        getContentPane().add(pnlPlayer, "spany2, wrap");

        pnlPlayersList.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("player.players"))); // NOI18N
        pnlPlayersList.setLayout(new MigLayout("flowy, insets panel", null, "[][]unrel[grow, push]unrel[]"));

        lblPlPre.setText(locale.getString("player.players.registered_preliminary"));
        pnlPlayersList.add(lblPlPre);

        lblPlFin.setText(locale.getString("player.players.registered_final"));
        pnlPlayersList.add(lblPlFin);

        pnlPlayersList.add(playerList, "grow");

        btnPrint.setText(locale.getString("btn.print"));
        btnPrint.addActionListener(this::btnPrintActionPerformed);
        pnlPlayersList.add(btnPrint, "push, grow");

        getContentPane().add(pnlPlayersList, "growx, growy");

        btnClose.setText(locale.getString("btn.close")); // NOI18N
        btnClose.addActionListener(this::btnCloseActionPerformed);
        getContentPane().add(btnClose, "split 3, flowx, tag cancel");

        btnHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/info/vannier/gotha/gothalogo16.jpg"))); // NOI18N
        btnHelp.setText(locale.getString("btn.help")); // NOI18N
        btnHelp.addActionListener(this::btnHelpActionPerformed);
        getContentPane().add(btnHelp, "tag help");

        btnSmms.setText(locale.getString("tournament.btn_set_smms_for_all_players")); // NOI18N
        btnSmms.addActionListener(this::btnSmmsActionPerformed);
        getContentPane().add(btnSmms, "tag apply");

        pack();
    }

    @Override
    public void onPlayerDoubleClicked(Player player) {
        resetRatingListControls();
        this.playerEditor.resetForm();
        this.playerMode = JFrPlayersManager.PLAYER_MODE_MODIF;
        playerEditor.setMode(PlayerEditor.Mode.MODIFY);
        playerEditor.setPlayer(player);
        playerInModification = player.toBuilder();
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

    @Override
    public void onPlayerSelected(RatedPlayer player) {
        if (player == null) {
            this.playerEditor.resetForm();
        } else {
            this.playerEditor.setPlayer(player, this.rdbRankFromGrade.isSelected());
        }
    }

    @Override
    public void onRatingListSelected(RatingListType rlType) {
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
    }

    // See also JFrRatings.useRatingList, which should stay a clone
    private void useRatingList(RatingListType typeRatingList) {
        switch (typeRatingList) {
            case EGF:
            case FFG:
            case AGA:
            case RGF:
                try {
                    ratingList = RatingListFactory.instance().loadDefaultFile(typeRatingList);
                    ratingListSearch.setRatingList(ratingList);
                } catch (Exception e) {
                    // TODO log error
                    e.printStackTrace();
                    ratingList = new RatingList();
                    ratingListSearch.setRatingList(null);
                }
                break;
            default:
                ratingList = new RatingList();
                ratingListSearch.setRatingList(null);
        }
        int nbPlayersInRL = ratingList.getALRatedPlayers().size();
        if (nbPlayersInRL == 0) {
            ratingList.setRatingListType(RatingListType.UND);
        } else {
            String strType = "";

            switch (ratingList.getRatingListType()) {
                case EGF:
                case FFG:
                case AGA:
                case RGF:
                    strType = locale.format("rating_list.name", locale.getString(ratingList.getRatingListType().getL10nKey()));
                    break;
            }
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
                    Player.Builder pb = player.toBuilder();
                    pb.setRegisteringStatus(FINAL);
                    player = pb.build();
                }
                tournament.modifyPlayer(playerInModification.build(), player);
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
    private javax.swing.ButtonGroup grpSetRank;
    private javax.swing.JLabel lblPlFin;
    private javax.swing.JLabel lblPlPre;
    private ru.gofederation.gotha.ui.PlayerList playerList;
    private javax.swing.JPanel pnlPlayer;
    private javax.swing.JPanel pnlPlayersList;
    private ru.gofederation.gotha.ui.RatingListControls ratingListControls;
    private javax.swing.JRadioButton rdbRankFromGoR;
    private javax.swing.JRadioButton rdbRankFromGrade;
    private javax.swing.JTextField txfNbPlFin;
    private javax.swing.JTextField txfNbPlPre;
    private javax.swing.JTextField txfSearchId;

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
