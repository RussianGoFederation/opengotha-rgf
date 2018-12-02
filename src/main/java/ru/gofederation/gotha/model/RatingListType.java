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

public enum RatingListType {
    UND(0,   "",                                                                        "",                           ""),
    EGF(1,   "http://www.europeangodatabase.eu/EGD/EGD_2_0/downloads/allworld_lp.html", "ratinglists/egf_db.txt",     "rating_list.egf"),
    FFG(257, "http://ffg.jeudego.org/echelle/echtxt/ech_ffg_V3.txt",                    "ratinglists/ech_ffg_V3.txt", "rating_list.ffg"),
    AGA(513, "https://usgo.org/mm/tdlista.txt",                                         "ratinglists/tdlista.txt",    "rating_list.aga");

    private final int id;
    private final String filename;
    private final String url;
    private final String l10nKey;

    RatingListType(int id, String url, String filename, String l10nKey) {
        this.id = id;
        this.filename = filename;
        this.url = url;
        this.l10nKey = l10nKey;
    }

    public static RatingListType fromId(int id) {
        for (RatingListType rlType : values()) {
            if (id == rlType.id) return rlType;
        }

        return UND;
    }

    public String getFilename() {
        return filename;
    }

    public String getUrl() {
        return url;
    }

    public String getL10nKey() {
        return l10nKey;
    }
}
