package info.vannier.gotha;


import java.util.Objects;

import ru.gofederation.gotha.model.RatingOrigin;

import static ru.gofederation.gotha.model.RatingOrigin.AGA;
import static ru.gofederation.gotha.model.RatingOrigin.FFG;
import static ru.gofederation.gotha.model.RatingOrigin.UNDEF;

public final class RatedPlayer {
    private final String egfPin;
    private final String ffgLicence;
    private final String ffgLicenceStatus;
    private final String agaID;
    private final String agaExpirationDate;
    private final int rgfId;
    private final String name;
    private final String firstName;
    private final String country;
    private final String club;
    private final int rawRating;
    private final String strGrade;
    private final RatingOrigin ratingOrigin;


    public RatedPlayer(
            String egfPin,
            String ffgLicence,
            String ffgLicenceStatus,
            String agaID,
            String agaExpirationDate,
            String name,
            String firstName,
            String country,
            String club,
            int rawRating,
            String strGrade,
            RatingOrigin ratingOrigin){
         this.egfPin = egfPin;
         this.ffgLicence = ffgLicence;
         this.ffgLicenceStatus = ffgLicenceStatus;
         this.agaID = agaID;
         this.agaExpirationDate = agaExpirationDate;
         this.rgfId = 0;
         this.name = name;
         this.firstName = firstName;
         this.country = country;
         this.club = club;
         this.rawRating = rawRating;
         this.strGrade = strGrade;
         this.ratingOrigin = ratingOrigin;
    }

    private RatedPlayer(Builder builder) {
        this.egfPin = builder.getEgfPin();
        this.ffgLicence = builder.getFfgLicence();
        this.ffgLicenceStatus = builder.getFfgLicenceStatus();
        this.agaID = builder.getAgaID();
        this.agaExpirationDate = builder.getAgaExpirationDate();
        this.rgfId = builder.getRgfId();
        this.name = builder.getName();
        this.firstName = builder.getFirstName();
        this.country = builder.getCountry();
        this.club = builder.getClub();
        this.rawRating = builder.getRawRating();
        this.strGrade = builder.getStrGrade();
        this.ratingOrigin = builder.getRatingOrigin();
    }

    public String getEgfPin() {
        return egfPin;
    }

    public String getFfgLicence() {
        return ffgLicence;
    }

    public String getAgaId() {
        return agaID;
    }

    public String getAgaExpirationDate() {
        return agaExpirationDate;
    }

    public int getRgfId() {
        return rgfId;
    }

    public String getFfgLicenceStatus() {
        return ffgLicenceStatus;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public String getClub() {
        return club;
    }

    private int getRawRating() {
        return rawRating;
    }

    public String getStrRawRating() {

        int rr = getRawRating();
        String strRR = "" + rawRating;

        if (getRatingOrigin() == AGA){
            // Generate a eeee.ff string
            int e = rr /100;
            int f = Math.abs(rr %100);
            String strF = ".00";
            if (f > 9) strF = "." + f;
            else strF = ".0" + f;

            strRR = "" + e + strF;
        }
        return "" + strRR;
    }

    public int getStdRating() {
        int stdRating = this.rawRating;
        if (ratingOrigin == FFG) stdRating = this.rawRating + 2050;
        if (ratingOrigin == AGA){
            if (this.rawRating >= 100) stdRating =  this.rawRating + 1950;
            if (this.rawRating <= -100) stdRating = this.rawRating + 2150;
            if (this.rawRating > -100 && this.rawRating < 100) stdRating = 2050;
        }

        stdRating = Math.min(stdRating, Player.MAX_RATING);
        stdRating = Math.max(stdRating, Player.MIN_RATING);

        return stdRating;
    }

    public RatingOrigin getRatingOrigin() {
        return ratingOrigin;
    }

    /** returns Levenshtein between s and t
     */
    public static int distance_Levenshtein(String s, String t){
        //*****************************
        // Compute Levenshtein distance
        //*****************************
        int d[][]; // matrix
        int n; // length of s
        int m; // length of t
        int i; // iterates through s
        int j; // iterates through t
        char s_i; // ith character of s
        char t_j; // jth character of t
        int cost; // cost

        // Step 1
        n = s.length();
        m = t.length();
        if (n == 0)
            return m;
        if (m == 0)
            return n;
        d = new int[n+1][m+1];

        // Step 2
        for (i = 0; i <= n; i++)
            d[i][0] = i;
        for (j = 0; j <= m; j++)
            d[0][j] = j;

        // Step 3
        for (i = 1; i <= n; i++) {
            s_i = s.charAt(i - 1);

            // Step 4
            for (j = 1; j <= m; j++) {
                t_j = t.charAt(j - 1);

                // Step 5
                if (s_i == t_j)
                    cost = 0;
                else
                    cost = 1;

                // Step 6
                d[i][j] = Math.min(d[i-1][j]+1, d[i][j-1]+1);
                d[i][j] = Math.min(d[i][j], d[i-1][j-1] + cost);
            }
        }

        // Step 7
        return d[n][m];
    }

    /**
     * @return the strGrade
     */
    public String getStrGrade() {
        return strGrade;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (egfPin == null ? 0 : egfPin.hashCode());
        hash = 31 * hash + (ffgLicence == null ? 0 : ffgLicence.hashCode());
        hash = 31 * hash + (ffgLicenceStatus == null ? 0 : ffgLicenceStatus.hashCode());
        hash = 31 * hash + (agaID == null ? 0 : agaID.hashCode());
        hash = 31 * hash + (agaExpirationDate == null ? 0 : agaExpirationDate.hashCode());
        hash = 31 * hash + rgfId;
        hash = 31 * hash + (name == null ? 0 : name.hashCode());
        hash = 31 * hash + (firstName == null ? 0 : firstName.hashCode());
        hash = 31 * hash + (country == null ? 0 : country.hashCode());
        hash = 31 * hash + (club == null ? 0 : club.hashCode());
        hash = 31 * hash + rawRating;
        hash = 31 * hash + (strGrade == null ? 0 : strGrade.hashCode());
        hash = 31 * hash + (ratingOrigin == null ? 0 : ratingOrigin.hashCode());
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if (null == other) return false;
        if (this == other) return true;
        if (!(other instanceof RatedPlayer)) return false;

        RatedPlayer otherPlayer = (RatedPlayer) other;

        return Objects.equals(this.egfPin, otherPlayer.egfPin)
            && Objects.equals(this.ffgLicence, otherPlayer.ffgLicence)
            && Objects.equals(this.ffgLicenceStatus, otherPlayer.ffgLicenceStatus)
            && Objects.equals(this.agaID, otherPlayer.agaID)
            && Objects.equals(this.agaExpirationDate, otherPlayer.agaExpirationDate)
            && this.rgfId == otherPlayer.rgfId
            && Objects.equals(this.name, otherPlayer.name)
            && Objects.equals(this.firstName, otherPlayer.firstName)
            && Objects.equals(this.country, otherPlayer.country)
            && Objects.equals(this.club, otherPlayer.club)
            && this.rawRating == otherPlayer.rawRating
            && Objects.equals(this.strGrade, otherPlayer.strGrade)
            && Objects.equals(this.ratingOrigin, otherPlayer.ratingOrigin)
        ;
    }

    @Override
    public String toString() {
        String strPlayerString = "";
        String strAGAID = "";
        if (getRatingOrigin() == AGA) strAGAID = ":" + getAgaId();

        strPlayerString = getName() + " " + getFirstName() + strAGAID + " " +
            getCountry() + " " + getClub() + " " + getStrRawRating();

        return strPlayerString;
    }

    public static final class Builder {
        private String egfPin = "";
        private String ffgLicence = "";
        private String ffgLicenceStatus = "";
        private String agaID = "";
        private String agaExpirationDate = "";
        private int rgfId = 0;
        private String name = "";
        private String firstName = "";
        private String country = "";
        private String club = "";
        private int rawRating = 0;
        private String strGrade = "";
        private RatingOrigin ratingOrigin = UNDEF;

        public Builder setEgfPin(String egfPin) {
            this.egfPin = egfPin;
            return this;
        }

        public String getEgfPin() {
            return egfPin;
        }

        public Builder setFfgLicence(String ffgLicence, String ffgLicenceStatus) {
            this.ffgLicence = ffgLicence;
            this.ffgLicenceStatus = ffgLicenceStatus;
            return this;
        }

        public String getFfgLicence() {
            return ffgLicence;
        }

        public String getFfgLicenceStatus() {
            return ffgLicenceStatus;
        }

        public Builder setAgaID(String agaID, String agaExpirationDate) {
            this.agaID = agaID;
            this.agaExpirationDate = agaExpirationDate;
            return this;
        }

        public String getAgaID() {
            return agaID;
        }

        public String getAgaExpirationDate() {
            return agaExpirationDate;
        }

        public Builder setRgfId(int rgfId) {
            this.rgfId = rgfId;
            return this;
        }

        public int getRgfId() {
            return rgfId;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public String getName() {
            return name;
        }

        public Builder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public String getFirstName() {
            return firstName;
        }

        public Builder setCountry(String country) {
            this.country = country;
            return this;
        }

        public String getCountry() {
            return country;
        }

        public Builder setClub(String club) {
            this.club = club;
            return this;
        }

        public String getClub() {
            return club;
        }

        public Builder setRawRating(RatingOrigin ratingOrigin, int rawRating) {
            this.ratingOrigin = ratingOrigin;
            this.rawRating = rawRating;
            return this;
        }

        public int getRawRating() {
            return rawRating;
        }

        public RatingOrigin getRatingOrigin() {
            return ratingOrigin;
        }

        public Builder setStrGrade(String strGrade) {
            this.strGrade = strGrade;
            return this;
        }

        public String getStrGrade() {
            return strGrade;
        }

        public RatedPlayer build() {
            return new RatedPlayer(this);
        }
    }
}
