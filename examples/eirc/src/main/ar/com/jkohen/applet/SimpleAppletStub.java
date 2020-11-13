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

import java.applet.AppletContext;
import java.applet.AppletStub;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

/**
 * This class implements AppletStub interface in a simple way, suitable to allow Applet instances to run as command-line applications.
 *
 * @author <a href="mailto:jkohen@tough.com">Javier Kohen</a>
 */
public class SimpleAppletStub implements AppletStub {
    private Hashtable parameters;

    /**
     * Creates a new <code>SimpleAppletStub</code> instance.
     *
     * @param args a <code>String[]</code> containing command-line arguments in the form: --name0; value0; ... --nameN; valueN.
     */
    public SimpleAppletStub(String [] args) {
	this.parameters = new Hashtable();

	// Parse command-line parameters.
	for (int i = 0; i < args.length - 1; i++) {
	    if (args[i].startsWith("--")) {
		parameters.put(args[i].substring(2), args[i + 1]);
	    }
	}
    }

    public void appletResize(int width, int height) {
    }

    public AppletContext getAppletContext() {
	return new SimpleAppletContext();
    }

    public URL getCodeBase() {
	URL base = null;

	try {
	    base = new URL("file:");
	} catch (MalformedURLException ex) {
	    System.err.println(ex);
	}

	return base;
    }

    public URL getDocumentBase() {
	URL base = null;

	try {
	    base = new URL("file:");
	} catch (MalformedURLException ex) {
	    System.err.println(ex);
	}

	return base;
    }

    public String getParameter(String name) {
  	return (String) parameters.get(name);
    }

    public boolean isActive() {
	return true;
    }
}
