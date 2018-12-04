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

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public final class GothaPreferences {
    private static GothaPreferences instance;

    private static final String PREFERENCES_NODE = "info/vannier/opengotha";
    private static final String LOCALE = "locale";
    public static final String DEFAULT_RATING_LIST = "playersmanager.defaultratinglist";

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

    public String getString(String key, String defaultValue) {
        return preferences.get(key, defaultValue);
    }

    public GothaPreferences putString(String key, String value) {
        preferences.put(key, value);
        return this;
    }

    public void sync() {
        try {
            preferences.sync();
        } catch (BackingStoreException e) {
            e.printStackTrace();
            // TODO log error
        }
    }
}
