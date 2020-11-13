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
import java.awt.event.*;
import java.text.Collator;
import java.util.Vector;
import ar.com.jkohen.irc.RFC1459;
import ar.com.jkohen.irc.User;

public class NickList extends Container
    implements MouseListener, ItemSelectable {

    public static final int SORT_ALPHA = 0;
    public static final int SORT_ALPHA_FAVOR_MODE = 1;
    public static final int SORT_METHODS = 2;

    public int PREFERRED_WIDTH;

    private ActionListener actionListener;
    private ItemListener itemListener;

    private Color textbg = SystemColor.text;
    private Color textfg = SystemColor.textText;
    private Color selbg = SystemColor.textHighlight;
    private Color selfg = SystemColor.textHighlightText;

    private Image image;
    private Graphics img_gfx;

    private Dimension preferred_size;

    private Collator collator;

    private int sort_method;
    private int [] item_index;

    // SORT_*_FAVOR_MODE masks. These are ordered by priority.
    private static final int [] SORT_MASKS = {
	User.OP_MASK, User.HALFOP_MASK, User.VOICE_MASK, User.NORMAL_MASK
    };

    public NickList() {
	this.preferred_size = new Dimension(1, 1);
	this.PREFERRED_WIDTH = 100;
	this.sort_method = SORT_ALPHA;
	this.collator = RFC1459.getCollator();

  	addMouseListener(this);
    }

    protected void addImpl(Component comp, Object constraints, int index) {
	if (!(comp instanceof NickItem)) {
  	    throw new IllegalArgumentException("not a NickItem");
	}

	NickItem ni = (NickItem) comp;
	ni.setForeground(getForeground());
	ni.setBackground(getBackground());
	ni.setTextForeground(textfg);
	ni.setTextBackground(textbg);
	ni.setSelectedForeground(selfg);
	ni.setSelectedBackground(selbg);

	super.addImpl(comp, constraints, index);
    }

    public void doLayout() {
	Component [] comps = getComponents();

	/* Sort components. */
	sortItems();

	/* Set components' bounds. */
	int y = 0;

	for (int i = 0; i < comps.length; i++) {
	    Component comp = comps[item_index[i]];
	    int height = comp.getPreferredSize().height;

	    comp.setBounds(0, y, PREFERRED_WIDTH, height);

	    y += height;
	}

	preferred_size.setSize(PREFERRED_WIDTH, y);
    }

    private void createImageBuffer(Dimension size) {
	this.image = createImage(size.width, size.height);
	if (null != img_gfx) {
	    img_gfx.dispose();
	}

	this.img_gfx = image.getGraphics();
    }

    public void update(Graphics g) {
	paint(g);
    }

    public void paint(Graphics g) {
	Dimension size = getSize();

	if (null == img_gfx
	    || size.height != image.getHeight(this)
	    || size.width != image.getWidth(this)) {
	    createImageBuffer(size);
	}

	img_gfx.setColor(getBackground());
	img_gfx.fillRect(0, 0, size.width, size.height);
	img_gfx.setColor(getForeground());

	super.paint(img_gfx);

	g.drawImage(image, 0, 0, this);
    }

    public int getSortMethod() {
	return sort_method;
    }

    public void setSortMethod(int method) {
	if (method >= SORT_METHODS) {
  	    throw new IllegalArgumentException("invalid sorting method");
	}

	this.sort_method = method;
	doLayout();
    }

    /**
     * This method invokes whatever sorting method has been selected with setSortMethod, and prepares the <code>item_index</code> array, where the elements are indexed into.
     *
     * @see setSortMethod
     */
    private void sortItems() {
	Component [] components = getComponents();
	int item_count = components.length;

	if (null == item_index || item_index.length != item_count) {
	    this.item_index = new int [item_count];
	    for (int i = 0; i < item_count; i++) {
		item_index[i] = i;
	    }
	}

	User [] users = new User [item_count];
	for (int i = 0; i < item_count; i++) {
	    users[i] = ((NickItem) components[i]).getUser();
	}

	switch (sort_method) {
	case SORT_ALPHA:
  	    sortItemsAlphabetically(users);
	    break;
	case SORT_ALPHA_FAVOR_MODE:
  	    sortItemsAlphabeticallyFavorMode(users);
	    break;
	}
    }

    private void sortItemsAlphabetically(User [] users) {
	int user_count = users.length;

	for (int i = 0; i < user_count; i++) {
	    for (int j = user_count - 1; j > i; j--) {
		User a = (User) users[item_index[i]];
		User b = (User) users[item_index[j]];

		if (collator.compare(a.getTag(), b.getTag()) > 0) {
		    int t = item_index[i];
		    item_index[i] = item_index[j];
		    item_index[j] = t;
		}
	    }
	}
    }

    /**
     * Note that this algorithm breaks if the MSB of MODES mask is used for sorting, as it uses sign to match the flags.
     */
    private void sortItemsAlphabeticallyFavorMode(User [] users) {
	int user_count = users.length;

	// Build an array with the mode masks for each nick.
	int [] modes = new int [user_count];
	for (int i = 0; i < user_count; i++) {
	    modes[i] = users[i].getModes();
	}

	// Bubble-sort the items, group by User modes.
	for (int i = 0; i < user_count; i++) {
	    for (int j = user_count - 1; j > i; j--) {
		int comp_status = 0;
		int mode_a = modes[item_index[i]];
		int mode_b = modes[item_index[j]];

		for (int m = 0; m < SORT_MASKS.length; m++) {
		    int mask = SORT_MASKS[m];

		    if (((mode_a | mode_b) & mask) != 0) {
			comp_status = (mode_b & mask) - (mode_a & mask);
			break;
		    }
		}

		User a = (User) users[item_index[i]];
		User b = (User) users[item_index[j]];

		if (comp_status > 0
		    || (0 == comp_status
			&& collator.compare(a.getTag(), b.getTag()) > 0)) {
		    int t = item_index[i];
		    item_index[i] = item_index[j];
		    item_index[j] = t;
		}
	    }
	}
    }

    protected void setItemSelected(NickItem item, boolean state) {
	item.setSelected(state);

	int item_state = state ? ItemEvent.SELECTED : ItemEvent.DESELECTED;
	processItemEvent(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED,
				       this, item_state));

	item.repaint();
    }

    protected void unselectAllItems() {
	Component [] comps = getComponents();

	for (int i = 0; i < comps.length; i++) {
	    NickItem item = (NickItem) comps[i];
	    setItemSelected(item, false);
	}
    }

    public Object [] getSelectedObjects() {
	Component [] comps = getComponents();
  	Vector temp_list = new Vector(comps.length);

	for (int i = 0; i < comps.length; i++) {
  	    NickItem ni = (NickItem) comps[i];
  	    if (ni.isSelected()) {
  		temp_list.addElement(ni);
  	    }
  	}

  	Object [] objs = new Object[temp_list.size()];
  	temp_list.copyInto(objs);

  	return objs;
    }

    public Color getTextBackground() {
	return textbg;
    }

    public void setTextBackground(Color c) {
	this.textbg = c;

	Component [] comps = getComponents();
	for (int i = 0; i < comps.length; i++) {
  	    NickItem ni = (NickItem) comps[i];
  	    ni.setTextBackground(c);
	}
    }

    public Color getTextForeground() {
	return textfg;
    }

    public void setTextForeground(Color c) {
	this.textfg = c;

	Component [] comps = getComponents();
	for (int i = 0; i < comps.length; i++) {
  	    NickItem ni = (NickItem) comps[i];
  	    ni.setTextForeground(c);
  	}
    }

    public Color getSelectedBackground() {
	return selbg;
    }

    public void setSelectedBackground(Color c) {
	this.selbg = c;

	Component [] comps = getComponents();
	for (int i = 0; i < comps.length; i++) {
  	    NickItem ni = (NickItem) comps[i];
  	    ni.setSelectedBackground(c);
  	}
    }

    public Color getSelectedForeground() {
	return selfg;
    }

    public void setSelectedForeground(Color c) {
	this.selfg = c;

	Component [] comps = getComponents();
	for (int i = 0; i < comps.length; i++) {
  	    NickItem ni = (NickItem) comps[i];
  	    ni.setSelectedForeground(c);
  	}
    }

    public Dimension getMinimumSize() {
	return getPreferredSize();
    }

    public Dimension getPreferredSize() {
	return preferred_size;
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

    protected void processActionEvent(ActionEvent e) {
	if (actionListener != null) {
	    actionListener.actionPerformed(e);
	}
    }

    public void addItemListener(ItemListener l) {
	if (null != l) {
	    itemListener = AWTEventMulticaster.add(itemListener, l);
	}
    }

    public void removeItemListener(ItemListener l) {
	if (null != l) {
	    itemListener = AWTEventMulticaster.remove(itemListener, l);
	}
    }

    protected void processItemEvent(ItemEvent e) {
	if (itemListener != null) {
	    itemListener.itemStateChanged(e);
	}
    }

    public void mouseClicked(MouseEvent ev) {
	Component comp = getComponentAt(ev.getPoint());
	// If it wasn't clicked over a NickItem.
	if (!(comp instanceof NickItem)) {
	    unselectAllItems();
	    return;
	}

	NickItem item = (NickItem) comp;

	int modifiers = ev.getModifiers();
	if (0 != (modifiers & MouseEvent.BUTTON1_MASK)
	    || 0 == modifiers) { // WORKAROUND: #32 Netscape Navigator doesn't set it right for BUTTON1.
	    switch (ev.getClickCount()) {
	    case 1:
		boolean was_selected = item.isSelected();

		// If Control wasn't pressed, unselect all other items.
		if (0 == (modifiers & MouseEvent.CTRL_MASK)) {
		    unselectAllItems();
		}

		setItemSelected(item, !was_selected);
		break;
	    case 2:
		processActionEvent(new
		    ActionEvent(this, ActionEvent.ACTION_PERFORMED,
				item.getUser().getTag(), modifiers));
		break;
	    }
	}
    }

    public void mousePressed(MouseEvent ev) {
    }

    public void mouseReleased(MouseEvent ev) {
    }

    public void mouseEntered(MouseEvent ev) {
    }

    public void mouseExited(MouseEvent ev) {
    }
}
