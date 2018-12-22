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

package ru.gofederation.gotha.model;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import info.vannier.gotha.Gotha;
import info.vannier.gotha.RatedPlayer;
import info.vannier.gotha.RatingList;
import ru.gofederation.gotha.model.rgf.RgfRatingList;

import static ru.gofederation.gotha.model.RatingOrigin.AGA;
import static ru.gofederation.gotha.model.RatingOrigin.EGF;
import static ru.gofederation.gotha.model.RatingOrigin.FFG;

public final class RatingListFactory {
    private RatingListFactory() {}
    private static RatingListFactory instance;
    public static RatingListFactory instance() {
        if (null == instance) instance = new RatingListFactory();
        return instance;
    }

    public RatingList loadDefaultFile(RatingListType type) throws IOException {
        File file = new File(Gotha.getRunningDirectory(), type.getFilename());
        try (InputStream in = new FileInputStream(file)) {
            return load(type, in);
        }
    }

    RatingList load(RatingListType type, InputStream in) throws IOException {
        RatingList ratingList = new RatingList();
        switch (type) {
            case AGA:
                ratingList.setRatingListType(RatingListType.AGA);
                loadAGA(in, ratingList);
                break;

            case EGF:
                ratingList.setRatingListType(RatingListType.EGF);
                loadEGF(in, ratingList);
                break;

            case FFG:
                ratingList.setRatingListType(RatingListType.FFG);
                loadFFG(in, ratingList);
                break;

            case RGF:
                ratingList.setRatingListType(RatingListType.RGF);
                loadRGF(in, ratingList);
                break;
        }
        return ratingList;
    }

    private List<String> lines(InputStream in) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, java.nio.charset.Charset.forName("ISO-8859-15")))) {
            String line ;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private void loadAGA(InputStream in, RatingList ratingList) throws IOException {
        ArrayList<RatedPlayer> players = new ArrayList<>();
        for (String strLine : lines(in)) {
            if (strLine.length() < 10) continue;
            int AGA_NAFI = 0;
            int AGA_ID = 1;
            int AGA_MTYPE = 2;
            int AGA_RATING = 3;
            int AGA_EXPIRATION = 4;
            int AGA_CLUB = 5;
            String[] myStrArr = strLine.split("\t");

            String name = "XXX";
            String firstName = "xxx";
            String agaID = "";
            String agaExpirationDate = "";
            String club = "";
            String country = "US";
            int rawRating = -2850;

            if(myStrArr.length > AGA_NAFI){
                String strNaFi = myStrArr[AGA_NAFI].trim();
                if (strNaFi.length() == 0) continue;
                String[] nameStrArr = strNaFi.split(",");
                if (nameStrArr.length < 2) nameStrArr = strNaFi.split(" ");
                name = nameStrArr[0].trim();
                if(nameStrArr.length > 1) firstName = nameStrArr[1].trim();
            }

            if(myStrArr.length > AGA_ID){
                agaID = myStrArr[AGA_ID];
            }

            if(myStrArr.length > AGA_EXPIRATION){
                agaExpirationDate = myStrArr[AGA_EXPIRATION];
            }

            if(myStrArr.length > AGA_MTYPE){
                String agaMType = myStrArr[AGA_MTYPE];
                if (agaMType.equals("Forgn")) country = "";
                else country = "US";
            }
            if(myStrArr.length > AGA_RATING){
                String strAgaRating = myStrArr[AGA_RATING];
                try{
                    Float d = (Float.parseFloat(strAgaRating));
                    rawRating = (int) Math.round(d * 100.0);
                }catch(Exception e){}
            }
            if(myStrArr.length > AGA_CLUB){
                club = myStrArr[AGA_CLUB];
            }

            RatedPlayer rP = new RatedPlayer(
                "", "", "", agaID, agaExpirationDate, name, firstName, country, club, rawRating, "", AGA);
            players.add(rP);
        }
        ratingList.setPlayers(players);
    }

    private void loadEGF(InputStream in, RatingList ratingList) throws IOException {
        int pos;
        ArrayList<RatedPlayer> players = new ArrayList<>();
        for (String strLine : lines(in)){
            pos = strLine.indexOf("(");
            if (pos >= 0) {
                String str = strLine.substring(pos);
                String[] dateElements = str.split(" ");
                if ((dateElements.length >= 3)
                    && (dateElements[2].length() >= 2)
                    && (dateElements[2].substring(0,2).compareTo("20") == 0) ) // Just take care of the "2100 year bug" :)
                    ratingList.setStrPublicationDate(str.substring(1, 12));
            }
            if (strLine.length() < 10) continue;
            String strPin = strLine.substring(1, 9);
            if (strPin.matches("[0-9]*")){
                String strNF = strLine.substring(11, 48);
                String strName = strNF;
                String strFirstName = "x";

                pos = strNF.indexOf(" ");

                if (pos > 0) strName = strNF.substring(0, pos).trim();
                if ((pos + 1) < strNF.length()) strFirstName = strNF.substring(pos + 1, strNF.length()).trim();

                String strCountry = strLine.substring(49, 52).trim();
                String strClub = strLine.substring(53, 57).trim();
                int rating = Integer.valueOf(strLine.substring(71, 75).trim());
                String strGrade = strLine.substring(60,63).trim();
                RatedPlayer rP = new RatedPlayer(
                    strPin, "", "", "", "", strName, strFirstName, strCountry, strClub, rating, strGrade, EGF);
                players.add(rP);
            }
        }
        ratingList.setPlayers(players);
    }

    private void loadFFG(InputStream in, RatingList ratingList) throws IOException {
        int pos;
        ArrayList<RatedPlayer> players = new ArrayList<>();
        for (String strLine : lines(in)) {
            pos = strLine.indexOf("Echelle au ");
            if (pos >= 0) {
                ratingList.setStrPublicationDate(strLine.substring(pos + 11, strLine.length()));
            }
            if (strLine.length() < 60) continue;

            String strNF = strLine.substring(0, 38);
            if (strNF.matches("[a-zA-Z].*")) {
                pos = strNF.indexOf(" ");
                String strName = strNF.substring(0, pos).trim();
                String strFirstName = strNF.substring(pos + 1, strNF.length()).trim();
                int rating = Integer.valueOf(strLine.substring(38, 43).trim());
                String strFfgLicenceStatus = strLine.substring(44, 45);
                String strFfgLicence = strLine.substring(46, 53);
                String strClub = strLine.substring(54, 58).trim();
                String strCountry = strLine.substring(59, 61).trim();
                RatedPlayer rP = new RatedPlayer(
                    "", strFfgLicence, strFfgLicenceStatus, "", "", strName, strFirstName, strCountry, strClub, rating, "", FFG);
                players.add(rP);
            }
        }
        ratingList.setPlayers(players);
    }

    private void loadRGF(InputStream in, RatingList ratingList) {
        RgfRatingList rl = new Gson().fromJson(new InputStreamReader(in), RgfRatingList.class);
        Set<RgfRatingList.Player> rps = rl.getPlayers();
        if (null == rps) throw new IllegalStateException();
        ArrayList<RatedPlayer> players = new ArrayList<>();
        for (RgfRatingList.Player rp : rps) {
            RatedPlayer player = new RatedPlayer.Builder()
                .setRgfId(rp.id)
                .setFirstName(rp.firstName)
                .setName(rp.lastName)
                .setRawRating(RatingOrigin.RGF, rp.rawRating)
                .setCountry("RU")
                .build();
            players.add(player);
        }

        players.sort((a, b) -> {
            int c = a.getName().compareTo(b.getName());
            if (c != 0) return c;
            c = a.getFirstName().compareTo(b.getFirstName());
            if (c != 0) return c;
            return 0;
        });

        ratingList.setPlayers(players);
    }
}
