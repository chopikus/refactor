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

public class SimpleParser {
    private String s;

    public SimpleParser() {
	this("<pattern>.*</pattern>");
    }

    public SimpleParser(String s) {
        this.s = s;
    }

    public String parse (ParseTreeElement node) {
	return parse(node, s);
    }

    public String parse (ParseTreeElement node, String s) {
        while (0 != s.length()) {
            String t = s.substring(s.indexOf('<') + 1, s.indexOf('>'));

            if (t.startsWith("category")) {
                String temp = t.substring(t.indexOf('"') + 1,
					  t.lastIndexOf('"'));
                node.addNode(temp);
                s = parse((ParseTreeElement) node.getNode(temp),
			  s.substring(s.indexOf('>') + 1));
            } else if (t.startsWith("pattern")) {
		String temp = s.substring(s.indexOf('>') + 1);
		temp = temp.substring(0, temp.indexOf("</pattern>"));
		node.addLeaf(temp);
		s = s.substring(s.indexOf("</pattern>") + 10);
	    } else if (t.startsWith("/category")) {
		if (s.indexOf('>') + 1 < s.length()) {
		    return s.substring(s.indexOf('>') + 1);
		} else {
		    return "";
		}
	    } else {
		return "";
	    }
        }

        return "";
    }
}
