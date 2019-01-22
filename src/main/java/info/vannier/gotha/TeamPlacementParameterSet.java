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

package info.vannier.gotha;

import java.util.Arrays;

import ru.gofederation.gotha.util.GothaLocale;

/**
 *
 * @author Luc Vannier
 */
public final class TeamPlacementParameterSet implements java.io.Serializable{
    private static final long serialVersionUID = Gotha.GOTHA_DATA_VERSION;

    final static int TPL_MAX_NUMBER_OF_CRITERIA = 6;

    private TeamPlacementCriterion[] plaCriteria;

    public TeamPlacementParameterSet() {
        plaCriteria = new TeamPlacementCriterion[TPL_MAX_NUMBER_OF_CRITERIA];
        Arrays.fill(plaCriteria, TeamPlacementCriterion.NUL);
    }

    public TeamPlacementParameterSet(TeamPlacementParameterSet tpps) {
        TeamPlacementCriterion[] plaCritModel = tpps.getPlaCriteria();
        TeamPlacementCriterion[] plaCrit = new TeamPlacementCriterion[TPL_MAX_NUMBER_OF_CRITERIA];
        System.arraycopy(plaCritModel, 0, plaCrit, 0, TPL_MAX_NUMBER_OF_CRITERIA);
        this.setPlaCriteria(plaCrit);
    }

    public TeamPlacementParameterSet deepCopy(){
        return new TeamPlacementParameterSet(this);
    }

    public boolean equals(TeamPlacementParameterSet tpps){
        for (int ic = 0; ic < TeamPlacementParameterSet.TPL_MAX_NUMBER_OF_CRITERIA; ic++){
            if (this.getPlaCriterion(ic) != tpps.getPlaCriterion(ic)) return false;
        }
        return true;
    }

    public void init(){
        plaCriteria = new TeamPlacementCriterion[TPL_MAX_NUMBER_OF_CRITERIA];
        plaCriteria[0] = TeamPlacementCriterion.TEAMPOINTS;
        plaCriteria[1] = TeamPlacementCriterion.BOARDWINS;
        plaCriteria[2] = TeamPlacementCriterion.BOARDWINS_3UB;
        plaCriteria[3] = TeamPlacementCriterion.BOARDWINS_2UB;
        plaCriteria[4] = TeamPlacementCriterion.BOARDWINS_1UB;
        plaCriteria[5] = TeamPlacementCriterion.MEAN_RATING;
    }

    @Deprecated
    public static String criterionShortName(int uid) {
        TeamPlacementCriterion criterion = TeamPlacementCriterion.fromUid(uid);
        if (null != criterion)
            return criterion.getShortName();
        return "";
    }

    @Deprecated
    public static String criterionLongName(int uid){
        TeamPlacementCriterion criterion = TeamPlacementCriterion.fromUid(uid);
        if (null != criterion)
            return criterion.getLongName();
        return "";
    }

    @Deprecated
    public static String criterionDescription(int uid, GothaLocale locale){
        TeamPlacementCriterion criterion = TeamPlacementCriterion.fromUid(uid);
        if (null != criterion)
            return criterion.getDescription(locale);
        return "";
    }

    @Deprecated
    public static String[] criteriaLongNames() {
        String[] critLN = new String[TeamPlacementCriterion.values().length];
        for (int i = 0; i < TeamPlacementCriterion.values().length; i++)
            critLN[i] = TeamPlacementCriterion.values()[i].getLongName();
        return critLN;
    }

    @Deprecated
    public static int criterionCoef(int uid) {
        TeamPlacementCriterion criterion = TeamPlacementCriterion.fromUid(uid);
        if (null != criterion)
            return criterion.getCoef();
        return 1;

    }

    @Deprecated
    public static int criterionUID(String longName) {
        TeamPlacementCriterion criterion = TeamPlacementCriterion.fromLongName(longName);
        if (null != criterion)
            return criterion.getUid();
        return TeamPlacementCriterion.NUL.getUid();
    }

    public static TeamPlacementCriterion[] purgeUselessCriteria(TeamPlacementCriterion[] tC) {
        int nbC = 0;
        for (int c = 0; c < tC.length; c++) {
            if (tC[c] != TeamPlacementCriterion.NUL) {
                nbC++;
            }
        }
        TeamPlacementCriterion[] tabCrit;
        if (nbC == 0) {
            tabCrit = new TeamPlacementCriterion[1];
            tabCrit[0] = TeamPlacementCriterion.NUL;
        } else {
            tabCrit = new TeamPlacementCriterion[nbC];
            tabCrit[0] = TeamPlacementCriterion.NUL;
            int crit = 0;
            for (int c = 0; c < tC.length; c++) {
                if (tC[c] != TeamPlacementCriterion.NUL) {
                    tabCrit[crit++] = tC[c];
                }
            }
        }
        return tabCrit;
    }

    /**
     * @return the plaCriteria
     */
    public TeamPlacementCriterion[] getPlaCriteria() {
        TeamPlacementCriterion[] c = Arrays.copyOf(plaCriteria, plaCriteria.length);
        for (int i = 0; i < c.length; i++) {
            if (c[i] == null) {
                c[i] = TeamPlacementCriterion.NUL;
            }
        }
        return c;
    }

    /**
     * @return the plaCriterion for crit number
     */
    public TeamPlacementCriterion getPlaCriterion(int iCrit) {
        return plaCriteria[iCrit];
    }

    /**
     * @param plaCriteria the plaCriteria to set
     */
    public final void setPlaCriteria(TeamPlacementCriterion[] plaCriteria) {
        this.plaCriteria = new TeamPlacementCriterion[plaCriteria.length];
        System.arraycopy(plaCriteria, 0, this.plaCriteria, 0, plaCriteria.length);
    }
}
