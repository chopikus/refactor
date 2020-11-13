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

public class ParseTreeElement implements TreeComponent {
    private String name;
    private boolean isNode;

    private TreeComponent[] nodes = new TreeComponent[0];
    private TreeComponent[] leaves = new TreeComponent[0];

    public ParseTreeElement(int type) {
	this(type, "");
    }

    public ParseTreeElement(int type, String name) {
	this.name = name;
	this.isNode = TreeElement.NODE == type;
    }

    public boolean isNode () {
	return isNode;
    }

    public boolean hasLeaves () {
	return (leaves.length > 0);
    }

    public boolean hasNodes () {
	return (nodes.length > 0);
    }

    public String getName () {
	return name;
    }

    public void addNode(String newnode) {
	TreeComponent[] tmp = new TreeComponent[nodes.length+1];
	for (int i = 0; i<nodes.length;i++) {
	    tmp[i] = nodes[i];
	}
	tmp[nodes.length] = new ParseTreeElement (TreeElement.NODE, newnode);
	nodes = tmp;
    }

    public void addLeaf(String newleaf) {
	TreeComponent[] tmp = new TreeComponent[leaves.length+1];
	for (int i = 0; i<leaves.length;i++) {
	    tmp[i] = leaves[i];
	}
	tmp[leaves.length] = new ParseTreeElement (TreeElement.LEAF, newleaf);
	leaves = tmp;
    }

    public TreeComponent[] getAllNodes () {
	return nodes;
    }

    public TreeComponent[] getAllLeaves () {
	return leaves;
    }

    public String[] getAllNodeNames() {
	String[] out = new String[nodes.length];
	for (int i=0;i<nodes.length;i++) {
	    out[i] = nodes[i].getName();
	}
	return out;
    }

    public String[] getAllLeaveNames() {
	String[] out = new String[leaves.length];
	for (int i=0;i<leaves.length;i++) {
	    out[i] = leaves[i].getName();
	}
	return out;
    }

    public TreeComponent getNode(String name) {
	int i = 0;
	while((i<nodes.length)&&(!nodes[i].getName().equals(name))) {
	    i++;
	}
	if (i==nodes.length) {
            // Fail-Save ?
            System.out.println("Fehler beim Auffinden eines Knotens");
	    return new ParseTreeElement(TreeElement.NODE,"empty");
	} else {
            return nodes[i];
	}
    }

    public TreeComponent getLeaf(String name) {
	int i = 0;
	while((i<leaves.length)&&(!leaves[i].getName().equals(name))) {
	    i++;
	}
	if (i==leaves.length) {
            // Fail-Save ?
            System.out.println("Fehler beim Auffinden eines Blattes");
	    return new ParseTreeElement(TreeElement.LEAF,"empty");
	} else {
            return leaves[i];
	}
    }

}
