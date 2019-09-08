/*
 * JFrGamesOptions.java
 */
package info.vannier.gotha;

import net.miginfocom.swing.MigLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JLabel;

import ru.gofederation.gotha.ui.FrameBase;
import ru.gofederation.gotha.util.GothaLocale;

/**
 *
 * @author  Administrateur
 */
public class JFrGamesOptions extends javax.swing.JDialog {

    private static final long REFRESH_DELAY = 2000;
    private long lastComponentsUpdateTime = 0;
    private TournamentInterface tournament;

    private final GothaLocale locale = GothaLocale.getCurrentLocale();

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

    public JFrGamesOptions(JFrame owner, TournamentInterface tournament) throws RemoteException {
        super(owner, true);
        this.tournament = tournament;

        initComponents();
        customInitComponents();
        setupRefreshTimer();
    }

    private void initComponents() {
        grpComplTimeSystem = new javax.swing.ButtonGroup();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txfSize = new javax.swing.JTextField();
        txfKomi = new javax.swing.JTextField();
        btnClose = new javax.swing.JButton();
        btnHelp = new javax.swing.JButton();
        pnlTime = new javax.swing.JPanel();
        txfBasicTime = new javax.swing.JTextField();
        rdbSuddenDeath = new javax.swing.JRadioButton();
        rdbStdByoYomi = new javax.swing.JRadioButton();
        rdbCanByoYomi = new javax.swing.JRadioButton();
        rdbFischer = new javax.swing.JRadioButton();
        lblFischerTime = new javax.swing.JLabel();
        txfFischerTime = new javax.swing.JTextField();
        lblStdTime = new javax.swing.JLabel();
        txfStdTime = new javax.swing.JTextField();
        lblCanTime = new javax.swing.JLabel();
        txfCanTime = new javax.swing.JTextField();
        txfCanNbMoves = new javax.swing.JTextField();
        nbMovesLabel = new javax.swing.JLabel();
        lblEGFClass = new javax.swing.JLabel();
        lblAT = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Games settings");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new MigLayout("insets dialog, wrap 2", "[grow][]", "[]unrel[]unrel[]unrel[]"));

        jLabel2.setText(locale.getString("game.goban_size"));
        getContentPane().add(jLabel2);

        txfSize.setText("19");
        txfSize.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txfSizeFocusLost(evt);
            }
        });
        getContentPane().add(txfSize, "w 36lp");

        jLabel3.setText(locale.getString("game.komi"));
        getContentPane().add(jLabel3);

        txfKomi.setText("7.5");
        txfKomi.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txfKomiFocusLost(evt);
            }
        });
        getContentPane().add(txfKomi, "w 36lp");

        pnlTime.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("game.thinking_time")));
        pnlTime.setLayout(new MigLayout("insets panel, wrap 4", null, "[]unrel[]unrel[][]unrel[][]unrel[][]unrel[]unrel[]"));

        pnlTime.add(new JLabel(locale.getString("game.thinking_time.basic_time")), "span 3");

        txfBasicTime.setText("0");
        txfBasicTime.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txfBasicTimeFocusLost(evt);
            }
        });
        pnlTime.add(txfBasicTime, "w 35lp");

        grpComplTimeSystem.add(rdbSuddenDeath);
        rdbSuddenDeath.setSelected(true);
        rdbSuddenDeath.setText(locale.getString("game.thinking_time.sudden_death"));
        rdbSuddenDeath.addActionListener(this::rdbComplTimeSystemActionPerformed);
        pnlTime.add(rdbSuddenDeath, "span 4");

        grpComplTimeSystem.add(rdbStdByoYomi);
        rdbStdByoYomi.setText(locale.getString("game.thinking_time.standard_byo-yomi"));
        rdbStdByoYomi.addActionListener(this::rdbComplTimeSystemActionPerformed);
        pnlTime.add(rdbStdByoYomi, "span 4");

        lblStdTime.setText(locale.getString("game.thinking_time.time_seconds"));
        pnlTime.add(lblStdTime, "gapbefore indent, span 3, ax right");

        txfStdTime.setText("30");
        txfStdTime.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txfStdTimeFocusLost(evt);
            }
        });
        pnlTime.add(txfStdTime, "w 36lp");

        grpComplTimeSystem.add(rdbCanByoYomi);
        rdbCanByoYomi.setText(locale.getString("game.thinking_time.canadian_byo-yomi"));
        rdbCanByoYomi.addActionListener(this::rdbComplTimeSystemActionPerformed);
        pnlTime.add(rdbCanByoYomi, "span 4");

        txfCanNbMoves.setText("15");
        txfCanNbMoves.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txfCanNbMovesFocusLost(evt);
            }
        });
        pnlTime.add(txfCanNbMoves, "w 28lp, gapbefore indent");

        nbMovesLabel.setText(locale.format("game.moves", 15));
        pnlTime.add(nbMovesLabel);

        lblCanTime.setText(locale.getString("game.thinking_time.time_seconds"));
        pnlTime.add(lblCanTime, "ax right");

        txfCanTime.setText("300");
        txfCanTime.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txfCanTimeFocusLost(evt);
            }
        });
        pnlTime.add(txfCanTime, "w 36lp");

        grpComplTimeSystem.add(rdbFischer);
        rdbFischer.setText(locale.getString("game.thinking_time.fischer_system"));
        rdbFischer.addActionListener(this::rdbComplTimeSystemActionPerformed);
        pnlTime.add(rdbFischer, "span 4");

        lblFischerTime.setText(locale.getString("game.thinking_time.bonus_time_seconds"));
        pnlTime.add(lblFischerTime, "gapbefore indent, span 3, ax right");

        txfFischerTime.setText("5");
        txfFischerTime.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txfFischerTimeFocusLost(evt);
            }
        });
        pnlTime.add(txfFischerTime, "w 36lp");

        lblAT.setFont(FrameBase.scaleFont(lblEGFClass.getFont(), 1.1f));
        lblAT.setForeground(new java.awt.Color(255, 0, 0));
        lblAT.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblAT.setText(locale.getString("game.thinking_time.adjusted_time"));
        pnlTime.add(lblAT, "span 4, ax center");

        lblEGFClass.setFont(FrameBase.scaleFont(lblEGFClass.getFont(), 1.2f));
        lblEGFClass.setForeground(new java.awt.Color(255, 0, 0));
        lblEGFClass.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblEGFClass.setText(locale.getString("game.thinking_time.egf_class"));
        pnlTime.add(lblEGFClass, "span 4, ax center");

        getContentPane().add(pnlTime, "span 2");

        btnClose.setText(locale.getString("btn.close"));
        btnClose.addActionListener(this::btnCloseActionPerformed);
        getContentPane().add(btnClose, "span, split 2, tag cancel");

        btnHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/info/vannier/gotha/gothalogo16.jpg")));
        btnHelp.setText(locale.getString("btn.help"));
        btnHelp.addActionListener(this::btnHelpActionPerformed);
        getContentPane().add(btnHelp, "tag help");

        pack();
    }

    private void txfKomiFocusLost(java.awt.event.FocusEvent evt) {
        try {
            TournamentParameterSet tps = tournament.getTournamentParameterSet();
            GeneralParameterSet gps = tps.getGeneralParameterSet();
            String oldKomi = gps.getStrKomi();
            String newKomi = txfKomi.getText();
            if (newKomi.compareTo(oldKomi) == 0) return;

            gps.setStrKomi(newKomi);
            tournament.setTournamentParameterSet(tps);
            this.tournamentChanged();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesOptions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void txfSizeFocusLost(java.awt.event.FocusEvent evt) {
        try {
            TournamentParameterSet tps = tournament.getTournamentParameterSet();
            GeneralParameterSet gps = tps.getGeneralParameterSet();

            String oldStrSize = gps.getStrSize();
            String newStrSize = txfSize.getText();
            if (newStrSize.compareTo(oldStrSize) == 0) return;

            gps.setStrSize(newStrSize);
            tournament.setTournamentParameterSet(tps);
            this.tournamentChanged();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesOptions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void txfBasicTimeFocusLost(java.awt.event.FocusEvent evt) {
        try {
            TournamentParameterSet tps = tournament.getTournamentParameterSet();
            GeneralParameterSet gps = tps.getGeneralParameterSet();
            int oldVal = gps.getBasicTime();
            int newVal = oldVal;
            try {
                newVal = Integer.parseInt(this.txfBasicTime.getText());
            } catch (NumberFormatException e) {
                this.txfBasicTime.setText("" + oldVal);
                return;
            }
            if (newVal == oldVal) {
                return;
            }
            gps.setBasicTime(newVal);
            tournament.setTournamentParameterSet(tps);
            this.tournamentChanged();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesOptions.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {
        cleanClose();
    }

    private void cleanClose(){
        running = false;
        dispose();
    }

    private void btnHelpActionPerformed(java.awt.event.ActionEvent evt) {
        Gotha.displayGothaHelp("Games Options");
    }

    private void rdbComplTimeSystemActionPerformed(java.awt.event.ActionEvent evt) {
        TournamentParameterSet tps = null;
        GeneralParameterSet gps = null;
        try {
            tps = tournament.getTournamentParameterSet();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesOptions.class.getName()).log(Level.SEVERE, null, ex);
        }
        gps = tps.getGeneralParameterSet();

        int oldComplementaryTimeSystem = gps.getComplementaryTimeSystem();
        int newComplementaryTimeSystem = GeneralParameterSet.GEN_GP_CTS_SUDDENDEATH;
        if (this.rdbSuddenDeath.isSelected()) {
            newComplementaryTimeSystem = GeneralParameterSet.GEN_GP_CTS_SUDDENDEATH;
        }
        if (this.rdbStdByoYomi.isSelected()) {
            newComplementaryTimeSystem = GeneralParameterSet.GEN_GP_CTS_STDBYOYOMI;
        }
        if (this.rdbCanByoYomi.isSelected()) {
            newComplementaryTimeSystem = GeneralParameterSet.GEN_GP_CTS_CANBYOYOMI;
        }
        if (this.rdbFischer.isSelected()) {
            newComplementaryTimeSystem = GeneralParameterSet.GEN_GP_CTS_FISCHER;
        }

        if (newComplementaryTimeSystem == oldComplementaryTimeSystem) {
            return;
        }

        gps.setComplementaryTimeSystem(newComplementaryTimeSystem);

        try {
            tournament.setTournamentParameterSet(tps);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesOptions.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.tournamentChanged();

    }

    private void txfStdTimeFocusLost(java.awt.event.FocusEvent evt) {
        try {
            TournamentParameterSet tps = tournament.getTournamentParameterSet();
            GeneralParameterSet gps = tps.getGeneralParameterSet();
            int oldVal = gps.getStdByoYomiTime();
            int newVal = oldVal;
            try {
                newVal = Integer.parseInt(this.txfStdTime.getText());
            } catch (NumberFormatException e) {
                this.txfStdTime.setText("" + oldVal);
                return;
            }
            if (newVal == oldVal) {
                return;
            }
            gps.setStdByoYomiTime(newVal);
            tournament.setTournamentParameterSet(tps);
            this.tournamentChanged();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesOptions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void txfCanTimeFocusLost(java.awt.event.FocusEvent evt) {
        try {
            TournamentParameterSet tps = tournament.getTournamentParameterSet();
            GeneralParameterSet gps = tps.getGeneralParameterSet();
            int oldVal = gps.getCanByoYomiTime();
            int newVal = oldVal;
            try {
                newVal = Integer.parseInt(this.txfCanTime.getText());
            } catch (NumberFormatException e) {
                this.txfCanTime.setText("" + oldVal);
                return;
            }
            if (newVal == oldVal) {
                return;
            }
            gps.setCanByoYomiTime(newVal);
            tournament.setTournamentParameterSet(tps);
            this.tournamentChanged();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesOptions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void txfFischerTimeFocusLost(java.awt.event.FocusEvent evt) {
        try {
            TournamentParameterSet tps = tournament.getTournamentParameterSet();
            GeneralParameterSet gps = tps.getGeneralParameterSet();
            int oldVal = gps.getFischerTime();
            int newVal = oldVal;
            try {
                newVal = Integer.parseInt(this.txfFischerTime.getText());
            } catch (NumberFormatException e) {
                this.txfFischerTime.setText("" + oldVal);
                return;
            }
            if (newVal == oldVal) {
                return;
            }
            gps.setFischerTime(newVal);
            tournament.setTournamentParameterSet(tps);
            this.tournamentChanged();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesOptions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void txfCanNbMovesFocusLost(java.awt.event.FocusEvent evt) {
        try {
            TournamentParameterSet tps = tournament.getTournamentParameterSet();
            GeneralParameterSet gps = tps.getGeneralParameterSet();
            int oldVal = gps.getNbMovesCanTime();
            int newVal = oldVal;
            try {
                newVal = Integer.parseInt(this.txfCanNbMoves.getText());
            } catch (NumberFormatException e) {
                this.txfCanNbMoves.setText("" + oldVal);
                return;
            }
            if (newVal == oldVal) {
                return;
            }
            gps.setNbMovesCanTime(newVal);
            nbMovesLabel.setText(locale.format("game.moves", gps.getNbMovesCanTime()));
            tournament.setTournamentParameterSet(tps);
            this.tournamentChanged();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesOptions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        cleanClose();
    }

    private void customInitComponents() throws RemoteException {
        updateAllViews();
        // this.updatePnlGam();
    }

    private void tournamentChanged() {
        try {
            tournament.setLastTournamentModificationTime(tournament.getCurrentTournamentTime());
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesOptions.class.getName()).log(Level.SEVERE, null, ex);
        }

        updateAllViews();
    }

    private void updateAllViews() {
        try {
            if (!tournament.isOpen()) cleanClose();
            this.lastComponentsUpdateTime = tournament.getCurrentTournamentTime();
            setTitle("Games Settings. " + tournament.getFullName());
            updatePnlGam();
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGamesOptions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void updatePnlGam() throws RemoteException {
        GeneralParameterSet gps = null;
        gps = tournament.getTournamentParameterSet().getGeneralParameterSet();
        this.txfSize.setText(gps.getStrSize());
        this.txfKomi.setText(gps.getStrKomi());

        this.txfBasicTime.setText("" + gps.getBasicTime());

        int complTimeSystem = gps.getComplementaryTimeSystem();
        this.txfStdTime.setEnabled(false);
        this.txfCanNbMoves.setEnabled(false);
        this.txfCanTime.setEnabled(false);
        this.txfFischerTime.setEnabled(false);

        switch (complTimeSystem) {
            case GeneralParameterSet.GEN_GP_CTS_SUDDENDEATH:
                this.rdbSuddenDeath.setSelected(true);
                break;
            case GeneralParameterSet.GEN_GP_CTS_STDBYOYOMI:
                this.rdbStdByoYomi.setSelected(true);
                this.txfStdTime.setEnabled(true);
                break;
            case GeneralParameterSet.GEN_GP_CTS_CANBYOYOMI:
                this.rdbCanByoYomi.setSelected(true);
                this.txfCanNbMoves.setEnabled(true);
                this.txfCanTime.setEnabled(true);
                break;
            case GeneralParameterSet.GEN_GP_CTS_FISCHER:
                this.rdbFischer.setSelected(true);
                this.txfFischerTime.setEnabled(true);
                break;
        }

        this.txfStdTime.setText("" + gps.getStdByoYomiTime());
        this.txfCanNbMoves.setText("" + gps.getNbMovesCanTime());
        this.txfCanTime.setText("" + gps.getCanByoYomiTime());
        this.txfFischerTime.setText("" + gps.getFischerTime());

        // What EGF Adjusted time ?
        int at = tournament.egfAdjustedTime();

        this.lblAT.setText(locale.format("game.thinking_time.adjusted_time", at / 60));

        // What EGF class ?
        String strClass = tournament.egfClass();

        this.lblEGFClass.setText(locale.format("game.thinking_time.egf_class", strClass));

    }

    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnHelp;
    private javax.swing.ButtonGroup grpComplTimeSystem;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel nbMovesLabel;
    private javax.swing.JLabel lblAT;
    private javax.swing.JLabel lblCanTime;
    private javax.swing.JLabel lblEGFClass;
    private javax.swing.JLabel lblFischerTime;
    private javax.swing.JLabel lblStdTime;
    private javax.swing.JPanel pnlTime;
    private javax.swing.JRadioButton rdbCanByoYomi;
    private javax.swing.JRadioButton rdbFischer;
    private javax.swing.JRadioButton rdbStdByoYomi;
    private javax.swing.JRadioButton rdbSuddenDeath;
    private javax.swing.JTextField txfBasicTime;
    private javax.swing.JTextField txfCanNbMoves;
    private javax.swing.JTextField txfCanTime;
    private javax.swing.JTextField txfFischerTime;
    private javax.swing.JTextField txfKomi;
    private javax.swing.JTextField txfSize;
    private javax.swing.JTextField txfStdTime;
}
