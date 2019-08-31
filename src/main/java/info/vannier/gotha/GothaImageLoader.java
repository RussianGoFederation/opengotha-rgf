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

package info.vannier.gotha;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class GothaImageLoader extends Thread{
    String strURL;
    JLabel lbl;
    public GothaImageLoader(String strURL, JLabel lbl){
        this.strURL = strURL;
        this.lbl = lbl;
    }
    @Override
    public void run(){
        URL url = null;
        try {
            url = new URL(strURL);
        } catch (MalformedURLException ex) {
            Logger.getLogger(JFrPlayersManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        ImageIcon myIcon = new ImageIcon(url);
        lbl.setIcon(myIcon);
    }

    public static void loadImage(String strURL, JLabel lbl){
        (new GothaImageLoader(strURL, lbl)).start();
    }
}
