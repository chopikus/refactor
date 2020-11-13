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

public class Tree {
    private TreeElement root;
    private TreeElement currentNode;

    public Tree() {
	this.currentNode = this.root
	    = new TreeElement (TreeElement.NODE, "root");
    }

    public String[] getAllNodeNames() {
	TreeElement[] tmp = currentNode.getAllNodes();
	String[] out = new String[tmp.length];
	for (int i=0;i<tmp.length;i++) {
	    out[i] = tmp[i].getName();
	}
	return out;
    }

    public String[] getAllLeaveNames() {
	TreeElement[] tmp = currentNode.getAllLeaves();
	String[] out = new String[tmp.length];
	for (int i=0;i<tmp.length;i++) {
	    out[i] = tmp[i].getName();
	}
	return out;
    }

    public boolean setCurrentNode(String name) {
	TreeElement[] tmp = currentNode.getAllNodes();
	int i = 0;
	while((!tmp[i].getName().equals(name))&&(i<tmp.length)) {
	    i++;
	}
	if (i==tmp.length) {
	    return false;
	} else {
	    currentNode = tmp[i];
	    return true;
	}
    }

    public void setRoot() {
	currentNode = root;
    }

    public void addLeaf(String name) {
	currentNode.addLeaf(name);
    }

    public void addLeaves(String[] names) {
	currentNode.addLeaves(names);
    }

    public void addNode(String name) {
	currentNode.addNode(name);
    }

    public void addNodes(String[] names) {
	currentNode.addNodes(names);
    }

    public boolean hasLeaves() {
	return currentNode.hasLeaves();
    }

    public boolean hasNodes() {
	return currentNode.hasNodes();
    }

    public boolean nodeIsExpanded(String name) {
	TreeElement[] tmp = currentNode.getAllNodes();
	int i = 0;
	while((!tmp[i].getName().equals(name))&&(i<tmp.length)) {
	    i++;
	}
	if (i==tmp.length) {
	    return false;
	} else if (tmp[i].isExpanded() == false) {
	    return false;
	} else if (tmp[i].isExpanded() == true) {
	    return true;
	}
	return false;
    }
}
