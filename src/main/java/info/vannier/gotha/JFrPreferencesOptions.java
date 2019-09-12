/*
 * JFrPreferences.java
 */

package info.vannier.gotha;

import net.miginfocom.swing.MigLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JLabel;
import javax.swing.table.DefaultTableModel;

import ru.gofederation.gotha.util.GothaLocale;

/**
 *
 * @author Luc Vannier
 */
public class JFrPreferencesOptions extends javax.swing.JFrame {
    private static final long REFRESH_DELAY = 2000;

    private final GothaLocale locale = GothaLocale.getCurrentLocale();

    /** Creates new form JFrPreferences */
    public JFrPreferencesOptions() {
        initComponents();
        customInitComponents();
        setupRefreshPreferencesTimer();
    }

    // This setupRefreshTimer is dedicated to refresh Preferences and not Tournament data

    private volatile boolean running = true;
    javax.swing.Timer timer = null;
    private void setupRefreshPreferencesTimer() {
        ActionListener taskPerformer;
        taskPerformer = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (!running){
                    timer.stop();
                }
                updateAllViews();
            }
        };
        timer = new javax.swing.Timer((int) REFRESH_DELAY, taskPerformer);
        timer.start();
    }

    private void initComponents() {
        pnlBasicPreferences = new javax.swing.JPanel();
        scpBasicPreferences = new javax.swing.JScrollPane();
        tblBasicPreferences = new javax.swing.JTable();
        btnClearBasicPreferences = new javax.swing.JButton();
        pnlInternetAccess = new javax.swing.JPanel();
        ckbJournaling = new javax.swing.JCheckBox();
        ckbRatingLists = new javax.swing.JCheckBox();
        ckbPhotos = new javax.swing.JCheckBox();
        btnHelp = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        pnlLogPreferences = new javax.swing.JPanel();
        scpLogPreferences = new javax.swing.JScrollPane();
        tblLogPreferences = new javax.swing.JTable();
        btnClearLogPreferences = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(locale.getString("preferences"));
        setResizable(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new MigLayout("insets dialog", "[grow, fill, sg]unrel[grow, fill, sg]", "[grow, fill]unrel[]unrel[]"));

        pnlBasicPreferences.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("preferences.basic")));
        pnlBasicPreferences.setLayout(new MigLayout("flowy, insets panel"));

        tblBasicPreferences.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                locale.getString("preferences.key"), locale.getString("preferences.value")
            }
        ));
        scpBasicPreferences.setViewportView(tblBasicPreferences);

        pnlBasicPreferences.add(scpBasicPreferences, "push, grow");

        btnClearBasicPreferences.setText(locale.getString("preferences.basic.btn_clear"));
        btnClearBasicPreferences.addActionListener(this::btnClearBasicPreferencesActionPerformed);
        pnlBasicPreferences.add(btnClearBasicPreferences, "growx");

        getContentPane().add(pnlBasicPreferences);

        pnlLogPreferences.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("preferences.log")));
        pnlLogPreferences.setLayout(new MigLayout("flowy, insets panel"));

        tblLogPreferences.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                locale.getString("preferences.key"), locale.getString("preferences.value")
            }
        ));
        scpLogPreferences.setViewportView(tblLogPreferences);

        pnlLogPreferences.add(scpLogPreferences, "push, grow");

        btnClearLogPreferences.setText(locale.getString("preferences.log.btn_clear"));
        btnClearLogPreferences.addActionListener(this::btnClearLogPreferencesActionPerformed);
        pnlLogPreferences.add(btnClearLogPreferences, "growx");

        getContentPane().add(pnlLogPreferences, "wrap");

        pnlInternetAccess.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("preferences.internet")));
        pnlInternetAccess.setLayout(new MigLayout("insets panel, flowy"));

        pnlInternetAccess.add(new JLabel(locale.getString("preferences.enable_internet_access")));

        ckbRatingLists.setSelected(true);
        ckbRatingLists.setText(locale.getString("preferences.enable_internet_access.rating_list"));
        ckbRatingLists.addActionListener(this::ckbRatingListsActionPerformed);
        pnlInternetAccess.add(ckbRatingLists, "gapleft indent");

        ckbPhotos.setSelected(true);
        ckbPhotos.setText(locale.getString("preferences.enable_internet_access.photos"));
        ckbPhotos.addActionListener(this::ckbPhotosActionPerformed);
        pnlInternetAccess.add(ckbPhotos, "gapleft indent");

        ckbJournaling.setEnabled(false);
        ckbJournaling.setText(locale.getString("preferences.enable_internet_access.journal"));
        ckbJournaling.addActionListener(this::ckbJournalingActionPerformed);
        pnlInternetAccess.add(ckbJournaling, "gapleft indent");

        getContentPane().add(pnlInternetAccess, "spanx 2, growx, wrap");

        btnHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/info/vannier/gotha/gothalogo16.jpg")));
        btnHelp.setText(locale.getString("btn.help"));
        btnHelp.addActionListener(this::btnHelpActionPerformed);
        getContentPane().add(btnHelp, "span 2, split 2, tag help");

        btnClose.setText(locale.getString("btn.close"));
        btnClose.addActionListener(this::btnCloseActionPerformed);
        getContentPane().add(btnClose, "tag cancel");

        pack();
    }

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {
        cleanClose();
}

    private void cleanClose(){
        running = false;
        dispose();
    }

    private void btnClearBasicPreferencesActionPerformed(java.awt.event.ActionEvent evt) {
        Preferences prefsRoot = Preferences.userRoot();
        Preferences gothaPrefs = prefsRoot.node(Gotha.strPreferences);
        try {
            gothaPrefs.clear();
        } catch (BackingStoreException ex) {
            Logger.getLogger(JFrPreferencesOptions.class.getName()).log(Level.SEVERE, null, ex);
        }
        Gotha.setRatingListsDownloadEnabled(true);
        Gotha.setPhotosDownloadEnabled(true);
        Gotha.setJournalingReportEnabled(true);

        updateAllViews();
    }

    private void btnHelpActionPerformed(java.awt.event.ActionEvent evt) {
        Gotha.displayGothaHelp("Preferences frame");
}

    private void ckbJournalingActionPerformed(java.awt.event.ActionEvent evt) {
        Gotha.setJournalingReportEnabled(this.ckbJournaling.isSelected());
    }

    private void ckbPhotosActionPerformed(java.awt.event.ActionEvent evt) {
         Gotha.setPhotosDownloadEnabled(this.ckbPhotos.isSelected());
    }

    private void ckbRatingListsActionPerformed(java.awt.event.ActionEvent evt) {
        Gotha.setRatingListsDownloadEnabled(this.ckbRatingLists.isSelected());
    }

    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        cleanClose();
    }

    private void btnClearLogPreferencesActionPerformed(java.awt.event.ActionEvent evt) {
        Preferences prefsRoot = Preferences.userRoot();
        Preferences gothaLogPrefs = prefsRoot.node(Gotha.strPreferences + "/log");
        try {
            gothaLogPrefs.clear();
        } catch (BackingStoreException ex) {
            Logger.getLogger(JFrPreferencesOptions.class.getName()).log(Level.SEVERE, null, ex);
        }

        updateAllViews();
    }

    private void customInitComponents(){
        this.updateAllViews();
    }
    private void updateAllViews(){
        updatePnlPref();
        updatePnlInternetAccess();
    }

    private void updatePnlPref(){
        Preferences prefsRoot = Preferences.userRoot();

        Preferences gothaPrefs = prefsRoot.node(Gotha.strPreferences);
        String[] strPrefs = null;
        try {
            strPrefs = gothaPrefs.keys();
        } catch (BackingStoreException ex) {
            Logger.getLogger(JFrPreferencesOptions.class.getName()).log(Level.SEVERE, null, ex);
        }

        int len;
        if (strPrefs == null) len = 0;
        else len = strPrefs.length;
        DefaultTableModel model = (DefaultTableModel) tblBasicPreferences.getModel();
        model.setRowCount(len);
        for (int i = 0; i < len; i++){
            String key = strPrefs[i];
            String value = gothaPrefs.get(key, "def");
            model.setValueAt(key, i, 0);
            model.setValueAt(value, i, 1);
        }

        Preferences gothaLogPrefs = prefsRoot.node(Gotha.strPreferences + "/log");
        String[] strLogPrefs = null;
        try {
            strLogPrefs = gothaLogPrefs.keys();
        } catch (BackingStoreException ex) {
            Logger.getLogger(JFrPreferencesOptions.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (strPrefs == null) len = 0;
        else len = strLogPrefs.length;
        DefaultTableModel logModel = (DefaultTableModel) tblLogPreferences.getModel();
        logModel.setRowCount(len);
        for (int i = 0; i < len; i++){
            String key = strLogPrefs[i];
            String value = gothaLogPrefs.get(key, "def");

            logModel.setValueAt(key, i, 0);
            logModel.setValueAt(value, i, 1);
        }
    }

    private void updatePnlInternetAccess(){
        this.ckbRatingLists.setSelected(Gotha.isRatingListsDownloadEnabled());
        this.ckbPhotos.setSelected(Gotha.isPhotosDownloadEnabled());
        this.ckbJournaling.setSelected(Gotha.isJournalingReportEnabled());
    }

    private javax.swing.JButton btnClearBasicPreferences;
    private javax.swing.JButton btnClearLogPreferences;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnHelp;
    private javax.swing.JCheckBox ckbJournaling;
    private javax.swing.JCheckBox ckbPhotos;
    private javax.swing.JCheckBox ckbRatingLists;
    private javax.swing.JPanel pnlBasicPreferences;
    private javax.swing.JPanel pnlInternetAccess;
    private javax.swing.JPanel pnlLogPreferences;
    private javax.swing.JScrollPane scpBasicPreferences;
    private javax.swing.JScrollPane scpLogPreferences;
    private javax.swing.JTable tblBasicPreferences;
    private javax.swing.JTable tblLogPreferences;
}
