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

import com.ibm.icu.text.MessageFormat;
import ru.gofederation.gotha.model.PlacementCriterion;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;
import java.text.DateFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public enum GothaLocale implements ComboBoxModel<GothaLocale>, I18N {
    EN(Locale.forLanguageTag("en")), // Default locale comes first. Should always be EN.
    RU(Locale.forLanguageTag("ru"));

    private static GothaLocale currentLocale = null;
    private final Locale locale;
    private final DateFormat dateFormat;
    private ResourceBundle resources;

    GothaLocale(Locale locale) {
        this.locale = locale;
        this.dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
    }

    public static GothaLocale defaultLocale() {
        return EN;
    }

    public static GothaLocale fromTag(String tag) {
        for(GothaLocale locale : GothaLocale.values()) {
            if (locale.getCode().equals(tag)) {
                return locale;
            }
        }

        return defaultLocale();
    }

    public String getCode() {
        return locale.toLanguageTag();
    }

    public Locale getLocale() {
        return this.locale;
    }

    @Override
    public DateFormat getDateFormat() {
        return dateFormat;
    }

    private ResourceBundle getResources() {
        if (null == this.resources)
            this.resources = ResourceBundle.getBundle("l10n/strings", this.locale);

        return this.resources;
    }

    /**
     * @deprecated Use {@link #tr(String)} instead
     */
    @Deprecated
    public String getString(String key) {
        return tr(key);
    }

    public MessageFormat getFormat(String key) {
        return new MessageFormat(this.getString(key), this.locale);
    }

    /**
     * @deprecated Use {@link #tr(String, Object...)} instead
     */
    @Deprecated
    public String format(String key, Object... args) {
        return tr(key, args);
    }

    @Override
    public String tr(String key) {
        ResourceBundle bundle = getResources();
        if (bundle.containsKey(key)) {
            return bundle.getString(key);
        } else if (this != GothaLocale.defaultLocale()) {
            return GothaLocale.defaultLocale().getString(key);
        }

        return key;
    }

    @Override
    public String tr(String key, Object... args) {
        return getFormat(key).format(args);
    }

    public String getDescription(PlacementCriterion crit) {
        return tr("pps." + crit.name());
    }

    @Override
    public String toString() {
        return locale.getDisplayLanguage().toLowerCase(locale);
    }

    public static GothaLocale getCurrentLocale() {
        if (null == currentLocale) {
            String tag = GothaPreferences.instance().getLocale();
            currentLocale = GothaLocale.fromTag(tag);
        }

        return currentLocale;
    }

    @Override
    public void setSelectedItem(Object o) {
        if (o instanceof GothaLocale) {
            currentLocale = (GothaLocale)o;
            GothaPreferences.instance().setLocale(currentLocale.getCode());
        }
    }

    @Override
    public Object getSelectedItem() {
        return GothaLocale.getCurrentLocale();
    }

    @Override
    public int getSize() {
        return GothaLocale.values().length;
    }

    @Override
    public GothaLocale getElementAt(int i) {
        return GothaLocale.values()[i];
    }

    @Override
    public void addListDataListener(ListDataListener ll) {
    }

    @Override
    public void removeListDataListener(ListDataListener ll) {
    }
}
