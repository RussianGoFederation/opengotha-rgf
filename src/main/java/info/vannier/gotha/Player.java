/*
 * Player.java
 */
package info.vannier.gotha;

import org.jetbrains.annotations.Nullable;
import ru.gofederation.gotha.model.AgaId;
import ru.gofederation.gotha.model.FfgLicence;
import ru.gofederation.gotha.model.PlayerRegistrationStatus;
import ru.gofederation.gotha.model.Rank;
import ru.gofederation.gotha.model.RankKt;
import ru.gofederation.gotha.model.Rating;
import ru.gofederation.gotha.model.RatingOrigin;
import ru.gofederation.gotha.model.RgfId;

import java.util.Date;

import static ru.gofederation.gotha.model.RatingOrigin.AGA;
import static ru.gofederation.gotha.model.RatingOrigin.FFG;
import static ru.gofederation.gotha.model.RatingOrigin.UNDEF;

public class Player implements java.io.Serializable{
    private static final long serialVersionUID = Gotha.GOTHA_DATA_VERSION;
    private String name;
    private String firstName;
    private String patronymic;
    private Date dateOfBirth;
    /** keyString is computed at creation/modification time */
    private transient String keyString = null;
    private String country;
    private String club;
    private String egfPin;
    private FfgLicence ffgLicence;
    @Nullable
    private AgaId agaId;
    @Nullable
    private RgfId rgfId = null;
    /**
     * Rank between -30 (30K) and +8 (9D)
     */
    private Rank rank = RankKt.asRank(-20);

    private Rating rating = new Rating(UNDEF, UNDEF.getMinRating());

    /**
     * strGrade is relevant when player is registered from EGF rating list
     * "" when not relevant
     */
    private String strGrade = "";

    /**
     * When computing smms, rank is taken as a basis, then framed by McMahon floor and McMahon bar.
     * Then smms is added smsCorrection, which may be 0, 1 or 2, 1 0 for Bar Group, 1 for Super Group and 2 for Supersuper Group
     * smmsCorrection may also be negative;
     */
    private int smmsCorrection = 0;

    private int smmsByHand = -1;

    private boolean[] participating = new boolean[Gotha.MAX_NUMBER_OF_ROUNDS];

    private PlayerRegistrationStatus registeringStatus;

    /** Creates a new instance of Player */
    public Player() {
    }
    public Player(Player p) {
        deepCopy(p);
    }

    @Deprecated
    public Player(String name, String firstName, String country, String club, String egfPin, String ffgLicence, String ffgLicenceStatus,
            String agaId, String agaExpirationDate,
            int rank,  int rating, RatingOrigin ratingOrigin, String strGrade, int smmsCorrection,
            PlayerRegistrationStatus registeringStatus) throws PlayerException{
        if (name.length() < 1) throw new PlayerException("Player's name should have at least 1 character");
        this.name = name;
        if (firstName.length() < 1) throw new PlayerException("Player's first name should have at least 1 character");
        this.firstName = firstName;
        this.computeKeyString();
        if (country.length() == 1 || country.length() > 2) throw new PlayerException("Country name should either have 2 characters\n"
                + "or be absent");
        this.country = country;
        if (club.length() > 4) throw new PlayerException("Club name should have at most 4 character");
        this.club = club;
        this.egfPin = egfPin;
        if (ffgLicence.length() > 0) {
            this.ffgLicence = new FfgLicence(ffgLicence, ffgLicenceStatus);
        } else {
            this.ffgLicence = null;
        }
        if (agaId.length() > 0) {
            this.agaId = new AgaId(agaId, agaExpirationDate);
        } else {
            this.agaId = null;
        }
        // If rank is out of limits, set it according to strGrade, if exists
        if(rank < Gotha.MIN_RANK || rank > Gotha.MAX_RANK) rank = Player.convertKDPToInt(strGrade);
        this.rank = RankKt.asRank(rank);

        this.rating = new Rating(ratingOrigin, rating);

        if (strGrade.equals("")) strGrade = Player.convertIntToKD(rank);
        strGrade = strGrade.toUpperCase();
        this.strGrade = strGrade;

        this.smmsCorrection = smmsCorrection;
        this.registeringStatus = registeringStatus;



        for(int i = 0; i < Gotha.MAX_NUMBER_OF_ROUNDS; i++) {
            participating[i] = true;
        }
    }

    private Player(Builder builder) throws PlayerException {
        if (builder.getName().length() < 1) throw new PlayerException("Player's name should have at least 1 character");
        this.name = builder.getName();
        if (builder.getFirstName().length() < 1) throw new PlayerException("Player's first name should have at least 1 character");
        this.firstName = builder.getFirstName();
        this.computeKeyString();
        this.patronymic = builder.getPatronymic();
        this.dateOfBirth = builder.getDateOfBirth();
        if (builder.getCountry().length() == 1 || builder.getCountry().length() > 2) throw new PlayerException("Country name should either have 2 characters\nor be absent");
        this.country = builder.getCountry();
        if (builder.getClub().length() > 4) throw new PlayerException("Club name should have at most 4 character");
        this.club = builder.getClub();
        this.egfPin = builder.getEgfPin();
        this.ffgLicence = builder.getFfgLicence();
        this.agaId = builder.getAgaId();
        this.rgfId = builder.getRgfId();
        if (builder.getRank().getValue() < Gotha.MIN_RANK || builder.getRank().getValue() > Gotha.MAX_RANK) this.rank = RankKt.asRank(Player.convertKDPToInt(builder.getGrade()));
        else this.rank = builder.getRank();
        this.rating = builder.getRating();
        if (builder.getGrade().equals("")) this.strGrade = this.rank.toString().toUpperCase();
        else this.strGrade = builder.getGrade().toUpperCase();
        this.smmsCorrection = builder.getSmmsCorrection();
        this.smmsByHand = builder.getSmmsByHand();
        this.registeringStatus = builder.getRegistrationStatus();
        for (int i = 0; i < Gotha.MAX_NUMBER_OF_ROUNDS; i++) {
            participating[i] = true;
        }
    }

    /**
     * Copies p into this
     **/
    public void deepCopy(Player p){
        this.name = p.getName();
        this.firstName = p.getFirstName();
        this.patronymic = p.getPatronymic();
        this.dateOfBirth = p.getDateOfBirth();
        this.keyString = p.getKeyString();
        this.country = p.getCountry();
        this.club = p.getClub();
        this.egfPin = p.getEgfPin();
        this.ffgLicence = p.getFfgLicence();
        this.agaId = p.getAgaId();
        this.rgfId = p.getRgfId();
        this.rank = p.getRank();
        this.rating = p.getRating();
        this.strGrade = p.getStrGrade();
        this.smmsCorrection = p.getSmmsCorrection();
        this.smmsByHand = p.getSmmsByHand();
        boolean[] bPart = new boolean[p.getParticipating().length];
        System.arraycopy(p.getParticipating(), 0, bPart, 0, p.getParticipating().length);
        this.participating = bPart;
        this.registeringStatus = p.getRegisteringStatus();
    }

    public String getName() {
        return name;
    }

    public String getFirstName()  {
        return firstName;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String fullName(){
        return name + " " + firstName;
    }

    public String fullUnblankedName(){
        return name.replaceAll(" ", "_") + " " + firstName.replaceAll(" ", "_");
    }
/** Concatenates name and firtName
     * Shortens if necessary
     * @return
     */
    public String shortenedFullName(){
        String strName = getName();
        String strFirstName = getFirstName();
        if (strName.length() > 18) strName = strName.substring(0, 18);

        String strNF = strName + " " + strFirstName;
        if (strNF.length() > 22)  strNF = strNF.substring(0, 22);
        return strNF;
    }

    public String augmentedPlayerName(DPParameterSet dpps){
        String strNF = shortenedFullName();

//        String strRk = Player.convertIntToKD(this.getRank());
        String strGr = this.getStrGrade();
        String strCo = Gotha.leftString(this.getCountry(), 2);
        String strCl = Gotha.leftString(this.getClub(), 4);

//        boolean bRk = dpps.isShowPlayerRank();
        boolean bGr = dpps.isShowPlayerGrade();
        boolean bCo = dpps.isShowPlayerCountry();
        boolean bCl = dpps.isShowPlayerClub();

        if (!bGr && !bCo && !bCl) return strNF;
        String strPl = strNF + "(";
        boolean bFirst = true;
        if (bGr){
            strPl += strGr;
            bFirst = false;
        }
        if (bCo){
            if (!bFirst) strPl += ",";
            strPl += strCo;
            bFirst = false;
        }
        if (bCl){
            if (!bFirst) strPl += ",";
            strPl += strCl;
        }
        strPl += ")";

        return strPl;
    }

    public Rank getRank() {
        return rank;
    }

    /**
     * @deprecated use {@link #setRank(Rank) directly}
     */
    @Deprecated
    public void setRank(int rank) {
        if (rank > Gotha.MAX_RANK) return;
        if (rank < Gotha.MIN_RANK) return;
        this.rank = RankKt.asRank(rank);
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    private void computeKeyString(){
        this.keyString = (name + firstName).replaceAll(" ", "").toUpperCase();
    }

    public static String computeKeyString(String strNaFi){
        return strNaFi.replaceAll(" ", "").toUpperCase();
    }

    /**
     * Returns a key String for the player
     * fast and convenient for hash tables
     */
    public String getKeyString(){
        if (this.keyString == null) computeKeyString();
        return this.keyString;
   }

    /**
     * 2 players never have the same key string.
     * hasSameKeyString is, thus a way to test if 2 references refer to the same player
     **/
    public boolean hasSameKeyString(Player p){
        if (p == null) return false;
        if (getKeyString().compareTo(p.getKeyString()) == 0) return true;
        else return false;
    }

    public Rating getRating() {
        return rating;
    }

    public void setRating(Rating rating) {
        this.rating = rating;
    }

    public RatingOrigin getRatingOrigin() {
        return rating.getOrigin();
    }

    public String getStrRawRating() {
        int r = getRating().getValue();
        String strRR = "" + r;
        if (getRatingOrigin() == FFG){
            strRR = "" + (r - 2050);
        }
        if (getRatingOrigin() == AGA){
            r = r -2050;
            if (r >= 0) r = r + 100;
            if (r < 0) r = r - 100;

            // Generate a eeee.ff string
            int e = r /100;
            int f = Math.abs(r %100);
            String strF = ".00";
            if (f > 9) strF = "." + f;
            else strF = ".0" + f;

            strRR = "" + e + strF;
        }
        return "" + strRR;
    }

    public String getClub()  {
        return club;
    }

    public void setClub(String val)  {
        this.club = val;
    }

    public boolean[] getParticipating() {
        return participating.clone();
    }

    public void setParticipating(boolean[] val) {
        this.participating = val.clone();
    }

    public boolean getParticipating(int roundNumber) {
        return participating[roundNumber];
    }

    public void setParticipating(int roundNumber, boolean val) {
        this.participating[roundNumber] = val;
    }

    public PlayerRegistrationStatus getRegisteringStatus() {
        return registeringStatus;
    }

    public void setRegisteringStatus(PlayerRegistrationStatus val) {
        this.registeringStatus = val;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String val) {
        this.country = val;
    }

   public int category(GeneralParameterSet gps){
       if (gps.getNumberOfCategories() <= 1) return 0;
       int[] cat = gps.getLowerCategoryLimits();
        for (int c = 0; c < cat.length; c++){
            if (rank.getValue() >= cat[c]) return c;
        }
        return cat.length;
    }

    public int getSmmsByHand() {
        return smmsByHand;
    }

    public boolean isSmmsByHand() {
        return (this.smmsByHand >= 0);
    }

    public void setSmmsByHand(int smmsByHand) {
        this.smmsByHand = smmsByHand;
    }

    public int smms(GeneralParameterSet gps){
        if (smmsByHand >= 0) return smmsByHand;

//        int smms = getRank() + 30;
//        int floor = gps.getGenMMFloor();
//        int bar = gps.getGenMMBar();
//
//        if (smms < floor + 30) smms = floor + 30;
//        if (smms > bar + 30) smms = bar + 30;

        int zero = gps.getGenMMZero();
        int smms = getRank().getValue() - zero;
        int floor = gps.getGenMMFloor();
        int bar = gps.getGenMMBar();

        if (smms < floor - zero) smms = floor - zero;
        if (smms > bar - zero) smms = bar - zero;

        smms += smmsCorrection;

        return smms;
    }

    /**
     * Converts a String rank into an int rank
     * @deprecated User {@link RankKt#asRank(String)}  directly.
     */
    @Deprecated
    public static int convertKDPToInt(String strKDP) {
        return RankKt.asRank(strKDP).getValue();
    }

    /**
     * Converts an int rank into a String rank
     * @deprecated Use {@link RankKt#asRank(int)} directly
     */
    @Deprecated
    public static String convertIntToKD(int rank) {
        return RankKt.asRank(rank).toString();
    }

    /**
     * Converts rating to rank
     * rank = (rating + 1000)/100 - 30;
     * @deprecated Use {@link Rating#toRank} directly
     */
    @Deprecated
    public static int rankFromRating(RatingOrigin origin, int rating) {
        return new Rating(origin, rating).getValue();
    }

    /**
     * Converts rank to rating
     * rating = (rank + 30) *100 - 1000;
     * @deprecated Use {@link Rank#toRating(RatingOrigin)} directly
     */
    @Deprecated
    public static int ratingFromRank(RatingOrigin origin, int rank) {
        return origin.rankToRating(RankKt.asRank(rank)).getValue();
    }

    /**
     * Generates a numberOfRounds characters String with '+' for participating
     * and '-' for not participating
     */
    public static String convertParticipationToString(Player p, int numberOfRounds){
        boolean[] part = p.getParticipating();
        StringBuilder buf = new StringBuilder();
        for (int r = 0; r < numberOfRounds; r++){
            if (part[r]) buf.append("+");
            else buf.append("-");
        }
        return buf.toString();
    }

    /**
     * builds an Id String : EGF if exists, else FFG if exists, else AGA if exists, else ""
     * @return
     */
    public String getAnIdString(){
        String egfP = this.getEgfPin();
        String ffgL;
        if (null != this.getFfgLicence()) {
            ffgL = this.getFfgLicence().getLicence();
        } else {
            ffgL = null;
        }
        String agaI  = this.getAgaId() != null ? this.getAgaId().getId() : null;
        if (egfP != null && egfP.length() > 0) return "EGF Pin : " + egfP;
        else if (ffgL != null && ffgL.length() > 0) return "FFG Licence : " + ffgL;
        else if (agaI != null && agaI.length() > 0) return "AGA Id : " + agaI;
        return "";
    }

    public FfgLicence getFfgLicence() {
        return ffgLicence;
    }

    public String getEgfPin() {
        return egfPin;
    }

    public void setEgfPin(String egfPin) {
        this.egfPin = egfPin;
    }

    @Nullable
    public AgaId getAgaId() {
        return agaId;
    }

    public void setAgaId(AgaId agaId) {
        this.agaId = agaId;
    }

    public void setRgfId(RgfId rgfId) {
        this.rgfId = rgfId;
    }

    public RgfId getRgfId() {
        return rgfId;
    }

    public int getSmmsCorrection() {
        return smmsCorrection;
    }

    public void setSmmsCorrection(int smmsCorrection) {
        this.smmsCorrection = smmsCorrection;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
        this.keyString = null;
    }

    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
        this.keyString = null;

    }

    /**
     * @return the strGrade
     */
    public String getStrGrade() {
        return strGrade;
    }

    /**
     * @param strGrade the strGrade to set
     */
    public void setStrGrade(String strGrade) {
        this.strGrade = strGrade;
    }

    public static final class Builder {
        private String name = "";
        private String firstName = "";
        private String patronymic = "";
        private Date dateOfBirth = null;
        private String country = "";
        private String club = "";
        private String egfPin = "";
        @Nullable
        private FfgLicence ffgLicence = null;
        private String ffgLicenceStatus = "";
        @Nullable
        private AgaId agaId = null;
        private String agaExpirationDate = "";
        @Nullable
        private RgfId rgfId = null;
        private Rank rank = RankKt.asRank(-20);
        private Rating rating = UNDEF.rating(UNDEF.getMinRating());
        private String grade = "";
        private int smmsCorrection = 0;
        private int smmsByHand = -1;
        private PlayerRegistrationStatus registrationStatus = PlayerRegistrationStatus.PRELIMINARY;

        public String getName() {
            return name;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public String getFirstName() {
            return firstName;
        }

        public Builder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public String getPatronymic() {
            return patronymic;
        }

        public Builder setPatronymic(String patronymic) {
            this.patronymic = patronymic;
            return this;
        }

        public Date getDateOfBirth() {
            return dateOfBirth;
        }

        public Builder setDateOfBirth(Date dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public String getCountry() {
            return country;
        }

        public Builder setCountry(String country) {
            this.country = country;
            return this;
        }

        public String getClub() {
            return club;
        }

        public Builder setClub(String club) {
            this.club = club;
            return this;
        }

        public String getEgfPin() {
            return egfPin;
        }

        public Builder setEgfPin(String egfPin) {
            this.egfPin = egfPin;
            return this;
        }

        public FfgLicence getFfgLicence() {
            return ffgLicence;
        }

        public Builder setFfgLicence(FfgLicence ffgLicence) {
            this.ffgLicence = ffgLicence;
            return this;
        }

        @Nullable
        public AgaId getAgaId() {
            return agaId;
        }

        public Builder setAgaId(AgaId getAgaId) {
            this.agaId = agaId;
            return this;
        }

        @Nullable
        public RgfId getRgfId() {
            return rgfId;
        }

        public Builder setRgfId(RgfId rgfId) {
            this.rgfId = rgfId;
            return this;
        }

        public Rank getRank() {
            return rank;
        }

        public Builder setRank(int rank) {
            return setRank(RankKt.asRank(rank));
        }

        public Builder setRank(Rank rank) {
            this.rank = rank;
            return this;
        }

        public Rating getRating() {
            return rating;
        }

        public RatingOrigin getRatingOrigin() {
            return rating.getOrigin();
        }

        public Builder setRating(Rating rating) {
            this.rating = rating;
            return this;
        }

        public Builder setRating(int rating, RatingOrigin ratingOrigin) {
            this.rating = new Rating(ratingOrigin, rating);
            return this;
        }

        public String getGrade() {
            return grade;
        }

        public Builder setGrade(String grade) {
            this.grade = grade;
            return this;
        }

        public int getSmmsCorrection() {
            return smmsCorrection;
        }

        public Builder setSmmsCorrection(int smmsCorrection) {
            this.smmsCorrection = smmsCorrection;
            return this;
        }

        public int getSmmsByHand() {
            return smmsByHand;
        }

        public Builder setSmmsByHand(int smmsByHand) {
            this.smmsByHand = smmsByHand;
            return this;
        }

        public PlayerRegistrationStatus getRegistrationStatus() {
            return registrationStatus;
        }

        public Builder setRegistrationStatus(PlayerRegistrationStatus registrationStatus) {
            this.registrationStatus = registrationStatus;
            return this;
        }

        public Player build() throws PlayerException {
            return new Player(this);
        }
    }
}
