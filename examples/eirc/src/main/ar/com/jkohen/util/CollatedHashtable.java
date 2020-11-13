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

import java.text.Collator;
import java.util.Enumeration;
import java.util.Hashtable;

public class CollatedHashtable extends Hashtable {
    private Collator collator;

    public CollatedHashtable(Collator c) {
	this(c, 10, (float) 0.75);
    }

    public CollatedHashtable(Collator c, int initialCapacity) {
	this(c, initialCapacity, (float) 0.75);
    }

    public CollatedHashtable(Collator c, int initialCapacity,
			     float loadFactor) {
	super(initialCapacity, loadFactor);

	this.collator = c;
    }

    public boolean containsKey(Object key) {
	return super.containsKey(get_canon_key(key));
    }

    public Object get(Object key) {
	return super.get(get_canon_key(key));
    }

    public Object remove(Object key) {
	return super.remove(get_canon_key(key));
    }

    protected Object get_canon_key(Object key) {
	// If the key is stored as it is, avoid further processing.
	if (super.containsKey(key)) {
	    return key;
	}

	for (Enumeration e = keys(); e.hasMoreElements(); ) {
	    Object canon_key = e.nextElement();

	    if (collator.equals((String) key, (String) canon_key)) {
		return canon_key;
	    }
	}

	return key;
    }
}
