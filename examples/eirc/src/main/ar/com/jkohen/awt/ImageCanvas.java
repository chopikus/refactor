/*
Javier Kohen's Java Framework Classes.
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

package ar.com.jkohen.awt;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

public class ImageCanvas extends Canvas {
    private Image img;

    public ImageCanvas(Image img) {
	this.img = img;
    }

    public void paint(Graphics g) {
	g.drawImage(img, 0, 0, this);
    }

    public Dimension getMinimumSize() {
	return getPreferredSize();
    }

    public Dimension getPreferredSize() {
	int w, h;

	while (-1 == (w = img.getWidth(this))) {
	    ;
	}
	while (-1 == (h = img.getHeight(this))) {
	    ;
	}

	return new Dimension(w, h);
    }
}
