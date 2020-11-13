/*
Eteria IRC Client, an RFC 1459 compliant client program written in Java.
Copyright (C) 2000  Javier Kohen <jkohen at tough.com>

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

package ar.com.jkohen.util;

import java.util.MissingResourceException;
import java.util.Observable;
import java.util.Properties;

public class ConfigurationProperties extends Observable {
    private Properties properties;

    public ConfigurationProperties() {
	this.properties = new Properties();
    }

    public ConfigurationProperties(Properties defaults) {
	this.properties = new Properties(defaults);
    }

    public boolean getBoolean(String key) {
	String val = properties.getProperty(key);

	if (val.equals("0")
	    || val.equals("false")
	    || val.equals("no")
	    || val.equals("off")) {
	    return false;
	} else if (val.equals("1")
		   || val.equals("true")
		   || val.equals("yes")
		   || val.equals("on")) {
	    return true;
	}

	throw new MissingResourceException("value isn't boolean",
					   getClass().getName(), key);
    }

    public int getInt(String key) {
	String val = properties.getProperty(key);

	try {
	    return Integer.parseInt(val);
	} catch (NumberFormatException e) {
	    throw new MissingResourceException("value isn't int",
					       getClass().getName(), key);
	}
    }

    public String getString(String key) {
	return properties.getProperty(key);
    }

    public void setBoolean(String key, boolean val) {
	if (val == getBoolean(key)) {
	    return;
	}

	properties.put(key, String.valueOf(val));
	setChanged();
	notifyObservers(key);
    }

    public void setInt(String key, int val) {
	if (val == getInt(key)) {
	    return;
	}

	properties.put(key, String.valueOf(val));
	setChanged();
	notifyObservers(key);
    }

    public void setString(String key, String val) {
	if (val.equals(getString(key))) {
	    return;
	}

	properties.put(key, val);
	setChanged();
	notifyObservers(key);
    }
}
