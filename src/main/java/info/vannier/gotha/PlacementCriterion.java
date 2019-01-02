package info.vannier.gotha;

import ru.gofederation.gotha.util.GothaLocale;

public enum PlacementCriterion implements java.io.Serializable{
    /** Null criterion */
    NUL    (0,   "NULL",   "NULL",   1),
    /** Category */
    CAT    (1,   "CAT",    "CAT",    -1),
    /** Number of Wins */
    NBW    (100, "NBW",    "NBW",    2),
    /** McMahon score */
    MMS    (200, "MMS",    "MMS",    2),
    /** Strasbourg score */
    STS    (300, "STS",    "STS",    2),
    /** Rank */
    RANK   (11,  "Rank",   "Rank",   1),
    /** Rating */
    RATING (12,  "Rating", "Rating", 1),

    /** Cumulative Sum of Scores (Number of Wins) */
    CUSSW  (150, "CUSS",   "CUSSW",  2),
    /** Cumulative Sum of Scores (Number of Wins) */
    CUSSM  (250, "CUSS",   "CUSSM",  2),

    /** Sum of Opponents NbW */
    SOSW   (110, "SOS",    "SOSW",   2),
    /** Sum of (n-1)Opponents NbW */
    SOSWM1 (111, "SOS-1",  "SOSW-1", 2),
    /** Sum of (n-2)Opponents NbW */
    SOSWM2 (112, "SOS-2",  "SOSW-2", 2),
    /** Sum of Defeated opponents NbW scores */
    SODOSW (120, "SODOS",  "SODOSW", 4),

    /** Sum of Opponents McMahon scores */
    SOSM   (210, "SOS",    "SOSM",   2),
    /** Sum of (n-1)Opponents MMS */
    SOSMM1 (211, "SOS-1",  "SOSM-1", 2),
    /** Sum of (n-2)Opponents MMS */
    SOSMM2 (212, "SOS-2",  "SOSM-2", 2),
    /** Sum of Defeated opponents McMahon scores */
    SODOSM (220, "SODOS",  "SODOSM", 4),

    /** Sum of Opponents Strasbourg scores */
    SOSTS  (310, "SOSTS",  "SOSTS",  2),


    /** Sum of opponents SOS */
    SOSOSW (130, "SOSOS",  "SOSOSW", 2),
    /** Sum of opponents SOS */
    SOSOSM (230, "SOSOS",  "SOSOSM", 2),
    /** Exploits Reussis */
    EXT    (401, "EXT",    "EXT",    2),
    /** Exploits Tentes */
    EXR    (402, "EXR",    "EXR",    2),

    /** Direct Confrontation */
    DC     (502, "DC",     "DC",     1),
    /** Simplified Direct Confrontation */
    SDC    (501, "SDC",    "SDC",    1);

    private static final long serialVersionUID = Gotha.GOTHA_DATA_VERSION;

    private final int uid;
    private final String shortName;
    private final String longName;
    private final String descriptionKey;
    private final int coef;

    PlacementCriterion(int uid, String shortName, String longName, int coef) {
        this.uid = uid;
        this.shortName = shortName;
        this.longName = longName;
        this.descriptionKey = "pps." + name();
        this.coef = coef;
    }

    public static PlacementCriterion fromUid(int uid) {
        for (PlacementCriterion criterion : values()) {
            if (criterion.getUid() == uid)
                return criterion;
        }

        return null;
    }

    public static PlacementCriterion fromLongName(String longName) {
        for (PlacementCriterion criterion : values()) {
            if (criterion.getLongName() == longName)
                return criterion;
        }

        return null;
    }

    public int getUid() {
        return uid;
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    public String getDescriptionKey() {
        return descriptionKey;
    }

    public String getDescription(GothaLocale locale) {
        return locale.getString(getDescriptionKey());
    }

    public int getCoef() {
        return coef;
    }
}
