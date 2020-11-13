/*
Eteria IRC Client, an RFC 1459 compliant client program written in Java.
Copyright (C) 2000-2001  Javier Kohen <jkohen at tough.com>

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

import java.awt.*;
import java.io.IOException;
import ar.com.jkohen.irc.User;
import ar.com.jkohen.util.Resource;

public class NickItemBullet extends NickItem {
    private static final int OP = 0;
    private static final int HALFOP = 1;
    private static final int VOICE = 2;
    private static final int BULLETS = 3;

    private static int BULLET_DIAMETER;
    private static boolean bullets_loaded;
    private static final int BULLET_GAP = 2;

    private static Image [] bullets;

    public NickItemBullet(User user) {
	super(user);

	loadBullets();
    }

    private void loadBullets() {
	if (bullets_loaded) {
	    return;
	}

	bullets_loaded = true;

	bullets = new Image[BULLETS];

	Toolkit tk = Toolkit.getDefaultToolkit();

	try {
	    bullets[OP] = tk.createImage(Resource.getResource("green.gif",
  							      this));
	    bullets[HALFOP] = tk.createImage(Resource.getResource("red.gif",
								  this));
	    bullets[VOICE] = tk.createImage(Resource.getResource("yellow.gif",
								 this));
	} catch (IOException e) {
	    System.err.println(e);
	}

	for (int i = 0; i < BULLETS; i++) {
	    int bullet_width;

	    while (-1 == (bullet_width = bullets[i].getWidth(this))) {
	    }

	    BULLET_DIAMETER = Math.max(BULLET_DIAMETER, bullet_width);
	}
    }

    public void paint(Graphics g) {
	Dimension size = getSize();
	int x = 0;
	Color fg = getTextForeground();

	if (isSelected()) {
	    // Fill background.
  	    g.setColor(getSelectedBackground());
	    g.fillRect(0, 0, size.width, size.height);
  	    fg = getSelectedForeground();
	}

	Image current_bullet = null;

	if (user.isOp()) {
	    current_bullet = bullets[OP];
	} else if (user.isHalfOp()) {
	    current_bullet = bullets[HALFOP];
	} else if (user.isVoiced()) {
	    current_bullet = bullets[VOICE];
	}

	if (null != current_bullet) {
	    int y = (size.height - BULLET_DIAMETER) / 2;

	    g.drawImage(current_bullet, BULLET_GAP, y, this);

	    x += BULLET_DIAMETER + 2 * BULLET_GAP;
	}

	g.setColor(fg);

	FontMetrics fm = g.getFontMetrics();
	int baseline = fm.getLeading() + fm.getMaxAscent();

	g.drawString(user.getTag(), x, baseline);
    }

    public Dimension getPreferredSize() {
	Graphics g = getGraphics();
	FontMetrics fm = g.getFontMetrics();

	Dimension size = new Dimension(fm.stringWidth(user.getTag()),
				       fm.getLeading() + fm.getMaxAscent()
				       + fm.getMaxDescent());

	if (user.isOp() || user.isHalfOp() || user.isVoiced()) {
	    size.width += BULLET_DIAMETER + 2 * BULLET_GAP;
	}

	return size;
    }
}
