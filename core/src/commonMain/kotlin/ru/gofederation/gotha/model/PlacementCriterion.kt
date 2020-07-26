package ru.gofederation.gotha.model

import kotlin.jvm.JvmStatic

enum class PlacementCriterion(
    val uid: Int,
    val shortName: String,
    val longName: String,
    val coef: Int
) {
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

    companion object {
        @JvmStatic
        fun fromUid(uid: Int): PlacementCriterion? =
            values().firstOrNull { it.uid == uid }

        @JvmStatic
        fun fromLongName(longName: String): PlacementCriterion? =
            values().firstOrNull { it.longName == longName }
    }
}
