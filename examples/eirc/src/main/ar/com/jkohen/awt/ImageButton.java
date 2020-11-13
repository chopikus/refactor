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

import java.awt.*;
import java.awt.event.*;

public class ImageButton extends Canvas implements MouseListener {
    public final static int ENTERED = 0;
    public final static int EXITED = 1;
    public final static int PRESSED = 2;
    public final static int DISABLED = 3;
    public final static int STATES = 4;

    private ActionListener actionListener;

    private MediaTracker tracker;
    private Image stateImages[];

    protected String action_command;

    protected int state = EXITED;

    protected boolean mouseOver = false;
    protected boolean mousePressed = false;

    public ImageButton() {
	this.tracker = new MediaTracker(this);
	this.stateImages = new Image[STATES];
	this.action_command = "";

	addMouseListener(this);
    }

    public void setImage(int st, Image i) {
	tracker.addImage(i, 0);
	this.stateImages[st] = i;
    }

    public void paint(Graphics g) {
	if (!isEnabled()) {
	    this.state = DISABLED;
	}
	if (null != stateImages[state]) {
	    g.drawImage(stateImages[state], 0, 0, this);
	}
    }

    public void mouseClicked(MouseEvent e) {
	processActionEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
					   action_command, e.getModifiers()));
    }

    public void mouseEntered(MouseEvent e) {
	this.mouseOver = true;
	if (PRESSED == state) {
	    return;
	}
	this.state = mousePressed ? PRESSED : ENTERED;
	repaint();
    }

    public void mouseExited(MouseEvent e) {
	this.mouseOver = false;
	this.state = EXITED;
	repaint();
    }

    public void mousePressed(MouseEvent e) {
	this.mousePressed = true;
	this.state = PRESSED;
  	repaint();
    }

    public void mouseReleased(MouseEvent e) {
	this.mousePressed = false;
	this.state = mouseOver ? ENTERED : EXITED;
	repaint();
    }

    public Dimension getPreferredSize() {
	int w = -1, h = -1;

	try {
	    tracker.waitForID(0);
	} catch (InterruptedException e) {
	    System.err.println(e);
	    return new Dimension(-1, -1);
	}

	for (int i = 0; i < STATES; i++) {
	    if (null != stateImages[i]) {
		w = stateImages[i].getWidth(this);
		h = stateImages[i].getHeight(this);
		break;
	    }
	}

	return new Dimension(w, h);
    }

    public Dimension getMinimumSize() {
	return getPreferredSize();
    }

    public void setEnabled(boolean active) {
	if (isEnabled() != active) {
	    this.state = EXITED;
	    super.setEnabled(active);
    	    repaint();
	}
    }

    public void addActionListener(ActionListener l) {
	if (null != l) {
	    actionListener = AWTEventMulticaster.add(actionListener, l);
	}
    }

    public void removeActionListener(ActionListener l) {
	if (null != l) {
	    actionListener = AWTEventMulticaster.remove(actionListener, l);
	}
    }

    public String getActionCommand() {
	return action_command;
    }

    public void setActionCommand(String action) {
	this.action_command = action;
    }

    protected void processActionEvent(ActionEvent e) {
	if (actionListener != null) {
	    actionListener.actionPerformed(e);
	}
    }
}
