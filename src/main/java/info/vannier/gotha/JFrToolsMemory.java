/*
 * JFrToolsMemory.java
 *
 * Created on 9 mars 2012, 17:28:27
 */
package info.vannier.gotha;

import net.miginfocom.swing.MigLayout;

import java.awt.Color;
import java.awt.event.ActionListener;

/**
 *
 * @author Luc Vannier
 */
public class JFrToolsMemory extends javax.swing.JFrame {
    private static final long REFRESH_DELAY = 500;

    /** Creates new form JFrToolsMemory */
    public JFrToolsMemory() {
        initComponents();
        customInitComponents();
        setupRefreshTimer();
    }


    private volatile boolean running = true;
    private javax.swing.Timer timer = null;
    private void setupRefreshTimer() {
        ActionListener taskPerformer;
        taskPerformer = evt -> {
            if (!running){
                timer.stop();
            }
            updateComponents();
        };
        timer = new javax.swing.Timer((int) REFRESH_DELAY, taskPerformer);
        timer.start();
    }

    private void customInitComponents(){
        updateComponents();
    }

    private void updateComponents(){
        long maxM   = Runtime.getRuntime().maxMemory();
        long freeM  = Runtime.getRuntime().freeMemory();
        long totalM = Runtime.getRuntime().totalMemory();
        long usedM  = totalM - freeM;

        this.txfMaxMem.setText("" + maxM/1024/1024 + " MiB");
        this.txfUsedMem.setText("" + usedM/1024/1024 + " MiB");

        if (usedM * 100 /maxM > 80) this.txfUsedMem.setBackground(Color.red);
        else this.txfUsedMem.setBackground(Color.white);
    }

    private void initComponents() {

        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        txfMaxMem = new javax.swing.JTextField();
        txfUsedMem = new javax.swing.JTextField();
        javax.swing.JButton btnRunGB = new javax.swing.JButton();
        javax.swing.JButton btnClose = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Memory Manager");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new MigLayout("insets dialog", "[]unrel[]", "[][]unrel[]unrel[]"));

        jLabel1.setText("Max Memory :");
        getContentPane().add(jLabel1);

        txfMaxMem.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        getContentPane().add(txfMaxMem, "wmin 72lp, ax right, wrap");

        jLabel2.setText("Used Memory :");
        getContentPane().add(jLabel2);

        txfUsedMem.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        getContentPane().add(txfUsedMem, "wmin 72lp, ax r, wrap");

        btnRunGB.setText("Run Garbage Collector");
        btnRunGB.addActionListener(this::btnRunGBActionPerformed);
        getContentPane().add(btnRunGB, "spanx 2, grow, wrap");

        btnClose.setText("Close");
        btnClose.addActionListener(this::btnCloseActionPerformed);
        getContentPane().add(btnClose, "spanx 2, grow, wrap");

        pack();
    }

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {
        cleanClose();
    }

    private void cleanClose(){
        running = false;
        dispose();
    }

    private void btnRunGBActionPerformed(java.awt.event.ActionEvent evt) {
        Runtime.getRuntime().gc();
        this.updateComponents();
    }

    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        cleanClose();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new JFrToolsMemory().setVisible(true);
            }
        });
    }

    private javax.swing.JTextField txfMaxMem;
    private javax.swing.JTextField txfUsedMem;
}
