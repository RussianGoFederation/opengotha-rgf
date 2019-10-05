/*
 * This file is part of OpenGotha.
 *
 * OpenGotha is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * OpenGotha is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenGotha. If not, see <http://www.gnu.org/licenses/>.
 */

package ru.gofederation.gotha.ui;

import net.miginfocom.swing.MigLayout;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.PageAttributes;
import java.awt.PrintJob;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import info.vannier.gotha.CountriesList;
import info.vannier.gotha.Country;
import info.vannier.gotha.GeneralParameterSet;
import info.vannier.gotha.Gotha;
import info.vannier.gotha.GothaImageLoader;
import info.vannier.gotha.JFrPlayersManager;
import info.vannier.gotha.Player;
import info.vannier.gotha.PlayerException;
import info.vannier.gotha.RatedPlayer;
import info.vannier.gotha.TournamentInterface;
import ru.gofederation.gotha.model.PlayerRegistrationStatus;
import ru.gofederation.gotha.model.RatingOrigin;
import ru.gofederation.gotha.util.GothaLocale;

import static ru.gofederation.gotha.model.PlayerRegistrationStatus.FINAL;
import static ru.gofederation.gotha.model.PlayerRegistrationStatus.PRELIMINARY;
import static ru.gofederation.gotha.model.RatingOrigin.AGA;
import static ru.gofederation.gotha.model.RatingOrigin.FFG;

public class PlayerEditor extends JPanel {
    private GothaLocale locale = GothaLocale.getCurrentLocale();
    private TournamentInterface tournament;
    private Mode mode = Mode.NEW;
    private JCheckBox[] participation = new JCheckBox[Gotha.MAX_NUMBER_OF_ROUNDS];
    private Listener listener;

    private JTextField firstName;
    private JTextField lastName;
    private JLabel photo;
    private JTextField rating;
    private JTextField ratingOrigin;
    private JTextField grade;
    private JComboBox<String> country;
    private JTextField club;
    private JCheckBox smmsByHand;
    private JTextField smms;
    private JButton changeRating;
    private JTextField rank;
    private JTextField smmsCorrection;
    private JTextField agaId;
    private JLabel agaExpirationDate;
    private JTextField egfPin;
    private JTextField ffgLicence;
    private JTextField ffgLicenceStatus;
    private JLabel ffgLicenceStatusDescr;
    private JTextField rgfId;
    private JButton register;
    private ButtonGroup registrationStatus;
    private JRadioButton registrationPreliminary;
    private JRadioButton registrationFinal;
    private JCheckBox welcomeSheet;

    private JScrollPane welcomeSheetScroll;
    private JTextPane welcomeSheetPane;

    public PlayerEditor(TournamentInterface tournament, Listener listener) throws RemoteException {
        this.tournament = tournament;
        this.listener = listener;

        createUI();
        resetForm();

        GeneralParameterSet gps = tournament.getTournamentParameterSet().getGeneralParameterSet();

        setNumberOfRounds(gps.getNumberOfRounds());
    }

    private void createUI() {
        setLayout(new MigLayout("insets 0", null, "[][]unrel[][]unrel[]unrel[]unrel[][]"));

        // Last name
        add(new JLabel(locale.getString("player.last_name")));
        lastName = new JTextField();
        add(lastName, "width 120lp, sgx name");

        // Photo
        photo = new JLabel();
        add(photo, "spany 4, width 80, height 115");

        // Rounds
        JPanel rounds = new JPanel();
        rounds.setBorder(BorderFactory.createTitledBorder(locale.getString("player.participation")));
        rounds.setLayout(new MigLayout("wrap 5", "[]0[]", "[]0[]"));
        for (int i = 0; i < Gotha.MAX_NUMBER_OF_ROUNDS; i++) {
            JCheckBox cb = new JCheckBox(Integer.toString(i + 1));
            participation[i] = cb;
            rounds.add(cb, "hidemode 3, wmin 40lp");
        }
        add(rounds, "spany 4, ay top, spanx, wrap");

        // First name
        add(new JLabel(locale.getString("player.first_name")));
        firstName = new JTextField();
        add(firstName, "sgx name, wrap");

        // Country
        add(new JLabel(locale.getString("player.country")));
        country = new JComboBox<>();
        country.setEditable(false);
        initCountriesList();
        add(country, "width 50lp, wrap");

        // Club
        add(new JLabel(locale.getString("player.club")));
        club = new JTextField();
        add(club, "width 50lp, wrap");

        // Rating controls
        JPanel ratingsPanel = new JPanel();
        ratingsPanel.setLayout(new MigLayout("insets 0", "[][sg b]unrel[][sg b]unrel[][sg b]unrel[][sg b]", "[]unrel[][]"));
        changeRating = new JButton(locale.getString("player.btn_change_rating"));
        changeRating.addActionListener((event) -> changeRating());
        ratingsPanel.add(changeRating, "spanx 4");
        smmsByHand = new JCheckBox(locale.getString("player.smms_by_hand"));
        smmsByHand.addActionListener(this::smmsByHandActionPerformed);
        ratingsPanel.add(smmsByHand, "spanx 3");
        smms = new JTextField();
        ratingsPanel.add(smms, "width 50lp, wrap");
        ratingsPanel.add(new JLabel(locale.getString("player.rating")));
        rating = new JTextField();
        rating.setEditable(false);
        ratingsPanel.add(rating, "width 50lp");
        ratingsPanel.add(new JLabel(locale.getString("player.rating_origin")));
        ratingOrigin = new JTextField();
        ratingOrigin.setEditable(false);
        ratingsPanel.add(ratingOrigin, "width 50lp");
        ratingsPanel.add(new JLabel(locale.getString("rating_list.egf_pin")));
        egfPin = new JTextField();
        ratingsPanel.add(egfPin, "width 70lp");
        ratingsPanel.add(new JLabel(locale.getString("rating_list.rgf_id")));
        rgfId = new JTextField();
        ratingsPanel.add(rgfId, "width 70lp, wrap");
        ratingsPanel.add(new JLabel(locale.getString("player.grade")));
        grade = new JTextField();
        grade.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent focusEvent) {
                gradeFocusLost();
            }
        });
        ratingsPanel.add(grade, "width 50lp");
        ratingsPanel.add(new JLabel(locale.getString("player.rank")));
        rank = new JTextField();
        rank.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent focusEvent) {
                rankFocusLost();
            }
        });
        ratingsPanel.add(rank, "growx, split 2");
        smmsCorrection = new JTextField();
        smmsCorrection.setToolTipText("smms correction (relevant for McMahon super-groups)");
        smmsCorrection.setEditable(false);
        ratingsPanel.add(smmsCorrection, "width 16lp");
        ratingsPanel.add(new JLabel(locale.getString("rating_list.aga_id")));
        agaId = new JTextField();
        ratingsPanel.add(agaId, "width 70lp, id aga");
        agaExpirationDate = new JLabel();
        Font smallCursive = agaExpirationDate.getFont();
        smallCursive = smallCursive.deriveFont(Font.ITALIC, smallCursive.getSize() * 0.8f);
        agaExpirationDate.setFont(smallCursive);
        agaExpirationDate.setForeground(Color.black/* TODO: use some sort of theming */);
        ratingsPanel.add(agaExpirationDate, "pos aga.x aga.y2");
        ratingsPanel.add(new JLabel(locale.getString("rating_list.ffg_lic")));
        ffgLicence = new JTextField();
        ratingsPanel.add(ffgLicence, "growx, split 2, id ffg");
        ffgLicenceStatus = new JTextField();
        ffgLicenceStatus.setEditable(false);
        ratingsPanel.add(ffgLicenceStatus, "width 12lp, wrap, id ffg_s");
        ffgLicenceStatusDescr = new JLabel();
        ffgLicenceStatusDescr.setFont(smallCursive);
        ratingsPanel.add(ffgLicenceStatusDescr, "pos ffg.x ffg.y2 ffg_s.x2");
        add(ratingsPanel, "spanx, wrap");

        // Registration status
        JPanel registration = new JPanel();
        registration.setBorder(BorderFactory.createTitledBorder(locale.getString("player.registration")));
        registration.setLayout(new MigLayout("insets 0"));
        registrationFinal = new JRadioButton(locale.getString("player.registration.final"));
        registration.add(registrationFinal);
        registrationPreliminary = new JRadioButton(locale.getString("player.registration.preliminary"));
        registration.add(registrationPreliminary);
        registrationStatus = new ButtonGroup();
        registrationStatus.add(registrationFinal);
        registrationStatus.add(registrationPreliminary);
        add(registration, "spanx, wrap");

        // Register button
        register = new JButton(locale.getString("player.btn_register"));
        register.addActionListener(actionEvent -> {
            onRegisterAction();
        });
        add(register, "span, split 2, growx");

        // Reset button
        JButton reset = new JButton(locale.getString("player.btn_reset"));
        reset.addActionListener((actionEvent) -> resetForm());
        add(reset, "wrap");

        // Print welcome sheet?
        welcomeSheet = new JCheckBox(locale.getString("player.print_welcome_sheet"));
        welcomeSheet.setToolTipText(locale.getString("player.print_welcome_sheet_tooltip")); // NOI18N
        add(welcomeSheet, "span, wrap");

        // Welcome sheet. TODO: refactor.
        welcomeSheetPane = new JTextPane();
        welcomeSheetScroll = new JScrollPane();
        welcomeSheetScroll.setVisible(false);
        welcomeSheetScroll.setViewportView(welcomeSheetPane);
        add(welcomeSheetScroll, "pos 0 0 840 1188, hidemode 3");
    }

    private void initCountriesList(){
        File f = new File(Gotha.runningDirectory, "documents/iso_3166-1_list_en.xml");

        List<Country> countries = CountriesList.importCountriesFromXMLFile(f);
        this.country.removeAllItems();
        this.country.addItem("  ");

        if (countries == null) return;

        for(Country c : countries){
            this.country.addItem(c.getAlpha2Code());
        }
    }

    public void setNumberOfRounds(int numberOfRounds) {
        for (int i = 0; i < Gotha.MAX_NUMBER_OF_ROUNDS; i++) {
            participation[i].setVisible(i < numberOfRounds);
        }
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        if (mode == Mode.NEW) {
            register.setText(locale.getString("player.btn_register"));
        } else {
            register.setText(locale.getString("player.btn_save"));
        }
    }

    public void setPlayer(RatedPlayer player, boolean rankFromGrade) {
        resetForm();

        firstName.setText(player.getFirstName());
        lastName.setText(player.getName());

        int stdRating = player.getStdRating();
        RatingOrigin ratingOrigin = player.getRatingOrigin();
        String strRatingOrigin = ratingOrigin.toString();
        if (ratingOrigin == FFG) strRatingOrigin += " : " + player.getStrRawRating();
        if (ratingOrigin == AGA) strRatingOrigin += " : " + player.getStrRawRating();
        this.ratingOrigin.setText(strRatingOrigin);
        this.smmsCorrection.setText("0");
        this.rating.setText(Integer.toString(stdRating));
        this.agaId.setText(player.getAgaId());
        int rank = Player.rankFromRating(ratingOrigin, stdRating);
        if (rankFromGrade) rank = Player.convertKDPToInt(player.getStrGrade());
        this.rank.setText(Player.convertIntToKD(rank));
        this.grade.setText(player.getStrGrade());

        this.country.setSelectedItem(player.getCountry());
        this.club.setText(player.getClub());

        this.ffgLicence.setText(player.getFfgLicence());
        this.ffgLicenceStatus.setText(player.getFfgLicenceStatus());
        if (player.getFfgLicenceStatus().compareTo("-") == 0) {
            this.ffgLicenceStatusDescr.setText("Non licencié");
        } else {
            this.ffgLicenceStatusDescr.setText("");
        }
        if (player.getFfgLicenceStatus().compareTo("-") == 0) {
            this.ffgLicenceStatusDescr.setText("Non licencié");
            this.ffgLicenceStatusDescr.setForeground(Color.RED);
        } else if (player.getFfgLicenceStatus().compareTo("C") == 0){
            this.ffgLicenceStatusDescr.setText("Licence loisir");
            this.ffgLicenceStatusDescr.setForeground(Color.BLUE);
        } else {
            this.ffgLicenceStatusDescr.setText("");
            this.ffgLicenceStatusDescr.setForeground(Color.BLACK);
        }

        String strEGFPin = player.getEgfPin();
        this.egfPin.setText(strEGFPin);
        if (strEGFPin != null && strEGFPin.length() == 8 && Gotha.isPhotosDownloadEnabled())
            GothaImageLoader.loadImage("http://www.europeangodatabase.eu/EGD/Actions.php?key=" + strEGFPin, this.photo);

        this.agaId.setText(player.getAgaId());
        String strDate = player.getAgaExpirationDate();
        agaExpirationDate.setText(strDate);
        if (Gotha.isDateExpired(strDate)) agaExpirationDate.setForeground(Color.red);

        if (player.getRgfId() > 0) {
            this.rgfId.setText(Integer.toString(player.getRgfId()));
        }

        for (int i = 0; i < Gotha.MAX_NUMBER_OF_ROUNDS; i++) {
            participation[i].setSelected(true);
            participation[i].setEnabled(true);
        }

        this.register.setText(locale.getString("player.btn_register"));
    }

    /**
     * Fills this form with given player's data
     * @param player
     */
    public void setPlayer(Player player) {
        resetForm();

        setMode(Mode.MODIFY);

        firstName.setText(player.getFirstName());
        lastName.setText(player.getName());

        int rating = player.getRating();
        this.rating.setText(Integer.toString(rating));
        RatingOrigin ratingOrigin = player.getRatingOrigin();
        String strRatingOrigin = ratingOrigin.toString();
        if (ratingOrigin == RatingOrigin.FFG) strRatingOrigin += " : " + player.getStrRawRating();
        if (ratingOrigin == RatingOrigin.AGA) strRatingOrigin += " : " + player.getStrRawRating();
        this.ratingOrigin.setText(strRatingOrigin);
        this.grade.setText(player.getStrGrade());

        if (player.isSmmsByHand()) {
            this.smmsByHand.setSelected(true);
//            cbkSmmsByHandActionPerformed(null);
            this.smms.setText(Integer.toString(player.getSmmsByHand()));
        } else {
            this.smmsByHand.setSelected(false);
//            cbkSmmsByHandActionPerformed(null);
        }

        int corr = player.getSmmsCorrection();
        String strCorr = "" + corr;
        if (corr > 0 ) strCorr = "+" + corr;
        this.smmsCorrection.setText(strCorr);
        int rank = (player.getRank());
        this.rank.setText(Player.convertIntToKD(rank));
        this.country.setSelectedItem(player.getCountry());
        this.club.setText(player.getClub());
        this.ffgLicence.setText(player.getFfgLicence());
        this.ffgLicenceStatus.setText(player.getFfgLicenceStatus());

        if (player.getFfgLicenceStatus().equals("-")) {
            this.ffgLicenceStatusDescr.setText("Non licencié");
            this.ffgLicenceStatusDescr.setForeground(Color.RED/* TODO: use some sort of theming */);
        } else if (player.getFfgLicenceStatus().equals("C")){
            this.ffgLicenceStatusDescr.setText("Licence loisir");
            this.ffgLicenceStatusDescr.setForeground(Color.BLUE/* TODO: use some sort of theming */);
        } else {
            this.ffgLicenceStatusDescr.setText("");
            this.ffgLicenceStatusDescr.setForeground(Color.BLACK/* TODO: use some sort of theming */);
        }

        String strEGFPin = player.getEgfPin();
        this.egfPin.setText(strEGFPin);
        if (strEGFPin != null && strEGFPin.length() == 8 && Gotha.isPhotosDownloadEnabled())
            GothaImageLoader.loadImage("http://www.europeangodatabase.eu/EGD/Actions.php?key=" + strEGFPin, this.photo);

        if (player.getRgfId() > 0) {
            this.rgfId.setText(Integer.toString(player.getRgfId()));
        }

        this.agaId.setText(player.getAgaId());
        String strDate = player.getAgaExpirationDate();
        this.agaExpirationDate.setText(strDate);
        if (Gotha.isDateExpired(strDate)) this.agaExpirationDate.setForeground(Color.red/* TODO: use some sort of theming */);

        if (player.getRegisteringStatus() == PlayerRegistrationStatus.FINAL) {
            this.registrationFinal.setSelected(true);
        } else {
            this.registrationPreliminary.setSelected(true);
        }
        boolean bImplied = false;
        try {
            bImplied = tournament.isPlayerImplied(player);
        } catch (RemoteException ex) {
            Logger.getLogger(PlayerEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.registrationPreliminary.setEnabled(!bImplied);
        this.registrationFinal.setEnabled(!bImplied);

        boolean[] bPart = player.getParticipating();
        for (int r = 0; r < Gotha.MAX_NUMBER_OF_ROUNDS; r++) {
            try {
                participation[r].setSelected(bPart[r]);
                participation[r].setEnabled(!tournament.isPlayerImpliedInRound(player, r));
            } catch (RemoteException ex) {
                Logger.getLogger(PlayerEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Creates <strong>new</strong> player from this form
     * @return newly created {@link Player} object
     */
    public Player getPlayer() {
        manageRankGradeAndRatingValues(); // Before anything else, fill unfilled grade/rank/rating fields
        this.firstName.setText(normalizeCase(this.firstName.getText()));
        this.lastName.setText(normalizeCase(this.lastName.getText()));

        Player p;

        PlayerRegistrationStatus registration = getSelectedRegistrationStatus();

        int rating;
        int rank = Player.convertKDPToInt(this.rank.getText());

        String strOrigin;
        try{
            strOrigin = this.ratingOrigin.getText().substring(0, 3);
            rating = Integer.parseInt(this.rating.getText());
        }catch(Exception e){
            strOrigin = "INI";
            rating = Player.ratingFromRank(RatingOrigin.RGF, rank); // TODO: somehow set proper origin
        }

        int smmsCorrection;
        try {
            String strCorr = this.smmsCorrection.getText();
            if (strCorr.substring(0, 1).equals("+")) strCorr = strCorr.substring(1);
            smmsCorrection = Integer.parseInt(strCorr);
        } catch (NumberFormatException ex) {
            smmsCorrection = 0;
        }

        int rgfId = 0;
        try {
            rgfId = Integer.parseInt(this.rgfId.getText());
        } catch (NumberFormatException e) {
            // Noop is ok
        }

        try {
            p = new Player.Builder()
                .setName(this.lastName.getText())
                .setFirstName(this.firstName.getText())
                .setCountry(((String)this.country.getSelectedItem()))
                .setClub(this.club.getText().trim())
                .setEgfPin(this.egfPin.getText())
                .setFfgLicence(this.ffgLicence.getText(), this.ffgLicenceStatus.getText())
                .setAgaId(this.agaId.getText(), this.agaExpirationDate.getText())
                .setRgfId(rgfId)
                .setRank(rank)
                .setRating(rating, RatingOrigin.fromString(strOrigin))
                .setGrade(this.grade.getText())
                .setSmmsCorrection(smmsCorrection)
                .setSmmsByHand(getSmmsByHand())
                .setRegistrationStatus(registration)
                .build();

            boolean[] bPart = new boolean[Gotha.MAX_NUMBER_OF_ROUNDS];

            int nbRounds = 0;
            try {
                nbRounds = tournament.getTournamentParameterSet().getGeneralParameterSet().getNumberOfRounds();
            } catch (RemoteException ex) {
                Logger.getLogger(JFrPlayersManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (int i = 0; i < nbRounds; i++) {
                bPart[i] = participation[i].isSelected();
            }
            for (int i = nbRounds; i < Gotha.MAX_NUMBER_OF_ROUNDS; i++) {
                bPart[i] = participation[nbRounds - 1].isSelected();
            }
            p.setParticipating(bPart);
        } catch (PlayerException pe) {
            JOptionPane.showMessageDialog(this, pe.getMessage(), "Message", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (mode == Mode.NEW) {
            Preferences prefs = Preferences.userRoot().node(Gotha.strPreferences + "/playersmanager");
            prefs.put("defaultregistration", registration.toString());
        }
        /*
        if (this.playerMode == JFrPlayersManager.PLAYER_MODE_NEW) {
            try {
                tournament.addPlayer(p);
                // Keep current registration status as default registration status
                registration = FINAL;
                if (grpRegistration.getSelection() == rdbPreliminary.getModel()) registration = PRELIMINARY;
                Preferences prefs = Preferences.userRoot().node(Gotha.strPreferences + "/playersmanager");
                prefs.put("defaultregistration", registration.toString());

                resetRatingListControls();
                resetPlayerControls();
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
                if (tournament.isPlayerImplied(p)){
                    p.setRegisteringStatus(FINAL);
                }
                tournament.modifyPlayer(playerInModification, p);
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
            resetPlayerControls();

        }
         */
        // Print Welcome sheet
        if (this.welcomeSheet.isSelected()) {
            instanciateWelcomeSheet(new File(Gotha.runningDirectory, "welcomesheet/welcomesheet.html"),
                new File(Gotha.runningDirectory, "welcomesheet/actualwelcomesheet.html"), p);
            try {
                URL url = new File(Gotha.runningDirectory, "welcomesheet/actualwelcomesheet.html").toURI().toURL();
                welcomeSheetPane.setPage(url);
            } catch (IOException ex) {
                Logger.getLogger(JFrPlayersManager.class.getName()).log(Level.SEVERE, null, ex);
            }

            PageAttributes pa = new PageAttributes();
            pa.setPrinterResolution(100);
            pa.setOrigin(PageAttributes.OriginType.PRINTABLE);
            PrintJob pj = getToolkit().getPrintJob((JFrame) SwingUtilities.getWindowAncestor(this), "Welcome Sheet", null, pa);
            if (pj != null) {
                Graphics pg = pj.getGraphics();
                welcomeSheetPane.print(pg);
                pg.dispose();
                pj.end();
            }

        }

        return p;
    }

    private void gradeFocusLost() {
        this.grade.setText(grade.getText().toUpperCase());
        manageRankGradeAndRatingValues();
    }

    private void rankFocusLost() {
        String strRank = this.rank.getText();
        int rank = Player.convertKDPToInt(strRank);
        this.rank.setText(Player.convertIntToKD(rank));

        // update rating from rank
        if (this.rating.getText().equals("")){
            int rating = rank * 100 + 2100;
            this.rating.setText("" + rating);
            this.ratingOrigin.setText("INI");
        }

        this.manageRankGradeAndRatingValues();
    }

    private void changeRating() {
        int oldRating;
        try{
            oldRating = Integer.parseInt(this.rating.getText());
        }
        catch(NumberFormatException e){
            oldRating = 0;
        }

        String strMessage = locale.format("player.enter_new_rating", Player.MIN_RATING, Player.MAX_RATING);
        String strResponse = JOptionPane.showInputDialog(strMessage);
        int newRating = oldRating;
        try{
            newRating = Integer.parseInt(strResponse);
            if (newRating < Player.MIN_RATING) newRating = Player.MIN_RATING;
            if (newRating > Player.MAX_RATING) newRating = Player.MAX_RATING;
        }catch(Exception e){
            newRating = oldRating;
        }

        if (newRating != oldRating){
            this.rating.setText("" + newRating);
            this.ratingOrigin.setText("MAN");
        }

    }

    private void manageRankGradeAndRatingValues(){
        if (this.rank.getText().equals("") && !this.grade.getText().equals("")){
            int r = Player.convertKDPToInt(this.grade.getText());
            this.rank.setText(Player.convertIntToKD(r));
        }
        if (this.grade.getText().equals("") && !this.rank.getText().equals("")){
            this.grade.setText(this.rank.getText());
        }

        String strRank = this.rank.getText();
        if (strRank.equals("")) return;
        int rank = Player.convertKDPToInt(strRank);
        if (this.rating.getText().equals("")){
            int rating = rank * 100 + 2100;
            this.rating.setText("" + rating);
            this.ratingOrigin.setText("INI");
        }
    }

    private void onRegisterAction() {
        // Keep current registration status as default registration status
        Preferences prefs = Preferences.userRoot().node(Gotha.strPreferences + "/playersmanager");
        prefs.put("defaultregistration", this.getSelectedRegistrationStatus().name());

        if (null != listener) listener.onPlayerChanged(getPlayer());
    }

    public void resetForm() {
        setMode(Mode.NEW);
        this.firstName.setText("");
        this.lastName.setText("");
        this.rank.setText("");
        this.smmsCorrection.setText("0");
        this.ratingOrigin.setText("");
        this.rating.setText("");
        this.grade.setText("");
        this.country.setSelectedItem("  ");
        this.club.setText("");
        this.smmsByHand.setSelected(false);
        this.smmsByHandActionPerformed(null);
        this.ffgLicence.setText("");
        this.ffgLicenceStatus.setText("");
        this.ffgLicenceStatus.setText("");
        this.egfPin.setText("");
        this.photo.setIcon(null);
        this.rgfId.setText("");
        Preferences prefs = Preferences.userRoot().node(Gotha.strPreferences + "/playersmanager");
        String defaultRegistration = prefs.get("defaultregistration", FINAL.toString() );
        if (defaultRegistration.equals(PRELIMINARY.toString())) {
            this.registrationPreliminary.setSelected(true);
        } else {
            this.registrationFinal.setSelected(true);
        }
        registrationPreliminary.setEnabled(true);
        registrationFinal.setEnabled(true);
    }

    private static String normalizeCase(String text) {
        StringBuilder sb = new StringBuilder();
        Pattern namePattern = Pattern.compile(
            "(?:(da|de|degli|del|der|di|el|la|le|ter|und|van|vom|von|zu|zum)" +
                "|(.+?))(?:\\b|(?=_))([- _]?)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = namePattern.matcher(text.trim().toLowerCase());
        while (matcher.find()) {
            String noblePart = matcher.group(1);
            String namePart = matcher.group(2);
            String wordBreak = matcher.group(3);
            if (noblePart != null) {
                sb.append(noblePart);
            } else {
                sb.append(Character.toUpperCase(namePart.charAt(0)));
                sb.append(namePart.substring(1)); // always returns at least ""
            }
            if (wordBreak != null) {
                sb.append(wordBreak);
            }
        }
        return sb.toString();
    }

    private void instanciateWelcomeSheet(File templateFile, File actualFile, Player p) {
        Vector<String> vLines = new Vector<String>();
        try {
            FileInputStream fis = new FileInputStream(templateFile);
            BufferedReader d = new BufferedReader(new InputStreamReader(fis, java.nio.charset.Charset.forName("UTF-8")));

            String s;
            do {
                s = d.readLine();
                if (s != null) {
                    vLines.add(s);
                }
            } while (s != null);
            d.close();
            fis.close();
        } catch (Exception ex) {
            Logger.getLogger(JFrPlayersManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Replace player tags
        Vector<String> vActualLines = new Vector<String>();
        for (String strLine : vLines) {
            if (strLine.length() == 0) {
                continue;
            }
            strLine = strLine.replaceAll("<name>", p.getName());
            strLine = strLine.replaceAll("<firstname>", p.getFirstName());
            strLine = strLine.replaceAll("<country>", p.getCountry());
            strLine = strLine.replaceAll("<club>", p.getClub());
            strLine = strLine.replaceAll("<rank>", Player.convertIntToKD(p.getRank()));
            int rawRating = p.getRating();
            RatingOrigin ratingOrigin = p.getRatingOrigin();
            if (ratingOrigin == FFG) {
                rawRating -= 2050;
            }
            strLine = strLine.replaceAll("<rating>", Integer.valueOf(rawRating).toString());
            strLine = strLine.replaceAll("<ratingorigin>", ratingOrigin.toString());
            boolean[] bPart = p.getParticipating();
            String strPart = "";
            int nbRounds = 0;
            try {
                nbRounds = tournament.getTournamentParameterSet().getGeneralParameterSet().getNumberOfRounds();
            } catch (RemoteException ex) {
                Logger.getLogger(JFrPlayersManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (int r = 0; r < nbRounds; r++) {
                if (bPart[r]) {
                    strPart += " " + (r + 1);
                } else {
                    strPart += " -";
                }
            }
            strLine = strLine.replaceAll("<participation>", strPart);
            vActualLines.add(strLine);
        }

        Writer output = null;
        try {
            output = new BufferedWriter(new FileWriter(actualFile));
        } catch (IOException ex) {
            Logger.getLogger(JFrPlayersManager.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        for (String strLine : vActualLines) {
            try {
                output.write(strLine + "\n");
            } catch (IOException ex) {
                Logger.getLogger(JFrPlayersManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            output.close();
        } catch (IOException ex) {
            Logger.getLogger(JFrPlayersManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private int getSmmsByHand() {
        if (!smmsByHand.isSelected()) return -1;
        try {
            return Integer.parseInt(smms.getText().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void smmsByHandActionPerformed(ActionEvent event) {
        if (smmsByHand.isSelected()) {
            smms.setEditable(true);
            smms.setText("0");
        } else {
            smms.setEditable(false);
            smms.setText("");
        }

    }

    public enum Mode {
        MODIFY, NEW
    }

    public PlayerRegistrationStatus getSelectedRegistrationStatus() {
        if (registrationFinal.isSelected()) return FINAL;
        else return PRELIMINARY;
    }

    public interface Listener {
        void onPlayerChanged(Player player);
    }

    // TODO: RESET should also reset rating list controls
}
