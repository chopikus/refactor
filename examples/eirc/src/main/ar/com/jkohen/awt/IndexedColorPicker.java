/*
Javier Kohen's Java Framework Classes.
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
import java.awt.event.*;
import java.util.Vector;

public class IndexedColorPicker extends Canvas
    implements ColorPicker, MouseListener {

    private ActionListener actionListener;

    private Vector items;
    private int last_selected;
    private String action_command;

    private Dimension item_size;
    private Dimension disposition;
    private Dimension gap;

    private Image image;
    private Graphics img_gfx;

    public IndexedColorPicker() {
	this(null, 0);
    }

    public IndexedColorPicker(Color [] colors, int size) {
	this.disposition = new Dimension();
	this.item_size = new Dimension(16, 16);
	this.gap = new Dimension();
	this.action_command = "";

	this.items = new Vector(size);
	setColors(colors, size);

	addMouseListener(this);
    }

    public Color [] getColors() {
	Color [] colors = new Color [items.size()];
	items.copyInto(colors);

	return colors;
    }

    public void setColors(Color [] colors, int size) {
	items.removeAllElements();

	for (int i = 0; i < size; i++) {
	    items.addElement(colors[i]);
	}
    }

    public Dimension getItemSize() {
	return item_size;
    }

    public void setItemSize(int width, int height) {
	item_size.setSize(width, height);
    }

    public Dimension getDisposition() {
	return disposition;
    }

    protected Dimension getRealDisposition() {
	Dimension r = new Dimension(disposition.width, disposition.height);

	if (0 == r.width && 0 == r.height) {
	    r.width = r.height = (int) Math.sqrt(items.size() - 1) + 1;
	} else if (0 == r.width) {
	    r.width = (items.size() - 1) / r.height + 1;
	} else if (0 == r.height) {
	    r.height = (items.size() - 1) / r.width + 1;
	}

	return r;
    }

    public void setDisposition(int width, int height) {
	disposition.setSize(width, height);
    }

    public Dimension getGap() {
	return gap;
    }

    public void setGap(int width, int height) {
	gap.setSize(width, height);
    }

    /**
     * If <code>gap</code> is big enough, user will notice this method is not exact.
     */
    private int getItemAt(Point p) {
	Dimension real = getRealDisposition();

	int i = p.x / (item_size.width + gap.width);
	int j = p.y / (item_size.height + gap.height);

	return (i * real.height + j);
    }

    public int getSelectedIndex() {
	return last_selected;
    }

    public void setSelectedIndex(int i) {
	this.last_selected = i;
    }

    public Color getSelected() {
	return (Color) items.elementAt(last_selected);
    }

    public void setSelected(Color c) {
	this.last_selected = items.indexOf(c);
    }

    public Dimension getMinimumSize() {
	return getPreferredSize();
    }

    public Dimension getPreferredSize() {
	Dimension real = getRealDisposition();

	return new Dimension(item_size.width * real.width
			     + gap.width * (real.width - 1),
			     item_size.height * real.height
			     + gap.height * (real.height - 1));
    }

    private void createImageBuffer(Dimension size) {
	image = createImage(size.width, size.height);
	if (null != img_gfx) {
	    img_gfx.dispose();
	}
	img_gfx = image.getGraphics();
    }

    public void update(Graphics g) {
	paint(g);
    }

    public void paint(Graphics g) {
	Dimension size = getSize();

	if (null == img_gfx
	    || size.width != image.getWidth(this)
	    || size.height != image.getHeight(this)) {
	    createImageBuffer(size);
	}

	img_gfx.setColor(getBackground());
	img_gfx.fillRect(0, 0, size.width, size.height);
	img_gfx.setColor(getForeground());

	Dimension real_disposition = getRealDisposition();

	int x = 0, y;

	for (int i = 0; i < real_disposition.width; i++) {
	    y = 0;

	    for (int j = 0; j < real_disposition.height; j++) {
		int index = i * real_disposition.height + j;
		if (index >= items.size()) {
		    break;
		}

		img_gfx.setColor((Color) items.elementAt(index));
		img_gfx.fillRect(x, y, item_size.width, item_size.height);

		y += gap.height + item_size.height;
	    }

	    x += gap.width + item_size.width;
	}

	g.drawImage(image, 0, 0, this);
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

    public void mouseClicked(MouseEvent ev) {
    }

    public void mousePressed(MouseEvent ev) {
	this.last_selected = getItemAt(ev.getPoint());

	processActionEvent(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
					   action_command, ev.getModifiers()));

	// Avoid taking mouse focus.
	ev.consume();
    }

    public void mouseReleased(MouseEvent ev) {
    }

    public void mouseEntered(MouseEvent ev) {
    }

    public void mouseExited(MouseEvent ev) {
    }
}
