package info.vannier.gotha;

import ru.gofederation.gotha.model.HalfGame;
import ru.gofederation.gotha.presenter.HalfGameResultsKt;

import java.util.ArrayList;
import java.util.List;


/**
 * ScoredPlayer represents a player and all useful scoring information (nbw, mms, ... dc, sdc)
 *
 * All datas except dc and sdc are updated by (and only by) fillBaseScoringInfo(), according to gps as defined in current tournament
 * dc and sdc are updated by (and only by) fillDirScoringInfo(), according to pps and round number  as defined in argument.
 *
 * ScoredPlayer does not contain any information about pairing
 * @author Luc Vannier
 */
public class ScoredPlayer {
    /**
     * Generate strings with a "oooortch" format
     * oooo being opponent number,
     * r being the result "+", "-", "=" or "?"
     * t (type) is either "/" for normal results or "!" for by default results
     * c being the colour, "w", "b" or "?"
     * h being handicap, "0" ... "9"
     * @param tournament Tournament. Used for placement criteria and for absent and values scores, and also for retrieving ScoredPlayers
     */
    public static String[][] halfGamesStrings(List<ru.gofederation.gotha.model.ScoredPlayer> alOrderedScoredPlayers, int roundNumber, TournamentInterface tournament, boolean bFull) {
        HalfGame[][] halfGames = HalfGameResultsKt.halfGames(alOrderedScoredPlayers, roundNumber, tournament);
        String[][] hg = new String[roundNumber +1][alOrderedScoredPlayers.size()];
        for (int i = 0; i < alOrderedScoredPlayers.size(); i++) {
            for (int r = 0; r <= roundNumber; r++) {
                hg[r][i] = halfGames[r][i].toPaddedString(bFull);
            }
        }
        return hg;
    }

    /**
     * Generate an array of strings representing placement between 1 and number of players.
     * Basically placement is the position in alOrderedScoredPlayers + 1.
     * Except for ex-aequos
     */
    public static String[] positionStrings(ArrayList<ru.gofederation.gotha.model.ScoredPlayer> alOrderedScoredPlayers, int roundNumber, TournamentParameterSet tps) {
        PlacementParameterSet pps = tps.getPlacementParameterSet();
        ScoredPlayerComparator spc = new ScoredPlayerComparator(pps.getPlaCriteria(), roundNumber, true);
        int[] place = new int[alOrderedScoredPlayers.size()];
        if (place.length > 0) place[0] = 0;
        for (int i = 1; i < alOrderedScoredPlayers.size(); i++){
            if (spc.compare(alOrderedScoredPlayers.get(i), alOrderedScoredPlayers.get(i-1)) == 0) place[i] = place[i-1];
            else place[i] = i;
        }
        String[] strPlace = new String[alOrderedScoredPlayers.size()];
        for (int i = 0; i < alOrderedScoredPlayers.size(); i++){
            if ( i > 0 && place[i] == place[i-1] ) strPlace[i] = "    ";
            else strPlace[i] = "    " + (place[i] + 1);
            strPlace[i] = strPlace[i].substring(strPlace[i].length() - 4);
        }
        return strPlace;
    }

    /**
     * Generate a array of strings representing placement inside category between 1 and number of players.
     * Basically placement is the position in alOrderedScoredPlayers + 1.
     * Except for ex-aequos
     */
    public static String[] catPositionStrings(List<ru.gofederation.gotha.model.ScoredPlayer> alOrderedScoredPlayers, int roundNumber, TournamentParameterSet tps) {
        PlacementParameterSet pps = tps.getPlacementParameterSet();
        GeneralParameterSet gps = tps.getGeneralParameterSet();
        ScoredPlayerComparator spc = new ScoredPlayerComparator(pps.getPlaCriteria(), roundNumber, true);
        int[] place = new int[alOrderedScoredPlayers.size()];
        if (place.length > 0) place[0] = 0;
        int curCat = 0;
        int nbPlayersBeforeCurCat = 0;
        for (int i = 1; i < alOrderedScoredPlayers.size(); i++){
            int newCat = alOrderedScoredPlayers.get(i).category(gps);
            if (newCat != curCat){
                curCat = newCat;
                nbPlayersBeforeCurCat = i;
                place[i] = 0;
            }
            if (spc.compare(alOrderedScoredPlayers.get(i), alOrderedScoredPlayers.get(i-1)) == 0) place[i] = place[i-1];
            else place[i] = i - nbPlayersBeforeCurCat;
        }
        String[] strPlace = new String[alOrderedScoredPlayers.size()];
        for (int i = 0; i < alOrderedScoredPlayers.size(); i++){
            if (i > 0 && alOrderedScoredPlayers.get(i).category(gps) != alOrderedScoredPlayers.get(i-1).category(gps) )
                strPlace[i] = "    " + (place[i] + 1);
            else if (i > 0 && place[i] == place[i-1] ) strPlace[i] = "    ";
            else strPlace[i] = "    " + (place[i] + 1);
            strPlace[i] = strPlace[i].substring(strPlace[i].length() - 4);
        }
        return strPlace;
    }
}
