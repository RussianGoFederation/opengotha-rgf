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

package ru.gofederation.gotha.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;

import javax.swing.JFrame;

import ru.gofederation.gotha.util.GothaLocale;

public abstract class FrameBase extends JFrame {
    private final GothaLocale gothaLocale;

    public FrameBase() {
        gothaLocale = GothaLocale.getCurrentLocale();
        createUi(getContentPane());
    }

    protected abstract void createUi(Container contentPane);

    protected String getString(String key) {
        return gothaLocale.getString(key);
    }

    public static Font scaleFont(Component component, float scale) {
        return scaleFont(component.getFont(), scale);
    }

    public static Font scaleFont(Font font, float scale) {
        return font.deriveFont(font.getSize2D() * scale);
    }
}
