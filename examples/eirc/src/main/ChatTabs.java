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

import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import ar.com.jkohen.irc.Channel;
import ar.com.jkohen.awt.event.ChatPanelListener;

public class ChatTabs extends Canvas implements MouseListener {
    public static final int NORMAL = 0;
    public static final int CURRENT = 1;
    public static final int ALARM = 2;
    public static final int STATES = 3;

    public static final int LEFT = 0;
    public static final int CENTER = 1;
    public static final int RIGHT = 2;
    public static final int COMPLETE = 3;
    public static final int POSITIONS = 4;

    private ActionListener actionListener;

    private Image image;
    private Graphics img_gfx;
    private boolean dirty_tabs;
    private boolean dirty_images;

    private Hashtable name_to_tab;
    private Vector public_tabs;
    private Vector private_tabs;

    // Index of the tab displayed on the left margin.
    private int first_displayed;
    private Tab current_visible;

    private Image images[][];

    public ChatTabs() {
	this.public_tabs = new Vector();
	this.private_tabs = new Vector();
	this.name_to_tab = new Hashtable();

	this.images = new Image[ChatTabs.STATES][ChatTabs.POSITIONS];

	addMouseListener(this);
    }

    public void setImage(int state, int pos, Image img) {
	this.images[state][pos] = img;

	boolean changed_size = false;

	int height;
	while (-1 == (height = img.getHeight(this))) {
	}

	// Use maximum height as a reference.
	if (height > Tab.HEIGHT) {
	    Tab.HEIGHT = height;
	    changed_size = true;
	}

	Image [] state_images = images[state];

	if (CENTER != state &&
	    null != state_images[LEFT] && null != state_images[RIGHT]) {
	    int width_left;
	    int width_right;

	    while (-1 == (width_left = state_images[LEFT].getWidth(this))) {
	    }

	    while (-1 == (width_right = state_images[RIGHT].getWidth(this))) {
	    }

	    int width_sides = width_left + width_right;

	    // Use maximum width as a reference.
	    if (Tab.WIDTH - width_sides > Tab.CENTER_WIDTH) {
		Tab.CENTER_WIDTH = Tab.WIDTH - width_sides;
		changed_size = true;
	    }
	}

	if (changed_size) {
	    this.dirty_tabs = true;
	}

	this.dirty_images = true;
	repaint();
    }

    public void add(String name) {
	Tab tab = new Tab(name);

	if (Channel.isChannel(name)) {
	    public_tabs.addElement(tab);
	} else {
	    private_tabs.addElement(tab);
	}

	name_to_tab.put(name, tab);

	this.dirty_tabs = true;
	repaint();
    }

    public void remove(String name) {
	Tab tab = (Tab) name_to_tab.remove(name);

	if (tab.equals(current_visible)) {
	    this.current_visible = null;
	}

	if (Channel.isChannel(name)) {
	    removeTab(public_tabs, tab);
	} else {
	    removeTab(private_tabs, tab);
	}

	this.dirty_tabs = true;
	repaint();
    }

    private void removeTab(Vector v, Tab t) {
	int index = v.indexOf(t);

	v.removeElementAt(index);

	if (t.getImageIndex() == CURRENT && 0 != v.size()) {
	    if (index > 0) {
		index--;
	    }
	    Tab reverted = (Tab) v.elementAt(index);
	    reverted.setImageIndex(CURRENT);
	}
    }

    public void rename(String old_name, String name) {
	// FIXME: Must use an atomic operator.
	Tab tab = (Tab) name_to_tab.get(old_name);
	tab.setName(name);
	name_to_tab.put(name, tab);
	name_to_tab.remove(old_name);

	this.dirty_tabs = true;
	repaint();
    }

    public void setAlarm(String name, boolean alarm) {
	Tab tab = (Tab) name_to_tab.get(name);

	int current_index = tab.getImageIndex();
	int new_index = tab.equals(current_visible) ? ChatTabs.CURRENT
	    : alarm ? ChatTabs.ALARM
	    : ChatTabs.NORMAL;

	if (current_index != new_index) {
	    tab.setImageIndex(new_index);
	    repaint();
	}
    }

    public String getCurrent() {
	for (Enumeration e = name_to_tab.elements(); e.hasMoreElements(); ) {
	    Tab tab = (Tab) e.nextElement();
	    if (tab.getImageIndex() == CURRENT) {
		return tab.getName();
	    }
	}

	return null;
    }

    public void shiftLeft() {
	if (first_displayed > 0) {
	    this.first_displayed--;
	    repaint();
	}
    }

    public void shiftRight() {
	int visible_tabs = getSize().width / Tab.WIDTH;
	int tabs_in_longest_row = Math.max(public_tabs.size(),
					   private_tabs.size());

	if (first_displayed < tabs_in_longest_row - visible_tabs) {
	    this.first_displayed++;
	    repaint();
	}
    }

    public void makeVisible(String name) {
	makeVisible((Tab) name_to_tab.get(name));
    }

    protected void makeVisible(Tab new_visible) {
	int visible_tabs = getSize().width / Tab.WIDTH;
	Vector v = Channel.isChannel(new_visible.getName())
	    ? public_tabs : private_tabs;
	int tabs_in_row = v.size();
	int index = v.indexOf(new_visible);

	if (tabs_in_row <= visible_tabs) {
	    this.first_displayed = 0;
	} else if (tabs_in_row - index >= visible_tabs) {
	    this.first_displayed = index;
	} else {
	    this.first_displayed = tabs_in_row - visible_tabs;
	}

	if (null != current_visible) {
	    current_visible.setImageIndex(ChatTabs.NORMAL);
	}
	new_visible.setImageIndex(ChatTabs.CURRENT);

	current_visible = new_visible;
	repaint();
    }

    public String getVisible() {
	return current_visible.getName();
    }

    public String getTabAt(Point p) {
	p.translate(first_displayed * Tab.WIDTH, 0);

	Vector v = p.y < Tab.HEIGHT ? private_tabs : public_tabs;
	int index = p.x / Tab.WIDTH;

	if (index < v.size()) {
	    return ((Tab) v.elementAt(index)).getName();
	}

	return null;
    }

    protected void adjustTabsTitle(Graphics g) {
	FontMetrics fm = g.getFontMetrics();

	for (Enumeration e = name_to_tab.elements(); e.hasMoreElements(); ) {
	    Tab tab = (Tab) e.nextElement();
	    String name = tab.getName();
	    String title = name;
	    int title_length = title.length();

	    // Trim tab's title if the name's too long.
	    while (fm.stringWidth(title) > Tab.CENTER_WIDTH) {
		title = name.substring(0, --title_length).concat("...");
	    }
	    tab.setTitle(title);
	}
    }

    private void cacheImages() {
	for (int i = 0; i < STATES; i++) {
	    Image [] state_images = images[i];

	    int width_left = state_images[LEFT].getWidth(this);

	    // Build and cache the complete image.
	    Image image = createImage(Tab.WIDTH, Tab.HEIGHT);
	    Graphics g = image.getGraphics();

	    g.drawImage(state_images[LEFT], 0, 0, this);
	    g.drawImage(state_images[CENTER], width_left, 0,
			Tab.CENTER_WIDTH, Tab.HEIGHT, this);
	    g.drawImage(state_images[RIGHT],
			width_left + Tab.CENTER_WIDTH, 0, this);

	    g.dispose();

	    state_images[COMPLETE] = image;
	}
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

	if (dirty_tabs) {
	    this.dirty_tabs = false;
	    adjustTabsTitle(img_gfx);
	}

	if (dirty_images) {
	    this.dirty_images = false;
	    cacheImages();
	}

	img_gfx.setColor(getBackground());
	img_gfx.fillRect(0, 0, size.width, size.height);
	img_gfx.setColor(getForeground());

	FontMetrics fm = img_gfx.getFontMetrics();
	int baseline = fm.getMaxDescent();
	int text_height = fm.getMaxAscent() + fm.getLeading() + baseline;

	Vector [] v = { private_tabs, public_tabs };

	for (int i = 0; i < v.length; i++) {
	    int x = -first_displayed * Tab.WIDTH;
	    int y = i * Tab.HEIGHT;

	    for (Enumeration e = v[i].elements(); e.hasMoreElements(); ) {
		Tab tab = (Tab) e.nextElement();
		int state = tab.getImageIndex();

		// Render the tab's background.
  		img_gfx.drawImage(images[state][ChatTabs.COMPLETE],
				  x, y, this);

		// Render the title centered on the CENTER image.
  		int width_left = images[state][ChatTabs.LEFT].getWidth(this); 

		String title = tab.getTitle();
		int text_width = fm.stringWidth(title);

		img_gfx.drawString(title, x + width_left
				   + (Tab.CENTER_WIDTH - text_width) / 2,
				   y + Tab.HEIGHT - baseline
				   - (Tab.HEIGHT - text_height) / 2);

		x += Tab.WIDTH;
	    }
	}

	g.drawImage(image, 0, 0, this);
    }

    public Dimension getMinimumSize() {
	return getPreferredSize();
    }

    public Dimension getPreferredSize() {
	int tabs_in_longest_row = Math.max(public_tabs.size(),
					   private_tabs.size());

	return new Dimension(tabs_in_longest_row * Tab.WIDTH, 2 * Tab.HEIGHT);
    }

    public void setBounds(int x, int y, int w, int h) {
	super.setBounds(x, y, w, h);

	if (null != image
	    && (h != image.getHeight(this) || w != image.getWidth(this))) {
	    createImageBuffer(new Dimension(w, h));
	}
	makeVisible(current_visible);
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

    public void mousePressed(MouseEvent ev) {
	if (0 != (ev.getModifiers() & MouseEvent.BUTTON1_MASK)
	    || 0 == ev.getModifiers()) { // WORKAROUND: #32 Netscape Navigator doesn't set it right for BUTTON1.
	    Point p = ev.getPoint();

	    String tab = getTabAt(p);
	    if (null != tab) {
		processActionEvent(new ActionEvent(this,
						  ActionEvent.ACTION_PERFORMED,
						   tab));
	    }

	    // Hack to avoid getting mouse focus, and therefore take keyboard focus from other components.
	    ev.consume();
	}
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
}

class Tab {
    static int HEIGHT = 0;
    final static int WIDTH = 96;
    static int CENTER_WIDTH = 0;

    private String name;
    private String title;
    private int image_index;

    public Tab(String name) {
	this.name = this.title = name;
	this.image_index = ChatTabs.NORMAL;
    }

    public String toString() {
	return getClass().getName().concat("[name=").concat(name)
	    .concat(",title=").concat(title)
	    .concat(",state=").concat(String.valueOf(image_index)).concat("]");
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getTitle() {
	return title;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    public int getImageIndex() {
	return image_index;
    }

    public void setImageIndex(int image_index) {
	this.image_index = image_index;
    }
}
