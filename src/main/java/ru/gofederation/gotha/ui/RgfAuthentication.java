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

import net.miginfocom.swing.MigLayout;

import java.awt.Component;
import java.awt.EventQueue;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import ru.gofederation.gotha.util.GothaLocale;

public final class RgfAuthentication {
    private final Charset utf8 = Charset.forName("UTF8");
    private final GothaLocale locale = GothaLocale.getCurrentLocale();
    private static final AtomicReference<String> login = new AtomicReference<>(null);
    private static final AtomicReference<String> password = new AtomicReference<>(null);

    public String getAuthenticationHeader(Component parentComponent) {
        showAuthDialog(parentComponent);

        if (login.get() == null ||
            password.get() == null) {
            return null;
        }

        return "Basic " + new String(Base64.getEncoder().encode((login + ":" + password).getBytes(utf8)), utf8);
    }

    private void showAuthDialog(Component parentComponent) {
        final CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.isEventDispatchThread();

        Runnable r = () -> {
            JTextField loginField = new JTextField();
            loginField.setText(login.get());
            JPasswordField passwordField = new JPasswordField();
            passwordField.setText(password.get());

            JPanel panel = new JPanel(new MigLayout());
            panel.add(new JLabel(locale.getString("rgf.auth.credentials_hint")), "span, wrap unrel");
            panel.add(new JLabel(locale.getString("rgf.auth.login")));
            panel.add(loginField, "w 100lp, sgx input, wrap");
            panel.add(new JLabel(locale.getString("rgf.auth.password")));
            panel.add(passwordField, "sgx input");

            JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
            JDialog dialog = optionPane.createDialog(parentComponent, locale.getString("rgf.auth.window_title"));

            dialog.setVisible(true);

            Object value = optionPane.getValue();
            if (value instanceof Integer &&
                (Integer) value == JOptionPane.OK_OPTION &&
                loginField.getText().length() > 0 &&
                passwordField.getPassword().length > 0) {
                login.set(loginField.getText());
                password.set(new String(passwordField.getPassword()));
            }
            latch.countDown();
        };

        if (SwingUtilities.isEventDispatchThread()) r.run();
        else SwingUtilities.invokeLater(r);

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
