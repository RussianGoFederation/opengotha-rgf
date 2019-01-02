package info.vannier.gotha;

import java.util.Arrays;

import ru.gofederation.gotha.util.GothaLocale;

public final class PlacementParameterSet implements java.io.Serializable{
    private static final long serialVersionUID = Gotha.GOTHA_DATA_VERSION;
    
    final static int PLA_MAX_NUMBER_OF_CRITERIA = 6; 
    
    private PlacementCriterion[] plaCriteria;
    
    final static int PLA_SMMS_CORR_MAX    =  2;
    final static int PLA_SMMS_CORR_MIN    = -1;
    
    public PlacementParameterSet() {             
        plaCriteria = new PlacementCriterion[PLA_MAX_NUMBER_OF_CRITERIA];
        Arrays.fill(plaCriteria, PlacementCriterion.NUL);
    }
     
    public PlacementParameterSet(PlacementParameterSet pps) {
        PlacementCriterion[] plaCritModel = pps.getPlaCriteria();
        PlacementCriterion[] plaCrit = new PlacementCriterion[PLA_MAX_NUMBER_OF_CRITERIA];
        System.arraycopy(plaCritModel, 0, plaCrit, 0, PLA_MAX_NUMBER_OF_CRITERIA);
        this.plaCriteria = plaCrit;
    }
    
    public void initForMM() {
        plaCriteria = new PlacementCriterion[PLA_MAX_NUMBER_OF_CRITERIA];
        Arrays.fill(plaCriteria, PlacementCriterion.NUL);
        plaCriteria[0] = PlacementCriterion.MMS;
        plaCriteria[1] = PlacementCriterion.SOSM;
        plaCriteria[2] = PlacementCriterion.SOSOSM;
    }
    
    public void initForSwiss() {
        plaCriteria = new PlacementCriterion[PLA_MAX_NUMBER_OF_CRITERIA];
        Arrays.fill(plaCriteria, PlacementCriterion.NUL);
        plaCriteria[0] = PlacementCriterion.NBW;
        plaCriteria[1] = PlacementCriterion.SOSW;
        plaCriteria[2] = PlacementCriterion.SOSOSW;
    }
    
    public void initForSwissCat() {
        plaCriteria = new PlacementCriterion[PLA_MAX_NUMBER_OF_CRITERIA];
        Arrays.fill(plaCriteria, PlacementCriterion.NUL);
        plaCriteria[0] = PlacementCriterion.CAT;
        plaCriteria[1] = PlacementCriterion.NBW;
        plaCriteria[2] = PlacementCriterion.EXT;
        plaCriteria[3] = PlacementCriterion.EXR;
    }
    
    public String checkCriteriaCoherence(javax.swing.JFrame jfr){
        // DIR Coherence
        boolean bOK = true;
        String strMes = "Warning(s) :";
        PlacementCriterion[] crit = this.getPlaCriteria();
        
        // 1st coherence test : DC or SDC should not appear twice
        int nbDirCrit = 0;
        for (int i = 0; i < crit.length; i++){
            if (crit[i] == PlacementCriterion.DC) nbDirCrit ++;
            if (crit[i] == PlacementCriterion.SDC) nbDirCrit ++;
        }
        if (nbDirCrit > 1){
            strMes += "\nOnly one Direct Confrontation criteria (DC or SDC) should appear";
            bOK = false;
        }
        // 2nd coherence test : Criteria should not mix elements from McMahon group with elements from Swiss group
        int nbSWCriteria = 0;
        int nbMMCriteria = 0;
        for (int i = 0; i < crit.length; i++){
            switch(crit[i]){
                case CAT:
                case NBW:
                case SOSW:
                case SOSWM1:
                case SOSWM2:
                case SODOSW:
                case SOSOSW:
                case CUSSW:
                case EXR:
                case EXT:
                    nbSWCriteria++;
                    break;
                case MMS:
                case SOSM:
                case SOSMM1:
                case SOSMM2:
                case SODOSM:
                case SOSOSM:
                case CUSSM:
                case STS:
                case SOSTS:
                    nbMMCriteria++;
                    break;
            } 
        }
        if (nbSWCriteria > 0 && nbMMCriteria > 0){
            strMes += "\nMcMahon and Swiss Criteria mixed";
            bOK = false;
        }

        // 3rd test : SODOSM is taboo
        boolean bSODOSM = false;
        for (int i = 0; i < crit.length; i++){
            if (crit[i] == PlacementCriterion.SODOSM) bSODOSM= true;
        }
        if (bSODOSM){
            strMes += "\nSODOSM is not recommended";
            bOK = false;
        }
        
        // 4rd test : STS warning
        boolean bSTS = false;
        for (int i = 0; i < crit.length; i++){
            if (crit[i] == PlacementCriterion.STS) bSTS= true;
            if (crit[i] == PlacementCriterion.SOSTS) bSTS= true;
        }
        if (bSTS){
            strMes += "\nSTS and SOSTS scores only make sense in a McMahon tournament"
                    + " with a single elimination bracket for players of the top group (see Help).";
            bOK = false;
        }


        if (bOK) return "";
        else return strMes;
        
    }

    @Deprecated
    public static String criterionShortName(int uid) {
        PlacementCriterion criterion = PlacementCriterion.fromUid(uid);
        if (null != criterion)
            return criterion.getShortName();
        return "";
    }

    @Deprecated
    public static String criterionLongName(int uid) {
        PlacementCriterion criterion = PlacementCriterion.fromUid(uid);
        if (null != criterion)
            return criterion.getLongName();
        return "";
    }

    @Deprecated
    public static String criterionDescription(int uid, GothaLocale locale) {
        PlacementCriterion criterion = PlacementCriterion.fromUid(uid);
        if (null != criterion)
            return criterion.getDescription(locale);
        return "";
    }

    @Deprecated
    public static String[] criteriaLongNames() {
        String[] critLN = new String[PlacementCriterion.values().length];
        for (int i = 0; i < PlacementCriterion.values().length; i++)
            critLN[i] = PlacementCriterion.values()[i].getLongName();
        return critLN;
    }

    @Deprecated
    public static int criterionCoef(int uid) {
        PlacementCriterion criterion = PlacementCriterion.fromUid(uid);
        if (null != criterion)
            return criterion.getCoef();
        return 1;
    }

    @Deprecated
    public static int criterionUID(String longName) {
        PlacementCriterion criterion = PlacementCriterion.fromLongName(longName);
        if (null == criterion)
            criterion = PlacementCriterion.NUL;
        return criterion.getUid();
    }

    public static PlacementCriterion[] purgeUselessCriteria(PlacementCriterion[] tC) {
        int nbC = 0;
        for (int c = 0; c < tC.length; c++) {
            if (tC[c] != PlacementCriterion.NUL) {
                nbC++;
            }
        }
        PlacementCriterion[] tabCrit;
        if (nbC == 0) {
            tabCrit = new PlacementCriterion[1];
            tabCrit[0] = PlacementCriterion.NUL;
        } else {
            tabCrit = new PlacementCriterion[nbC];
            tabCrit[0] = PlacementCriterion.NUL;
            int crit = 0;
            for (int c = 0; c < tC.length; c++) {
                if (tC[c] != PlacementCriterion.NUL) {
                    tabCrit[crit++] = tC[c];
                }
            }
        }
        return tabCrit;
    }
    
    public PlacementCriterion[] getPlaCriteria() {
        return Arrays.copyOf(plaCriteria, plaCriteria.length);
    }

    public void setPlaCriteria(PlacementCriterion[] plaCriteria) {
        this.plaCriteria = Arrays.copyOf(plaCriteria, plaCriteria.length);
    }
    
    public PlacementCriterion mainCriterion() {
        PlacementCriterion mainCrit = PlacementCriterion.NBW;
        PlacementCriterion[] crit = getPlaCriteria();
        for (int iC = 0; iC < crit.length; iC++){
            if (crit[iC] == PlacementCriterion.NBW){
                return PlacementCriterion.NBW;
            }
            if (crit[iC] == PlacementCriterion.MMS){
                return PlacementCriterion.MMS;
            }
        }  
        return mainCrit;
    }
}
