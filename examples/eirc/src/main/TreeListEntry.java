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

import java.util.Vector;

public class TreeListEntry {
    private Vector prefixes;
    private String content;
    private TreeElement node;

    TreeListEntry (Vector v) {
	prefixes = (Vector) v.clone();
    }

    public void setNode (TreeElement te) {
        node = te;
    }

    public TreeElement getNode () {
        return node;
    }

    public void addPrefix (int i) {
	prefixes.addElement(new Integer(i));
    }

    public void setContent (String s) {
	content = s;
    }

    public Vector getPrefixes() {
	return prefixes;
    }

    public String getContent() {
	return content;
    }
}
