package ru.gofederation.gotha;

import info.vannier.gotha.GeneralParameterSet;
import info.vannier.gotha.PlacementParameterSet;
import info.vannier.gotha.Tournament;
import info.vannier.gotha.TournamentParameterSet;
import ru.gofederation.gotha.model.Game;
import ru.gofederation.gotha.model.PlacementCriterion;
import ru.gofederation.gotha.model.Player;
import ru.gofederation.gotha.model.ScoredPlayer;
import ru.gofederation.gotha.util.ScoreDisplayKt;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class is a reference copy of original ScoredPlayer implementation by Luc Vannier.
 */
public class ScoredPlayerReference extends Player implements java.io.Serializable{
    // For a given round and a given player, the status should be one and only one of the status below

    public final static int UNKNOWN = 0;        // For a given round and a given player, this status should not happen in actual round/actual player
    public final static int ABSENT = -3;        // For a given round and a given player, qualifies the fact that this player has been declared as not participating
    public final static int NOT_ASSIGNED = -2;  // For a given round and a given player, qualifies the fact that this player has been assigned as a neither as bye nor to a real game
    public final static int BYE = -1;           // For a given round and a given player, qualifies the fact that this player has been assigned as a Bye player
    public final static int PAIRED = 1;         // For a given round and a given player, qualifies the fact that this player has been assigned to a real game

    /** generalParameterSet is a part of ScoredPlayer because mms is dependent on McMahon bars and floors */
    private GeneralParameterSet generalParameterSet;

    /** for each round, participation[r] can be : ABSENT, NOT_ASSIGNED, BYE or PAIRED */
    private int[] participation;
    /** games played by this player */
    private Game[] gameArray;


    // First level scores
    private int[] nbwX2;       // number of wins * 2
    private int[] mmsX2;       // mcmahon score * 2
    private int[] stsX2;       // strasbourg score *2
    // Virtual scores : half points are given for not played games
    private int[] nbwVirtualX2;       // number of wins * 2
    private int[] mmsVirtualX2;       // mcmahon score * 2
    private int[] stsVirtualX2;       // Strasbourg score * 2

    // Second level scores
    private int[] cuswX2;      // Sum of successive nbw2
    private int[] cusmX2;      // Sum of successive mms2
    private int[] soswX2;      // Sum of Opponents nbw2
    private int[] soswM1X2;    // Sum of (n-1) Opponents nbw2
    private int[] soswM2X2;    // Sum of (n-2) Opponents nbw2
    private int[] sdswX4;      // Sum of Defeated Opponents nbw2 X2
    private int[] sosmX2;      // Sum of Opponents mms2
    private int[] sosmM1X2;    // Sum of (n-1) Opponents mms2
    private int[] sosmM2X2;    // Sum of (n-2) Opponents mms2
    private int[] sdsmX4;      // Sum of Defeated Opponents mms2 X2
    private int[] sostsX2;     // Sum of Opponents sts2

    private int[] extX2;      // Exploits tentes (based on nbw2, with a weight factor)
    private int[] exrX2;      // Exploits reussis(based on nbw2, with a weight factor)
    // Third level scores
    private int[] ssswX2;      // Sum of opponents sosw2 * 2
    private int[] sssmX2;      // Sum of opponents sosm2 * 2
    // Special Scores

    private int dc;            // Direct Confrontation
    private int sdc;           // Simplified Direct Confrontation

    // Pairing informations. Unlike preceeding data, these informations are computed for one round only : the current one
    public int numberOfGroups;      // Very redundant
    public int groupNumber;         //
    public int groupSize;           // Redundant
    public int innerPlacement;      // placement in homogeneous group (category and mainScore) beteen 0 and size(group) - 1
    public int nbDU;                // Number of Draw-ups
    public int nbDD;                // Number of Draw-downs

    private boolean isValidRoundNumber(int rn){
        if (rn < 0 || rn > participation.length){
            return false;
        }
        else return true;
    }
    public int getParticipation(int rn){
        if (isValidRoundNumber(rn)) return participation[rn];
        else return 0;
    }
    public void setParticipation(int rn, int participation){
        if (isValidRoundNumber(rn)) this.participation[rn] = participation;
        else this.participation[rn] = UNKNOWN;
    }
    public Game getGame(int rn){
        if (isValidRoundNumber(rn)) return gameArray[rn];
        else return null;
    }
    public void setGame(int rn, Game g){
        if (isValidRoundNumber(rn)) gameArray[rn] = g;
    }
    public boolean gameWasPlayed(int rn){
        Game g = getGame(rn);
        if (g == null) return false; // not paired
        return g.getResult().gameWasPlayer();
    }
    public int getNBWX2(int rn){
        if (isValidRoundNumber(rn)) return nbwX2[rn];
        else return 0;
    }
    public void setNBWX2(int rn, int value){
        if (isValidRoundNumber(rn)) nbwX2[rn] = value;
    }
    public int getMMSX2(int rn){
        if (isValidRoundNumber(rn)) return mmsX2[rn];
        else return 0;
    }
    public void setMMSX2(int rn, int value){
        if (isValidRoundNumber(rn)) mmsX2[rn] = value;
    }
    public int getSTSX2(int rn){
        if (isValidRoundNumber(rn)) return stsX2[rn];
        else return 0;
    }
    public void setSTSX2(int rn, int value){
        if (isValidRoundNumber(rn)) stsX2[rn] = value;
    }

    public int getNBWVirtualX2(int rn){
        if (isValidRoundNumber(rn)) return nbwVirtualX2[rn];
        else return 0;
    }
    public void setNBWVirtualX2(int rn, int value){
        if (isValidRoundNumber(rn)) nbwVirtualX2[rn] = value;
    }
    public int getMMSVirtualX2(int rn){
        if (isValidRoundNumber(rn)) return mmsVirtualX2[rn];
        else return 0;
    }
    public void setMMSVirtualX2(int rn, int value){
        if (isValidRoundNumber(rn)) mmsVirtualX2[rn] = value;
    }
    public int getSTSVirtualX2(int rn){
        if (isValidRoundNumber(rn)) return stsVirtualX2[rn];
        else return 0;
    }
    public void setSTSVirtualX2(int rn, int value){
        if (isValidRoundNumber(rn)) stsVirtualX2[rn] = value;
    }

    public int getCUSWX2(int rn){
        if (isValidRoundNumber(rn)) return cuswX2[rn];
        else return 0;
    }
    public void setCUSWX2(int rn, int value){
        if (isValidRoundNumber(rn)) cuswX2[rn] = value;
    }
    public int getCUSMX2(int rn){
        if (isValidRoundNumber(rn)) return cusmX2[rn];
        else return 0;
    }
    public void setCUSMX2(int rn, int value){
        if (isValidRoundNumber(rn)) cusmX2[rn] = value;
    }
    public int getSOSWX2(int rn){
        if (isValidRoundNumber(rn)) return soswX2[rn];
        else return 0;
    }
    public void setSOSWX2(int rn, int value){
        if (isValidRoundNumber(rn)) soswX2[rn] = value;
    }
    public int getSOSWM1X2(int rn){
        if (isValidRoundNumber(rn)) return soswM1X2[rn];
        else return 0;
    }
    public void setSOSWM1X2(int rn, int value){
        if (isValidRoundNumber(rn)) soswM1X2[rn] = value;
    }
    public int getSOSWM2X2(int rn){
        if (isValidRoundNumber(rn)) return soswM2X2[rn];
        else return 0;
    }
    public void setSOSWM2X2(int rn, int value){
        if (isValidRoundNumber(rn)) soswM2X2[rn] = value;
    }
    public int getSDSWX4(int rn){
        if (isValidRoundNumber(rn)) return sdswX4[rn];
        else return 0;
    }
    public void setSDSWX4(int rn, int value){
        if (isValidRoundNumber(rn)) sdswX4[rn] = value;
    }
    public int getSOSMX2(int rn){
        if (isValidRoundNumber(rn)) return sosmX2[rn];
        else return 0;
    }
    public void setSOSMX2(int rn, int value){
        if (isValidRoundNumber(rn)) sosmX2[rn] = value;
    }
    public int getSOSMM1X2(int rn){
        if (isValidRoundNumber(rn)) return sosmM1X2[rn];
        else return 0;
    }
    public void setSOSMM1X2(int rn, int value){
        if (isValidRoundNumber(rn)) sosmM1X2[rn] = value;
    }
    public int getSOSMM2X2(int rn){
        if (isValidRoundNumber(rn)) return sosmM2X2[rn];
        else return 0;
    }
    public void setSOSMM2X2(int rn, int value){
        if (isValidRoundNumber(rn)) sosmM2X2[rn] = value;
    }
    public int getSDSMX4(int rn){
        if (isValidRoundNumber(rn)) return sdsmX4[rn];
        else return 0;
    }
    public void setSDSMX4(int rn, int value){
        if (isValidRoundNumber(rn)) sdsmX4[rn] = value;
    }
    public int getSOSTSX2(int rn){
        if (isValidRoundNumber(rn)) return sostsX2[rn];
        else return 0;
    }
    public void setSOSTSX2(int rn, int value){
        if (isValidRoundNumber(rn)) sostsX2[rn] = value;
    }
    public int getEXTX2(int rn){
        if (isValidRoundNumber(rn)) return extX2[rn];
        else return 0;
    }
    public void setEXTX2(int rn, int value){
        if (isValidRoundNumber(rn)) extX2[rn] = value;
    }
    public int getEXRX2(int rn){
        if (isValidRoundNumber(rn)) return exrX2[rn];
        else return 0;
    }
    public void setEXRX2(int rn, int value){
        if (isValidRoundNumber(rn)) exrX2[rn] = value;
    }
    public int getSSSWX2(int rn){
        if (isValidRoundNumber(rn)) return ssswX2[rn];
        else return 0;
    }
    public void setSSSWX2(int rn, int value){
        if (isValidRoundNumber(rn)) ssswX2[rn] = value;
    }
    public int getSSSMX2(int rn){
        if (isValidRoundNumber(rn)) return sssmX2[rn];
        else return 0;
    }
    public void setSSSMX2(int rn, int value){
        if (isValidRoundNumber(rn)) sssmX2[rn] = value;
    }
    public int getDC(){
        return dc;
    }
    public void setDC(int value){
        dc = value;
    }
    public int getSDC(){
        return sdc;
    }
    public void setSDC(int value){
        sdc = value;
    }

    public ScoredPlayerReference(GeneralParameterSet gps, Player player) {
        super(player);
        this.generalParameterSet = gps;

        int numberOfRounds = generalParameterSet.getNumberOfRounds();
        participation = new int[numberOfRounds];
        gameArray = new Game[numberOfRounds];
        nbwX2  = new int[numberOfRounds];
        mmsX2  = new int[numberOfRounds];
        stsX2  = new int[numberOfRounds];
        nbwVirtualX2  = new int[numberOfRounds];
        mmsVirtualX2  = new int[numberOfRounds];
        stsVirtualX2  = new int[numberOfRounds];

        cuswX2 = new int[numberOfRounds];
        cusmX2 = new int[numberOfRounds];

        soswX2 = new int[numberOfRounds];
        soswM1X2 = new int[numberOfRounds];
        soswM2X2 = new int[numberOfRounds];

        sdswX4 = new int[numberOfRounds];

        sosmX2 = new int[numberOfRounds];
        sosmM1X2 = new int[numberOfRounds];
        sosmM2X2 = new int[numberOfRounds];

        sdsmX4 = new int[numberOfRounds];

        sostsX2 = new int[numberOfRounds];

        extX2  = new int[numberOfRounds];
        exrX2  = new int[numberOfRounds];

        ssswX2 = new int[numberOfRounds];
        sssmX2 = new int[numberOfRounds];

        for (int r = 0; r < numberOfRounds; r++){
            participation[r] = 0;
            gameArray[r] = null;
            nbwX2[r] = 0;
            mmsX2[r] = 0;
            stsX2[r] = 0;
            nbwVirtualX2[r] = 0;
            mmsVirtualX2[r] = 0;
            stsVirtualX2[r] = 0;

            cuswX2[r] = 0;
            cusmX2[r] = 0;

            soswX2[r] = 0;
            soswM1X2[r] = 0;
            soswM2X2[r] = 0;

            sosmX2[r] = 0;
            sosmM1X2[r] = 0;
            sosmM2X2[r] = 0;

            sostsX2[r] = 0;

            extX2[r] = 0;
            exrX2[r] = 0;

            ssswX2[r] = 0;
            sssmX2[r] = 0;
        }
        // dc and sdc are defined for the current round number
        dc = 0;
        sdc = 0;
    }

    public int getCritValue(PlacementCriterion criterion, int rn){
        switch(criterion){
            case NUL    : return 0;                      // Null criterion
            case CAT    : return - category(generalParameterSet);// Category
            case RANK   : return getRank().getValue();      // Rank
            case RATING : return getRating().getValue();    // Rating
            case NBW    : return (rn >= 0) ? nbwX2[rn] : 0;                     // Number of Wins
            case MMS    : return (rn >= 0) ? mmsX2[rn] : 2 * smms(generalParameterSet);  // McMahon score
            case STS    : return (rn >= 0) ? stsX2[rn] : 2 * smms(generalParameterSet);  // STS score

            case SOSW   : return (rn >= 0) ? this.soswX2[rn] : 0;	// Sum of Opponents McMahon scores
            case SOSWM1 : return (rn >= 0) ? this.soswM1X2[rn] : 0;
            case SOSWM2 : return (rn >= 0) ? this.soswM2X2[rn] : 0;
            case SODOSW : return (rn >= 0) ? this.getSdswX4()[rn] : 0;	// Sum of Defeated Opponents Scores
            case SOSOSW : return (rn >= 0) ? this.ssswX2[rn] : 0;	// Sum of opponents SOS
            case CUSSW  : return (rn >= 0) ? this.cuswX2[rn] : 0;	// Cuss

            case SOSM   : return (rn >= 0) ? this.sosmX2[rn] : 0;	// Sum of Opponents McMahon scores
            case SOSMM1 : return (rn >= 0) ? this.sosmM1X2[rn] : 0;
            case SOSMM2 : return (rn >= 0) ? this.sosmM2X2[rn] : 0;
            case SODOSM : return (rn >= 0) ? this.getSdsmX4()[rn] : 0;	// Sum of Defeated Opponents Scores
            case SOSOSM : return (rn >= 0) ? this.sssmX2[rn] : 0;	// Sum of opponents SOS
            case CUSSM  : return (rn >= 0) ? this.cusmX2[rn] : 0;	// Cuss

            case SOSTS  : return (rn >= 0) ? this.sostsX2[rn] : 0;	// Sum of Opponents STS scores

            case EXT    : return (rn >= 0) ? this.extX2[rn] : 0;       // Exploits tentes
            case EXR    : return (rn >= 0) ? this.exrX2[rn] : 0;       // Exploits reussis


            case DC     : return dc;
            case SDC    : return sdc;

            default :
                return 0;
        }
    }

    /**
     * converts a score value into a string
     * fractional part will be formatted as : ½ ¼ ¾
     */
    public String formatScore(PlacementCriterion crit, int roundNumber){
        int value = this.getCritValue(crit, roundNumber);
        int coef = crit.getCoef();
        if (coef == -1)   // only Cat
            return "" + (- value + 1);

        return ScoreDisplayKt.formatScore(value, coef);
    }

    /**
     * @return the sdswX4
     */
    private int[] getSdswX4() {
        return sdswX4;
    }

    /**
     * @return the sdsmX4
     */
    private int[] getSdsmX4() {
        return sdsmX4;
    }

    /**
     * This is a copy of original Tournament.fillBaseScoringInfoIfNecessary() by Luc Vannier.
     *
     * Dedicated to be used by orderedScoredPlayersList If necessary, rebuilds
     * this.htScoredPlayersList and fills base info, ie basic criteria, but
     * neither DC, SDC nor ordering nor group information
     */
    private static Map<String, ScoredPlayerReference> fillBaseScoringInfoIfNecessary(Tournament tournament) throws RemoteException {
        final TournamentParameterSet tournamentParameterSet = tournament.getTournamentParameterSet();
        final List<Game> games = tournament.gamesList();
        // 0) Preparation
        // **************
        GeneralParameterSet gps = tournamentParameterSet.getGeneralParameterSet();
        final Map<String, ScoredPlayerReference> hmScoredPlayers = new HashMap<>();
        for (Player p : tournament.playersHashMap().values()) {
            ScoredPlayerReference sp = new ScoredPlayerReference(tournamentParameterSet.getGeneralParameterSet(), p);
            hmScoredPlayers.put(p.getKeyString(), sp);
        }
        int numberOfRoundsToCompute = gps.getNumberOfRounds();

        // 1) participation
        // ****************
        for (ScoredPlayerReference sp : hmScoredPlayers.values()) {
            for (int r = 0; r < numberOfRoundsToCompute; r++) {
                if (!sp.isParticipating(r)) {
                    sp.setParticipation(r, ScoredPlayer.ABSENT);
                } else {
                    sp.setParticipation(r, ScoredPlayer.NOT_ASSIGNED);    // As an initial status
                }
            }
        }
        for (Game g : games) {
            Player wP = g.getWhitePlayer();
            Player bP = g.getBlackPlayer();
            if (wP == null) {
                continue;
            }
            if (bP == null) {
                continue;
            }
            int r = g.getRound();
            ScoredPlayerReference wSP = hmScoredPlayers.get(wP.getKeyString());
            ScoredPlayerReference bSP = hmScoredPlayers.get(bP.getKeyString());
            wSP.setParticipation(r, ScoredPlayer.PAIRED);
            bSP.setParticipation(r, ScoredPlayer.PAIRED);
        }
        for (int r = 0; r < numberOfRoundsToCompute; r++) {
            Player p = tournament.getByePlayers()[r];
            if (p != null) {
                ScoredPlayerReference sp = hmScoredPlayers.get(p.getKeyString());
                sp.setParticipation(r, ScoredPlayer.BYE);
            }
        }
        for (Game g : games) {
            Player wP = g.getWhitePlayer();
            Player bP = g.getBlackPlayer();
            if (wP == null) {
                continue;
            }
            if (bP == null) {
                continue;
            }
            int r = g.getRound();
            ScoredPlayerReference wSP = hmScoredPlayers.get(wP.getKeyString());
            ScoredPlayerReference bSP = hmScoredPlayers.get(bP.getKeyString());
            wSP.setGame(r, g);
            bSP.setGame(r, g);
        }

        // 2) nbwX2 and mmsX2
        for (int r = 0; r < numberOfRoundsToCompute; r++) {
            // Initialize
            for (ScoredPlayerReference sp : hmScoredPlayers.values()) {
                if (r == 0) {
                    sp.setNBWX2(r, 0);
                    sp.setMMSX2(r, 2 * sp.smms(gps));
                } else {
                    sp.setNBWX2(r, sp.getNBWX2(r - 1));
                    sp.setMMSX2(r, sp.getMMSX2(r - 1));
                }
            }

            // Points from games
            for (Game g : games) {
                if (g.getRound() != r) {
                    continue;
                }
                Player wP = g.getWhitePlayer();
                Player bP = g.getBlackPlayer();
                if (wP == null) {
                    continue;
                }
                if (bP == null) {
                    continue;
                }
                ScoredPlayerReference wSP = hmScoredPlayers.get(wP.getKeyString());
                ScoredPlayerReference bSP = hmScoredPlayers.get(bP.getKeyString());
                switch (g.getResult()) {
                    case BOTH_LOSE:
                    case BOTH_LOSE_BYDEF:
                    case UNKNOWN:
                        break;
                    case WHITE_WINS:
                    case WHITE_WINS_BYDEF:
                        wSP.setNBWX2(r, wSP.getNBWX2(r) + 2);
                        wSP.setMMSX2(r, wSP.getMMSX2(r) + 2);
                        break;
                    case BLACK_WINS:
                    case BLACK_WINS_BYDEF:
                        bSP.setNBWX2(r, bSP.getNBWX2(r) + 2);
                        bSP.setMMSX2(r, bSP.getMMSX2(r) + 2);
                        break;
                    case EQUAL:
                    case EQUAL_BYDEF:
                        wSP.setNBWX2(r, wSP.getNBWX2(r) + 1);
                        wSP.setMMSX2(r, wSP.getMMSX2(r) + 1);
                        bSP.setNBWX2(r, bSP.getNBWX2(r) + 1);
                        bSP.setMMSX2(r, bSP.getMMSX2(r) + 1);
                        break;
                    case BOTH_WIN:
                    case BOTH_WIN_BYDEF:
                        wSP.setNBWX2(r, wSP.getNBWX2(r) + 2);
                        wSP.setMMSX2(r, wSP.getMMSX2(r) + 2);
                        bSP.setNBWX2(r, bSP.getNBWX2(r) + 2);
                        bSP.setMMSX2(r, bSP.getMMSX2(r) + 2);
                        break;
                }
            }
        }
        for (ScoredPlayerReference sp : hmScoredPlayers.values()) {
            int nbPtsNBW2AbsentOrBye = 0;
            int nbPtsMMS2AbsentOrBye = 0;
            for (int r = 0; r < numberOfRoundsToCompute; r++) {
                if (sp.getParticipation(r) == ScoredPlayer.ABSENT) {
                    nbPtsNBW2AbsentOrBye += gps.getGenNBW2ValueAbsent();
                    nbPtsMMS2AbsentOrBye += gps.getGenMMS2ValueAbsent();
                }
                if (sp.getParticipation(r) == ScoredPlayer.BYE) {
                    nbPtsNBW2AbsentOrBye += gps.getGenNBW2ValueBye();
                    nbPtsMMS2AbsentOrBye += gps.getGenMMS2ValueBye();
                }
                int nbPNBW2AB = nbPtsNBW2AbsentOrBye;
                int nbPMMS2AB = nbPtsMMS2AbsentOrBye;
                if (gps.isGenRoundDownNBWMMS()) {
                    nbPNBW2AB = (nbPtsNBW2AbsentOrBye / 2) * 2;
                    nbPMMS2AB = (nbPtsMMS2AbsentOrBye / 2) * 2;
                }
                sp.setNBWX2(r, sp.getNBWX2(r) + nbPNBW2AB);
                sp.setMMSX2(r, sp.getMMSX2(r) + nbPMMS2AB);
            }
        }
        // 2b) STS
        for (ScoredPlayerReference sp : hmScoredPlayers.values()) {
            // First, initialize STS with MMS
            for (int r = 0; r < numberOfRoundsToCompute; r++) {
                sp.setSTSX2(r, sp.getMMSX2(r));
            }

            int nbRounds = gps.getNumberOfRounds();
            // Then, if sp is in topgroup and always winner up to quarterfinal, increase by 2 * 2
            if (sp.getMMSX2(nbRounds - 3) == 2 * (30 + gps.getGenMMBar() + nbRounds - 2)) {
                sp.setSTSX2(nbRounds - 3, sp.getSTSX2(nbRounds - 3) + 4);
                sp.setSTSX2(nbRounds - 2, sp.getSTSX2(nbRounds - 2) + 4);
                sp.setSTSX2(nbRounds - 1, sp.getSTSX2(nbRounds - 1) + 4);
            }
            // Then, if sp is in topgroup and always winner up to semifinal,    increase by 2 * 2
            if (sp.getMMSX2(nbRounds - 2) == 2 * (30 + gps.getGenMMBar() + nbRounds - 1)) {
                sp.setSTSX2(nbRounds - 2, sp.getSTSX2(nbRounds - 2) + 4);
                sp.setSTSX2(nbRounds - 1, sp.getSTSX2(nbRounds - 1) + 4);
            }
        }

        // 2bis) nbwVirtualX2 and mmsVirtualX2
        for (int r = 0; r < numberOfRoundsToCompute; r++) {
            // Initialize
            for (ScoredPlayerReference sp : hmScoredPlayers.values()) {
                if (r == 0) {
                    sp.setNBWVirtualX2(r, 0);
                    sp.setMMSVirtualX2(r, 2 * sp.smms(gps));
                } else {
                    sp.setNBWVirtualX2(r, sp.getNBWVirtualX2(r - 1));
                    sp.setMMSVirtualX2(r, sp.getMMSVirtualX2(r - 1));
                }
            }

            // Points from games
            for (Game g : games) {
                if (g.getRound() != r) {
                    continue;
                }
                Player wP = g.getWhitePlayer();
                Player bP = g.getBlackPlayer();
                if (wP == null) {
                    continue;
                }
                if (bP == null) {
                    continue;
                }
                ScoredPlayerReference wSP = hmScoredPlayers.get(wP.getKeyString());
                ScoredPlayerReference bSP = hmScoredPlayers.get(bP.getKeyString());
                switch (g.getResult()) {
                    case BOTH_LOSE:
                    case BOTH_LOSE_BYDEF: // All "BYDEF" results are separately processed
                    case WHITE_WINS_BYDEF:
                    case BLACK_WINS_BYDEF:
                    case EQUAL_BYDEF:
                    case BOTH_WIN_BYDEF:
                    case UNKNOWN:
                        break;
                    case WHITE_WINS:
                        wSP.setNBWVirtualX2(r, wSP.getNBWVirtualX2(r) + 2);
                        wSP.setMMSVirtualX2(r, wSP.getMMSVirtualX2(r) + 2);
                        break;
                    case BLACK_WINS:
                        bSP.setNBWVirtualX2(r, bSP.getNBWVirtualX2(r) + 2);
                        bSP.setMMSVirtualX2(r, bSP.getMMSVirtualX2(r) + 2);
                        break;
                    case EQUAL:
                        wSP.setNBWVirtualX2(r, wSP.getNBWVirtualX2(r) + 1);
                        wSP.setMMSVirtualX2(r, wSP.getMMSVirtualX2(r) + 1);
                        bSP.setNBWVirtualX2(r, bSP.getNBWVirtualX2(r) + 1);
                        bSP.setMMSVirtualX2(r, bSP.getMMSVirtualX2(r) + 1);
                        break;
                    case BOTH_WIN:
                        wSP.setNBWVirtualX2(r, wSP.getNBWVirtualX2(r) + 2);
                        wSP.setMMSVirtualX2(r, wSP.getMMSVirtualX2(r) + 2);
                        bSP.setNBWVirtualX2(r, bSP.getNBWVirtualX2(r) + 2);
                        bSP.setMMSVirtualX2(r, bSP.getMMSVirtualX2(r) + 2);
                        break;
                }
            }

        }
        for (ScoredPlayerReference sp : hmScoredPlayers.values()) {
            int nbVPX2 = 0;
            for (int r = 0; r < numberOfRoundsToCompute; r++) {
                if (!sp.gameWasPlayed(r)) nbVPX2++;
                sp.setNBWVirtualX2(r, sp.getNBWVirtualX2(r) + nbVPX2);
                sp.setMMSVirtualX2(r, sp.getMMSVirtualX2(r) + nbVPX2);
            }
        }

        // 3) CUSSW and CUSSM
        for (ScoredPlayerReference sp : hmScoredPlayers.values()) {
            sp.setCUSWX2(0, sp.getNBWX2(0));
            sp.setCUSMX2(0, sp.getMMSX2(0));
            for (int r = 1; r < numberOfRoundsToCompute; r++) {
                sp.setCUSWX2(r, sp.getCUSWX2(r - 1) + sp.getNBWX2(r));
                sp.setCUSMX2(r, sp.getCUSMX2(r - 1) + sp.getMMSX2(r));
            }
        }

        // 4.1) SOSW, SOSWM1, SOSWM2,SODOSW
        boolean bVirtual = tournamentParameterSet.getGeneralParameterSet().isGenCountNotPlayedGamesAsHalfPoint();
        for (int r = 0; r < numberOfRoundsToCompute; r++) {
            for (ScoredPlayerReference sp : hmScoredPlayers.values()) {
                int[] oswX2 = new int[numberOfRoundsToCompute];
                int[] doswX4 = new int[numberOfRoundsToCompute];    // Defeated opponents score
//                int[] osmX2  = new int[numberOfRoundsToCompute];
//                int[] dosmX4 = new int[numberOfRoundsToCompute];    // Defeated opponents score
//                int[] ostsX2 = new int[numberOfRoundsToCompute];

                for (int rr = 0; rr <= r; rr++) {
                    if (sp.getParticipation(rr) != ScoredPlayer.PAIRED) {
                        oswX2[rr] = 0;
                        doswX4[rr] = 0;
//                        osmX2[rr] = 2 * sp.smms(gps);
//                        ostsX2[rr] = 2 * sp.smms(gps);
                    } else {
                        Game g = sp.getGame(rr);
                        Player opp = tournament.opponent(g, sp);
                        int result = tournament.getWX2(g, sp);

                        ScoredPlayerReference sOpp = hmScoredPlayers.get(opp.getKeyString());
                        if (bVirtual){
                            oswX2[rr] = sOpp.getNBWVirtualX2(r);
                        }
                        else{
                            oswX2[rr] = sOpp.getNBWX2(r);
                        }
                        doswX4[rr] = oswX2[rr] * result;
                    }
                }
                int sosX2 = 0;
                int sdsX4 = 0;
                for (int rr = 0; rr <= r; rr++) {
                    sosX2 += oswX2[rr];
                    sdsX4 += doswX4[rr];
                }
                sp.setSOSWX2(r, sosX2);
                sp.setSDSWX4(r, sdsX4);

                // soswM1X2, soswM2X2
                int sosM1X2 = 0;
                int sosM2X2 = 0;
                if (r == 0) {
                    sosM1X2 = 0;
                    sosM2X2 = 0;
                } else if (r == 1) {
                    sosM1X2 = Math.max(oswX2[0], oswX2[1]);
                    sosM2X2 = 0;
                } else {
                    int rMin = 0;
                    for (int rr = 1; rr <= r; rr++) {
                        if (oswX2[rr] < oswX2[rMin]) {
                            rMin = rr;
                        }
                    }
                    int rMin2 = 0;
                    if (rMin == 0) {
                        rMin2 = 1;
                    }
                    for (int rr = 0; rr <= r; rr++) {
                        if (rr == rMin) {
                            continue;
                        }
                        if (oswX2[rr] < oswX2[rMin2]) {
                            rMin2 = rr;
                        }
                    }
                    sosM1X2 = sp.getSOSWX2(r) - oswX2[rMin];
                    sosM2X2 = sosM1X2 - oswX2[rMin2];
                }
                sp.setSOSWM1X2(r, sosM1X2);
                sp.setSOSWM2X2(r, sosM2X2);
            }
        }

        // 4.2) SOSM, SOSMM1, SOSMM2, SODOSM, SOSTS
        for (int r = 0; r < numberOfRoundsToCompute; r++) {
            for (ScoredPlayerReference sp : hmScoredPlayers.values()) {
                int[] osmX2  = new int[numberOfRoundsToCompute];
                int[] dosmX4 = new int[numberOfRoundsToCompute];    // Defeated opponents score
                int[] ostsX2 = new int[numberOfRoundsToCompute];
                for (int rr = 0; rr <= r; rr++) {
                    if (sp.getParticipation(rr) != ScoredPlayer.PAIRED) {
                        osmX2[rr] = 2 * sp.smms(gps);
                        ostsX2[rr] = 2 * sp.smms(gps);
                    } else {
                        Game g = sp.getGame(rr);
                        Player opp = tournament.opponent(g, sp);
                        int result = tournament.getWX2(g, sp);
                        ScoredPlayerReference sOpp = hmScoredPlayers.get(opp.getKeyString());

                        if (bVirtual){
                            osmX2[rr] = sOpp.getMMSVirtualX2(r);
                            ostsX2[rr] = sOpp.getSTSVirtualX2(r);
                        }
                        else{
                            osmX2[rr] = sOpp.getMMSX2(r);
                            ostsX2[rr] = sOpp.getSTSX2(r);
                        }

                        // osmX2[rr] = sOpp.getMMSX2(r);
                        // ostsX2[rr] = sOpp.getSTSX2(r);

                        if (g.getWhitePlayer().hasSameKeyString(sp)) {
                            osmX2[rr] += 2 * g.getHandicap();
                            ostsX2[rr] += 2 * g.getHandicap();
                        } else {
                            osmX2[rr] -= 2 * g.getHandicap();
                            ostsX2[rr] -= 2 * g.getHandicap();
                        }
                        dosmX4[rr] = osmX2[rr] * tournament.getWX2(g, sp);

                    }
                }
                int sosX2 = 0;
                int sdsX4 = 0;
                int sostsX2 = 0;
                for (int rr = 0; rr <= r; rr++) {
                    sosX2 += osmX2[rr];
                    sdsX4 += dosmX4[rr];
                    sostsX2 += ostsX2[rr];
                }
                sp.setSOSMX2(r, sosX2);
                sp.setSDSMX4(r, sdsX4);
                sp.setSOSTSX2(r, sostsX2);

                // sosmM1X2, sosmM2X2
                int sosM1X2 = 0;
                int sosM2X2 = 0;
                if (r == 0) {
                    sosM1X2 = 0;
                    sosM2X2 = 0;
                } else if (r == 1) {
                    sosM1X2 = Math.max(osmX2[0], osmX2[1]);
                    sosM2X2 = 0;
                } else {
                    int rMin = 0;
                    for (int rr = 1; rr <= r; rr++) {
                        if (osmX2[rr] < osmX2[rMin]) {
                            rMin = rr;
                        }
                    }
                    int rMin2 = 0;
                    if (rMin == 0) {
                        rMin2 = 1;
                    }
                    for (int rr = 0; rr <= r; rr++) {
                        if (rr == rMin) {
                            continue;
                        }
                        if (osmX2[rr] < osmX2[rMin2]) {
                            rMin2 = rr;
                        }
                    }
                    sosM1X2 = sp.getSOSMX2(r) - osmX2[rMin];
                    sosM2X2 = sosM1X2 - osmX2[rMin2];
                    sp.setSOSMM1X2(r, sosM1X2);
                    sp.setSOSMM2X2(r, sosM2X2);
                }
            }
        }



        // 5) SOSOSW and SOSOSM
        for (int r = 0; r < numberOfRoundsToCompute; r++) {
            for (ScoredPlayerReference sp : hmScoredPlayers.values()) {
                int sososwX2 = 0;
                int sososmX2 = 0;
                for (int rr = 0; rr <= r; rr++) {
                    if (sp.getParticipation(rr) != ScoredPlayer.PAIRED) {
                        sososwX2 += 0;
                        sososmX2 += 2 * sp.smms(gps) * (r + 1);
                    } else {
                        Game g = sp.getGame(rr);
                        Player opp;
                        if (g.getWhitePlayer().hasSameKeyString(sp)) {
                            opp = g.getBlackPlayer();
                        } else {
                            opp = g.getWhitePlayer();
                        }
                        ScoredPlayerReference sOpp = hmScoredPlayers.get(opp.getKeyString());
                        sososwX2 += sOpp.getSOSWX2(r);
                        sososmX2 += sOpp.getSOSMX2(r);
                    }
                }
                sp.setSSSWX2(r, sososwX2);
                sp.setSSSMX2(r, sososmX2);
            }
        }


        // 6)  EXT EXR
        for (int r = 0; r < numberOfRoundsToCompute; r++) {
            for (ScoredPlayerReference sp : hmScoredPlayers.values()) {
                int extX2 = 0;
                int exrX2 = 0;
                for (int rr = 0; rr <= r; rr++) {
                    if (sp.getParticipation(rr) != ScoredPlayer.PAIRED) {
                        continue;
                    }
                    Game g = sp.getGame(rr);
                    Player opp;
                    boolean spWasWhite;
                    if (g.getWhitePlayer().hasSameKeyString(sp)) {
                        opp = g.getBlackPlayer();
                        spWasWhite = true;
                    } else {
                        opp = g.getWhitePlayer();
                        spWasWhite = false;
                    }
                    ScoredPlayerReference sOpp = hmScoredPlayers.get(opp.getKeyString());

                    int realHd = g.getHandicap();
                    if (!spWasWhite) {
                        realHd = -realHd;
                    }
                    int naturalHd = sp.getRank().minus(sOpp.getRank());
                    int coef = 0;
                    if (realHd - naturalHd <= 0) {
                        coef = 0;
                    }
                    if (realHd - naturalHd == 0) {
                        coef = 1;
                    }
                    if (realHd - naturalHd == 1) {
                        coef = 2;
                    }
                    if (realHd - naturalHd >= 2) {
                        coef = 3;
                    }
                    extX2 += sOpp.getNBWX2(r) * coef;
                    boolean bWin = false;
                    if (spWasWhite
                        && (g.getResult() == Game.Result.WHITE_WINS
                        || g.getResult() == Game.Result.WHITE_WINS_BYDEF
                        || g.getResult() == Game.Result.BOTH_WIN
                        || g.getResult() == Game.Result.BOTH_WIN_BYDEF)) {
                        bWin = true;
                    }
                    if (!spWasWhite
                        && (g.getResult() == Game.Result.BLACK_WINS
                        || g.getResult() == Game.Result.BLACK_WINS_BYDEF
                        || g.getResult() == Game.Result.BOTH_WIN
                        || g.getResult() == Game.Result.BOTH_WIN_BYDEF)) {
                        bWin = true;
                    }
                    if (bWin) {
                        exrX2 += sOpp.getNBWX2(r) * coef;
                    }
                }
                sp.setEXTX2(r, extX2);
                sp.setEXRX2(r, exrX2);
            }
        }

        return hmScoredPlayers;
    }

    public static List<ScoredPlayerReference> getOrderedScoredPlayerList(Tournament tournament, int roundNumber) throws RemoteException {
        final Map<String, ScoredPlayerReference> hmScoredPlayers = fillBaseScoringInfoIfNecessary(tournament);
        final PlacementParameterSet pps = tournament.getTournamentParameterSet().getPlacementParameterSet();
        // Order hmScoredPlayers into alOrderedScoredPlayers according to Main criteria (before DC and SDC)
        ArrayList<ScoredPlayerReference> alOrderedScoredPlayers = new ArrayList<>(hmScoredPlayers.values());

        PlacementCriterion[] crit = pps.getPlaCriteria();
        PlacementCriterion[] primaryCrit = new PlacementCriterion[crit.length];
        int iCritDir = crit.length; // set to first CD or SDC criterion if found
        for (int iC = 0; iC < crit.length; iC++) {
            if (crit[iC] == PlacementCriterion.DC || crit[iC] == PlacementCriterion.SDC) {
                iCritDir = iC;
                break;
            } else {
                primaryCrit[iC] = crit[iC];
            }
        }
        for (int iC = iCritDir; iC < crit.length; iC++) {
            primaryCrit[iC] = PlacementCriterion.NUL;
        }

        // Sort on primary criteria
        ScoredPlayerComparator spc = new ScoredPlayerComparator(primaryCrit, roundNumber, false);
        Collections.sort(alOrderedScoredPlayers, spc);
        // Compute (Simplified) Direct Confrontation criteria
        fillDirScoringInfo(alOrderedScoredPlayers, tournament.gamesList(), roundNumber, pps);

        // And now, complete sort
        spc = new ScoredPlayerComparator(crit, roundNumber, false);
        Collections.sort(alOrderedScoredPlayers, spc);

        return alOrderedScoredPlayers;
    }

    /**
     * Defines dir attributes (dc and sdc) of all players in alSP if this
     * criterion exists in plaCriteria Dir value is a tie break (also said Dir
     * Confrontation) fillDirScoringInfo makes preparation work and subcontracts
     * actual computation to defineDirForExAequoGroup
     *
     * @param alSP ArrayList of ScoredPlayers for which Dir Criterion should be
     * made
     */
    private static boolean fillDirScoringInfo(List<ScoredPlayerReference> alSP, List<Game> games, int roundNumber, PlacementParameterSet pps) {
        // By default dir is 0
        for (ScoredPlayerReference sP : alSP) {
            sP.setSDC(0);
            sP.setDC(0);
        }

        PlacementCriterion[] crit = pps.getPlaCriteria();
        int nbCritBeforeDir = -1;
        for (int cr = 0; cr < crit.length; cr++) {
            if (crit[cr] == PlacementCriterion.DC || crit[cr] == PlacementCriterion.SDC) {
                nbCritBeforeDir = cr;
                break;
            }
        }
        if (nbCritBeforeDir < 0) {
            return false;
        }

        int numPlayer = 0;

        while (numPlayer < alSP.size()) {
            ArrayList<ScoredPlayerReference> alExAequoBeforeDirScoredPlayers = new ArrayList<>();
            alExAequoBeforeDirScoredPlayers.add(alSP.get(numPlayer));
            int[] critValue = new int[nbCritBeforeDir];
            for (int cr = 0; cr < nbCritBeforeDir; cr++) {
                critValue[cr] = alSP.get(numPlayer).getCritValue(crit[cr], roundNumber);
            }

            numPlayer++;
            while (numPlayer < alSP.size()) {
                ScoredPlayerReference sP = alSP.get(numPlayer);
                boolean bCandidateOK = true;
                for (int cr = 0; cr < nbCritBeforeDir; cr++) {
                    if (sP.getCritValue(crit[cr], roundNumber) != critValue[cr]) {
                        bCandidateOK = false;
                    }
                }
                if (!bCandidateOK) {
                    break;
                } else {
                    alExAequoBeforeDirScoredPlayers.add(sP);
                    numPlayer++;
                }
            }
            defineDirForExAequoGroup(alExAequoBeforeDirScoredPlayers, games, roundNumber, pps);
        }
        return true;
    }

    /**
     * computes DC or SDC for players of alExAequoBeforeDirScoredPlayers, taking
     * in account games in rounds 0 to to roundnumber included
     */
    private static void defineDirForExAequoGroup(List<ScoredPlayerReference> alExAequoBeforeDirScoredPlayers, List<Game> games, int roundNumber, PlacementParameterSet pps) {
        int nbP = alExAequoBeforeDirScoredPlayers.size();
        if (nbP <= 1) {
            return;
        }

        int[][] pair = defineAPairMatrix(alExAequoBeforeDirScoredPlayers, games, roundNumber);

        // limit to [-1 .. +1]
        for (int i = 0; i < nbP; i++) {
            for (int j = 0; j < nbP; j++) {
                if (i == j) {
                    pair[i][j] = 0;
                }
                if (pair[i][j] >= 1) {
                    pair[i][j] = 1;
                }
                if (pair[i][j] <= -1) {
                    pair[i][j] = -1;
                }
            }
        }

        // SDC Algorithm
        // Relevant only if every possible pair of players have played at least once
        // and, if more than one game has been played between both, their algebric sum  is != 0
        boolean bRelevant = true;
        for (int i = 0; i < nbP; i++) {
            for (int j = 0; j < nbP; j++) {
                if (i != j && pair[i][j] == 0) {
                    bRelevant = false;
                }
            }
        }
        if (bRelevant) {
            for (int i = 0; i < nbP; i++) {
                ScoredPlayerReference sP = alExAequoBeforeDirScoredPlayers.get(i);
                for (int j = 0; j < nbP; j++) {
                    if (pair[i][j] == 1) {
                        sP.setSDC(sP.getSDC() + 1);
                    }
                }
            }
        }

        // DC Algorithm
        // Order alExAequoBeforeDirScoredPlayers according to secondary criteria
        PlacementCriterion[] crit = pps.getPlaCriteria();
        PlacementCriterion[] secCrit = Arrays.copyOf(crit, crit.length);
        for (int iC = 0; iC < secCrit.length; iC++) {
            if (crit[iC] != PlacementCriterion.DC) {
                secCrit[iC] = PlacementCriterion.NUL;
            } else {
                secCrit[iC] = PlacementCriterion.NUL;
                break;
            }
        }
        ScoredPlayerComparator spc = new ScoredPlayerComparator(secCrit, roundNumber, true);
        Collections.sort(alExAequoBeforeDirScoredPlayers, spc);
        int[] place = new int[alExAequoBeforeDirScoredPlayers.size()];
        place[0] = 0;
        // Give the same place to players with same values as secondary criteria
        for (int i = 1; i < alExAequoBeforeDirScoredPlayers.size(); i++) {
            if (spc.compare(alExAequoBeforeDirScoredPlayers.get(i), alExAequoBeforeDirScoredPlayers.get(i - 1)) == 0) {
                place[i] = place[i - 1];
            } else {
                place[i] = i;
            }
        }
        // Prepare sc for Matthieu
        int[] sc = new int[alExAequoBeforeDirScoredPlayers.size()];
        for (int i = 0; i < nbP; i++) {
            sc[i] = nbP - place[i];
        }
        // and make a pair[][] up to date after sorting
        pair = defineAPairMatrix(alExAequoBeforeDirScoredPlayers, games, roundNumber);

        // It's up to you, Matthieu!
        mw.go.confrontation.Confrontation conf = new mw.go.confrontation.Confrontation();
        for (int i = 0; i < nbP; i++) {
            conf.newPlayer(i, sc[i]);
        }
        for (int i = 0; i < nbP; i++) {
            for (int j = 0; j < nbP; j++) {
                if (pair[i][j] == 1) {
                    conf.newGame(i, j);
                }
            }
        }

        java.util.List<Integer> result = conf.topologicalSort(true, true);

        int max = 0;
        for (int i = 0; i < nbP; i++) {
            ScoredPlayerReference sp = alExAequoBeforeDirScoredPlayers.get(i);
            max = Math.max(max, conf.getPlayer(i).rank);
        }
        // Reverse order and store
        for (int i = 0; i < nbP; i++) {
            ScoredPlayerReference sp = alExAequoBeforeDirScoredPlayers.get(i);
            int dc = max - conf.getPlayer(i).rank;
            sp.setDC(dc);
        }
    }

    /**
     * Checks all games played between players references by alSP, and, for each
     * pair of players (i, j) stores the algebric sum of results between them.
     * For instance if player 2 won against player 3, the returned array will
     * contain +1 in [2][3] and -1 in [3][2]. This method is dedicated to be
     * used by defineDirForExAequoGroup
     *
     * @param alSP list of ScoredPlayers to be checked
     * @param roundNumber last round number to be taken in account
     * @return the
     */
    private static int[][] defineAPairMatrix(List<ScoredPlayerReference> alSP, List<Game> games, int roundNumber) {
        int nbP = alSP.size();
        int[][] pair = new int[nbP][nbP];

        for (int i = 0; i < nbP; i++) {
            for (int j = 0; j < nbP; j++) {
                pair[i][j] = 0;
            }
        }
        for (Game g : games) {
            if (g.getRound() > roundNumber) {
                continue;
            }
            Player wP = g.getWhitePlayer();
            Player bP = g.getBlackPlayer();
            int numWP = -1;
            int numBP = -1;
            for (int i = 0; i < nbP; i++) {
                Player p = alSP.get(i);

                if (p.hasSameKeyString(wP)) {
                    numWP = i;
                }
                if (p.hasSameKeyString(bP)) {
                    numBP = i;
                }
            }
            if (numWP < 0) {
                continue;
            }
            if (numBP < 0) {
                continue;
            }
            Game.Result res = g.getResult();
            if (res == Game.Result.WHITE_WINS) {
                pair[numWP][numBP]++;
                pair[numBP][numWP]--;
            }
            if (res == Game.Result.BLACK_WINS) {
                pair[numWP][numBP]--;
                pair[numBP][numWP]++;
            }
        }
        return pair;
    }

    /**
     * computes DC or SDC for players of alExAequoBeforeDirScoredPlayers, taking
     * in account games in rounds 0 to to roundnumber included
     */
    private static void defineDirForExAequoGroup(ArrayList<ScoredPlayerReference> alExAequoBeforeDirScoredPlayers, List<Game> games, int roundNumber, PlacementParameterSet pps) {
        int nbP = alExAequoBeforeDirScoredPlayers.size();
        if (nbP <= 1) {
            return;
        }

        int[][] pair = defineAPairMatrix(alExAequoBeforeDirScoredPlayers, games, roundNumber);

        // limit to [-1 .. +1]
        for (int i = 0; i < nbP; i++) {
            for (int j = 0; j < nbP; j++) {
                if (i == j) {
                    pair[i][j] = 0;
                }
                if (pair[i][j] >= 1) {
                    pair[i][j] = 1;
                }
                if (pair[i][j] <= -1) {
                    pair[i][j] = -1;
                }
            }
        }

        // SDC Algorithm
        // Relevant only if every possible pair of players have played at least once
        // and, if more than one game has been played between both, their algebric sum  is != 0
        boolean bRelevant = true;
        for (int i = 0; i < nbP; i++) {
            for (int j = 0; j < nbP; j++) {
                if (i != j && pair[i][j] == 0) {
                    bRelevant = false;
                }
            }
        }
        if (bRelevant) {
            for (int i = 0; i < nbP; i++) {
                ScoredPlayerReference sP = alExAequoBeforeDirScoredPlayers.get(i);
                for (int j = 0; j < nbP; j++) {
                    if (pair[i][j] == 1) {
                        sP.setSDC(sP.getSDC() + 1);
                    }
                }
            }
        }

        // DC Algorithm
        // Order alExAequoBeforeDirScoredPlayers according to secondary criteria
        PlacementCriterion[] crit = pps.getPlaCriteria();
        PlacementCriterion[] secCrit = Arrays.copyOf(crit, crit.length);
        for (int iC = 0; iC < secCrit.length; iC++) {
            if (crit[iC] != PlacementCriterion.DC) {
                secCrit[iC] = PlacementCriterion.NUL;
            } else {
                secCrit[iC] = PlacementCriterion.NUL;
                break;
            }
        }
        ScoredPlayerComparator spc = new ScoredPlayerComparator(secCrit, roundNumber, true);
        Collections.sort(alExAequoBeforeDirScoredPlayers, spc);
        int[] place = new int[alExAequoBeforeDirScoredPlayers.size()];
        place[0] = 0;
        // Give the same place to players with same values as secondary criteria
        for (int i = 1; i < alExAequoBeforeDirScoredPlayers.size(); i++) {
            if (spc.compare(alExAequoBeforeDirScoredPlayers.get(i), alExAequoBeforeDirScoredPlayers.get(i - 1)) == 0) {
                place[i] = place[i - 1];
            } else {
                place[i] = i;
            }
        }
        // Prepare sc for Matthieu
        int[] sc = new int[alExAequoBeforeDirScoredPlayers.size()];
        for (int i = 0; i < nbP; i++) {
            sc[i] = nbP - place[i];
        }
        // and make a pair[][] up to date after sorting
        pair = defineAPairMatrix(alExAequoBeforeDirScoredPlayers, games, roundNumber);

        // It's up to you, Matthieu!
        mw.go.confrontation.Confrontation conf = new mw.go.confrontation.Confrontation();
        for (int i = 0; i < nbP; i++) {
            conf.newPlayer(i, sc[i]);
        }
        for (int i = 0; i < nbP; i++) {
            for (int j = 0; j < nbP; j++) {
                if (pair[i][j] == 1) {
                    conf.newGame(i, j);
                }
            }
        }

        java.util.List<Integer> result = conf.topologicalSort(true, true);

        int max = 0;
        for (int i = 0; i < nbP; i++) {
            ScoredPlayerReference sp = alExAequoBeforeDirScoredPlayers.get(i);
            max = Math.max(max, conf.getPlayer(i).rank);
        }
        // Reverse order and store
        for (int i = 0; i < nbP; i++) {
            ScoredPlayerReference sp = alExAequoBeforeDirScoredPlayers.get(i);
            int dc = max - conf.getPlayer(i).rank;
            sp.setDC(dc);
        }
    }
}
