/*
 * JFrToolsRMI.java
 */

package info.vannier.gotha;

import net.miginfocom.swing.MigLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import javax.swing.JFrame;

/**
 *
 * @author Luc Vannier
 */
public class JFrToolsRMI extends javax.swing.JDialog {
    private static final long REFRESH_DELAY = 500;
    Registry reg = null;
    /** Creates new form JFrToolsTRMI */
    public JFrToolsRMI(JFrame owner, boolean modal) {
        super(owner, modal);

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
                if(ckbRTUpdate.isSelected())updateComponents();
            }
        };
        timer = new javax.swing.Timer((int) REFRESH_DELAY, taskPerformer);
        timer.start();
    }


    private void initComponents() {

        scpRegistryContents = new javax.swing.JScrollPane();
        lstRegistryContents = new javax.swing.JList<>();
        btnUnbind = new javax.swing.JButton();
        scpClients = new javax.swing.JScrollPane();
        lstClients = new javax.swing.JList<>();
        lblClients = new javax.swing.JLabel();
        lblClients1 = new javax.swing.JLabel();
        btnForgetNonActiveClients = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        ckbRTUpdate = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("RMI Manager");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new MigLayout("flowy, insets dialog", "[:30%:, fill]unrel[:70%:, fill]", "[][grow, fill][]unrel[]unrel[]"));

        lblClients1.setText("List of tournaments");
        getContentPane().add(lblClients1);

        lstRegistryContents.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scpRegistryContents.setViewportView(lstRegistryContents);

        getContentPane().add(scpRegistryContents);

        btnUnbind.setText("Unbind selected tournament");
        btnUnbind.addActionListener(this::btnUnbindActionPerformed);
        getContentPane().add(btnUnbind);

        ckbRTUpdate.setSelected(true);
        ckbRTUpdate.setText("Real time update");
        getContentPane().add(ckbRTUpdate, "spanx 2");

        btnClose.setText("Close");
        btnClose.addActionListener(this::btnCloseActionPerformed);
        getContentPane().add(btnClose, "spanx 2, wrap, tag cancel");

        lblClients.setText("List of clients");
        getContentPane().add(lblClients);

        lstClients.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        scpClients.setViewportView(lstClients);

        getContentPane().add(scpClients);

        btnForgetNonActiveClients.setText("Forget non active clients");
        btnForgetNonActiveClients.addActionListener(this::btnForgetNonActiveClientsActionPerformed);
        getContentPane().add(btnForgetNonActiveClients);

        pack();
    }

    private void customInitComponents(){
//        int w = JFrGotha.MEDIUM_FRAME_WIDTH;
//        int h = JFrGotha.MEDIUM_FRAME_HEIGHT;
//        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
//        setBounds((dim.width - w)/2, (dim.height -h)/2, w, h);
//        setIconImage(Gotha.getIconImage());

        updateComponents();
    }

    private void updateComponents(){
        this.lstRegistryContents.removeAll();
        String[] lstTN = GothaRMIServer.tournamentNamesList();
        if (lstTN != null)
            lstRegistryContents.setListData(lstTN);

        ArrayList<GothaRMIClient> alCN = GothaRMIServer.clientsList();
        String[] lstCN = new String[alCN.size()];
        for (int i = 0; i < lstCN.length; i++){
            GothaRMIClient cl = alCN.get(i);
            long lastKnownActivity = (System.currentTimeMillis() - cl.getLastSignOfLife()) /1000;

            lstCN[i] = cl.getClientUniqueId()
                    + " " + cl.getTournamentName()
                    + " last known activity = " + lastKnownActivity  + " sec ago";
        }
        this.lstClients.setListData(lstCN);
    }

    private void btnUnbindActionPerformed(java.awt.event.ActionEvent evt) {
        String nameToUnbind = (String)lstRegistryContents.getSelectedValue();
        GothaRMIServer.removeTournament(nameToUnbind);
        updateComponents();
}

    private void btnForgetNonActiveClientsActionPerformed(java.awt.event.ActionEvent evt) {
        long MAX_IDLE_TIME = 30000;
        ArrayList<GothaRMIClient> alC = GothaRMIServer.clientsList();
        for (GothaRMIClient cl : alC){
            long lastKnownActivity = System.currentTimeMillis() - cl.getLastSignOfLife();
            if (lastKnownActivity > MAX_IDLE_TIME) GothaRMIServer.removeClient(cl.getClientUniqueId());
            this.updateComponents();
        }
    }

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {
        cleanClose();
    }

    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        cleanClose();
    }

    private void cleanClose(){
        running = false;
        dispose();
    }



    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnForgetNonActiveClients;
    private javax.swing.JButton btnUnbind;
    private javax.swing.JCheckBox ckbRTUpdate;
    private javax.swing.JLabel lblClients;
    private javax.swing.JLabel lblClients1;
    private javax.swing.JList<String> lstClients;
    private javax.swing.JList<String> lstRegistryContents;
    private javax.swing.JScrollPane scpClients;
    private javax.swing.JScrollPane scpRegistryContents;
}
