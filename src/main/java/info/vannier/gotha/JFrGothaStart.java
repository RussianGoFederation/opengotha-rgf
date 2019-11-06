/*
 * JFrGothaStart.java
 */
package info.vannier.gotha;

import net.miginfocom.swing.MigLayout;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import ru.gofederation.gotha.util.GothaLocale;

/**
 *
 * @author  Luc Vannier
 */
public class JFrGothaStart extends javax.swing.JFrame {

	private GothaLocale locale;

    /** Creates new form JFrGothaStart */
    public JFrGothaStart() {
		this.locale = GothaLocale.getCurrentLocale();

        // Log Platform and JDK elements

        String strOS = System.getProperty("os.name") + " " + System.getProperty("os.arch") + " " + System.getProperty("os.version");
        LogElements.incrementElement("gotha.os", strOS);
        String strJRE = System.getProperty("java.version") + " " + System.getProperty("java.vm.name");
        LogElements.incrementElement("gotha.jre", strJRE);
        String strVersion = Gotha.getGothaFullVersionNumber();
        LogElements.incrementElement("gotha.version", strVersion);

        File rootDir = new File(System.getProperty("user.dir"));
        File dir = findADirectoryContaining(rootDir, "tournamentfiles");

        if (dir == null) {
            String str = JOptionPane.showInputDialog(this, locale.getString("start.input_workdir"),
                    rootDir.toString());
            if (!new File(str, "tournamentfiles").exists())
                JOptionPane.showMessageDialog(this, locale.getString("start.input_workdir_error"), locale.getString("alert.message"),
                        JOptionPane.WARNING_MESSAGE);
            dir = new File(str);
        }

        Gotha.runningDirectory = dir;
        Gotha.exportDirectory = new File(Gotha.runningDirectory, "exportfiles");
        Gotha.exportHTMLDirectory = new File(Gotha.runningDirectory, "exportfiles/html");
        initComponents();
        customInitComponents();
    }

    private void initComponents() {

        grpRunningMode = new javax.swing.ButtonGroup();
        btnStart = new javax.swing.JButton();
        pnlRunningMode = new javax.swing.JPanel();
        rdbSAL = new javax.swing.JRadioButton();
        rdbServer = new javax.swing.JRadioButton();
        rdbClient = new javax.swing.JRadioButton();
        btnHelp = new javax.swing.JButton();
        pnlLocale = new javax.swing.JPanel();
        javax.swing.JComboBox<GothaLocale> languageComboBox = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setLayout(new MigLayout("insets dialog, flowy", "push[fill, sg]unrel:20lp:[fill, sg]push", "push[]unrel:20lp:[][]push"));

        pnlRunningMode.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("start.mode")));
        pnlRunningMode.setLayout(new MigLayout("insets panel, flowy", null, "push[]push[]push[]push"));

        grpRunningMode.add(rdbSAL);
        rdbSAL.setSelected(true);
        pnlRunningMode.add(rdbSAL);

        grpRunningMode.add(rdbServer);
        pnlRunningMode.add(rdbServer);

        grpRunningMode.add(rdbClient);
        pnlRunningMode.add(rdbClient);

        getContentPane().add(pnlRunningMode, "spany 3, growy, wrap");

        pnlLocale.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("start.language")));
        pnlLocale.setLayout(new MigLayout("insets panel"));

        languageComboBox.setModel(ru.gofederation.gotha.util.GothaLocale.getCurrentLocale());
        languageComboBox.addActionListener(this::languageComboBoxActionPerformed);
        pnlLocale.add(languageComboBox, "wmin 140lp");

        getContentPane().add(pnlLocale);

        btnHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/info/vannier/gotha/gothalogo16.jpg")));
        btnHelp.addActionListener(this::btnHelpActionPerformed);
        getContentPane().add(btnHelp);

        btnStart.addActionListener(this::btnStartActionPerformed);
        getContentPane().add(btnStart);

        pack();
    }

    private void customInitComponents() {
        int w = JFrGotha.SMALL_FRAME_WIDTH;
        int h = JFrGotha.SMALL_FRAME_HEIGHT;
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((dim.width - w) / 2, (dim.height - h) / 2, w, h);

        setIconImage(Gotha.getIconImage());

		updateLocale();
    }

	private void updateLocale() {
		locale = GothaLocale.getCurrentLocale();
		Gotha.locale = locale.getLocale();

		setTitle(locale.getString("start.window_title"));
		btnStart.setText(locale.getString("start.btn_start"));
		pnlRunningMode.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("start.mode")));
		rdbSAL.setText(locale.getString("start.standalone"));
		rdbServer.setText(locale.getString("start.server"));
		rdbClient.setText(locale.getString("start.client"));
		rdbClient.setToolTipText(locale.getString("start.client.tooltip"));
		btnHelp.setText(locale.getString("btn.help"));
		pnlLocale.setBorder(javax.swing.BorderFactory.createTitledBorder(locale.getString("start.language")));
	}

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {
        Gotha.locale = new Locale("en");

        Gotha.runningMode = Gotha.RUNNING_MODE_UNDEFINED;

        TournamentInterface tournament = null;

        if (grpRunningMode.getSelection() == this.rdbSAL.getModel()) {
            Gotha.runningMode = Gotha.RUNNING_MODE_SAL;
        }

        if (grpRunningMode.getSelection() == this.rdbServer.getModel()) {
            InetAddress inetAddress = Gotha.getBestIPAd();
            String strIPAd = inetAddress != null ? inetAddress.toString() : "";
            strIPAd = strIPAd.replace("/", "");
            strIPAd = JOptionPane.showInputDialog(locale.getString("start.input_this_server_ip"), strIPAd);
            if (strIPAd == null) return;

            System.setProperty("java.rmi.server.hostname", strIPAd);

            Gotha.runningMode = Gotha.RUNNING_MODE_SRV;
        }

        if (grpRunningMode.getSelection() == this.rdbClient.getModel()) {
            String strSN = "";
            try {
                strSN = InetAddress.getLocalHost().getHostAddress();
            } catch (java.net.UnknownHostException ex) {
                            Logger.getLogger(JFrGothaStart.class.getName()).log(Level.SEVERE, null, ex);
            }
            strSN = JOptionPane.showInputDialog(locale.getString("start.input_server_address"), strSN);
            Gotha.serverName = strSN;

            String[] lstTou = GothaRMIClient.tournamentNamesList(Gotha.serverName);
            String strTN;
            if (lstTou == null || lstTou.length == 0){
                String strMessage = locale.format("start.no_tournaments_found_on_server", strSN);
                JOptionPane.showMessageDialog(this, strMessage);
                return;
            } else {
                strTN = (String) JOptionPane.showInputDialog(this, locale.getString("start.select_tournament"), "OpenGotha",
                        JOptionPane.INFORMATION_MESSAGE, null, lstTou, lstTou[0]);
            }

            tournament = GothaRMIClient.getTournament(Gotha.serverName, strTN);
            try {
                Gotha.clientName = tournament.addGothaRMIClient(Gotha.getHostName());
            } catch (RemoteException ex) {
                Logger.getLogger(JFrGothaStart.class.getName()).log(Level.SEVERE, null, ex);
            }
            Gotha.runningMode = Gotha.RUNNING_MODE_CLI;
        }

        // Log elements
        String strRM;
        switch(Gotha.runningMode){
            case Gotha.RUNNING_MODE_SAL : strRM = "SAL"; break;
            case Gotha.RUNNING_MODE_SRV : strRM = "SRV"; break;
            case Gotha.RUNNING_MODE_CLI : strRM = "CLI"; break;
            default : strRM = "???";
        }
        LogElements.incrementElement("gotha.runningmode", strRM);

        ResourceBundle fileChooserBundle = ResourceBundle.getBundle("l10n/FileChooser", locale.getLocale());
        for (Enumeration<String> keys = fileChooserBundle.getKeys(); keys.hasMoreElements();) {
            String key = keys.nextElement();
            UIManager.put(key, fileChooserBundle.getString(key));
        }

        try {
            new JFrGotha(tournament).setVisible(true);
        } catch (RemoteException ex) {
            Logger.getLogger(JFrGothaStart.class.getName()).log(Level.SEVERE, null, ex);
        }

        dispose();
    }

    private void btnHelpActionPerformed(java.awt.event.ActionEvent evt) {
        Gotha.displayGothaHelp("Starting OpenGotha");
    }

    private void languageComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
        updateLocale();
    }

    /**
     * Recursively searches for a directory containing a directory whose name is strFile
     */
    private File findADirectoryContaining(File rootDir, String strFile){
        if (new File(rootDir, strFile).exists()){
            return rootDir;
        }
        else{
            File[] lst = rootDir.listFiles();
            if (lst == null) return null;
            for (File file : lst) {
                if (!file.isDirectory()) continue;
                if (file.isHidden()) continue;
                File f = findADirectoryContaining(file, strFile);
                if (f != null) {
                    return f;
                }
            }
            return null;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(getNativeLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new JFrGothaStart().setVisible(true);
        });
    }

    /**
     * Returns LookAndFeel class name based on current OS.
     * Falls back to {@link UIManager#getSystemLookAndFeelClassName()}.
     */
    private static String getNativeLookAndFeelClassName() {
        String systemLaf = System.getProperty("swing.systemlaf");
        if (systemLaf != null) {
            return systemLaf;
        }

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")){
            return "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        }
        else if (os.contains("osx")){
            return "com.apple.laf.AquaLookAndFeel";
        }
        else if (os.contains("nix") || os.contains("aix") || os.contains("nux")){
            return "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        }

        return UIManager.getCrossPlatformLookAndFeelClassName();
    }

    private javax.swing.JButton btnHelp;
    private javax.swing.JButton btnStart;
    private javax.swing.ButtonGroup grpRunningMode;
    private javax.swing.JPanel pnlLocale;
    private javax.swing.JPanel pnlRunningMode;
    private javax.swing.JRadioButton rdbClient;
    private javax.swing.JRadioButton rdbSAL;
    private javax.swing.JRadioButton rdbServer;
}
