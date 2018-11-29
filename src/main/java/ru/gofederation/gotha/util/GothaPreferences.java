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

package ru.gofederation.gotha.util;

import java.util.prefs.Preferences;

public final class GothaPreferences {
    private static GothaPreferences instance;

    private static final String PREFERENCES_NODE = "info/vannier/opengotha";
    private static final String LOCALE = "locale";

    private final Preferences preferences;

    private GothaPreferences() {
        preferences = Preferences.userRoot().node(PREFERENCES_NODE);
    }

    public static GothaPreferences instance() {
        if (null == instance) {
            instance = new GothaPreferences();
        }

        return instance;
    }

    public String getLocale() {
        return this.preferences.get(LOCALE, GothaLocale.values()[0].getCode());
    }

    public void setLocale(String localeCode) {
        this.preferences.put(LOCALE, localeCode);
    }
}
