/*
Javier Kohen's Java Framework Classes.
Copyright (C) 2001  Javier Kohen <jkohen at tough.com>

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package ar.com.jkohen.applet;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AudioClip;
import java.awt.Image;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * This class implements AppletContext interface in a simple way, suitable to allow Applet instances to run as command-line applications.
 *
 * @author <a href="mailto:jkohen@tough.com">Javier Kohen</a>
 */
public class SimpleAppletContext implements AppletContext {
    public Applet getApplet(String name) {
	return null;
    }

    public Enumeration getApplets() {
	return null;
    }

    public AudioClip getAudioClip(URL url) {
	return null;
    }

    public Image getImage(URL url) {
	return null;
    }

    public void showDocument(URL url) {
    }

    public void showDocument(URL url, String target) {
    }

    public void showStatus(String status) {
    }

    public InputStream getStream(String key) {
	return null;
    }

    public void setStream(String key, InputStream stream) {
    }

    /* If you're using an old Java compiler (previous to Java 2 API) you'll
     * have to remove the following method in order to be able to compile
     * this class. */
    public Iterator getStreamKeys() {
	return null;
    }
}
