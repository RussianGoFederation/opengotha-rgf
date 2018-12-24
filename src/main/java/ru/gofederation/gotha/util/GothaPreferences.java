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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public final class GothaPreferences {
    private static GothaPreferences instance;

    private static final String PREFERENCES_NODE = "info/vannier/opengotha";
    private static final String LOCALE = "locale";
    private static final String WINDOW_STATE = "window_state";
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

    public void persistWindowState(Window window, Dimension defaultDimension) {
        restoreWindowState(window, defaultDimension);

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveWindowState(window);
            }
        });
    }

    private void saveWindowState(Window window) {
        Preferences node = preferences.node(window.getClass().getSimpleName());
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // Save window position & dimensions together with screen resolution
        node.put(WINDOW_STATE, String.valueOf(screenSize.width) + "," + screenSize.height + "," +
            window.getX() + "," + window.getY() + "," +
            window.getWidth() + "," + window.getHeight());
    }

    private void restoreWindowState(Window window, Dimension defaultDimension) {
        Preferences node = preferences.node(window.getClass().getSimpleName());

        Point location = null;

        String savedStateStr = node.get(WINDOW_STATE, null);
        if (null != savedStateStr) {
            String[] savedState = savedStateStr.split(",");
            if (savedState.length == 6) {
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                if (screenSize.width == Integer.parseInt(savedState[0]) &&
                    screenSize.height == Integer.parseInt(savedState[1])) {
                    // Saved window state is only valid for specific screen resolution
                    defaultDimension.setSize(Integer.parseInt(savedState[4]), Integer.parseInt(savedState[5]));
                    location = new Point(Integer.parseInt(savedState[2]), Integer.parseInt(savedState[3]));
                } else {
                    // Screen resolution changed - invalidate saved state
                    node.remove(WINDOW_STATE);
                    sync();
                }
            }
        }
        if (null == location) {
            window.setLocationByPlatform(true);
        } else {
            window.setLocation(location);
        }
        window.setSize(defaultDimension);
        window.validate();
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
