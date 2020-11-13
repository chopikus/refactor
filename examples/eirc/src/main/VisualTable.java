/*
Eteria IRC Client, an RFC 1459 compliant client program written in Java.
Copyright (C) 2001  Christian Buck <cbuck at lantis.de>

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
import java.awt.image.*;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;
import ar.com.jkohen.util.Resource;
import java.io.IOException;
import java.awt.ScrollPane;

public class VisualTable extends Canvas implements MouseListener {
    public static final int EXPANDED = 0;
    public static final int COLLAPSED = 1;
    public static final int ENTRY = 2;
    public static final int LEAF = 3;

    private EIRC eirc;

    private String users_postfix;

    private Color selbg = SystemColor.textHighlight;
    private Color selfg = SystemColor.textHighlightText;

    private TreeElement root;
    private TreeElement selected;
    private Vector treeList = new Vector(0, 1);
    private Vector cells = new Vector(0, 1);

    private Image image;
    private Graphics img_gfx;

    private ResourceBundle images;
    
    private Image imgCollapsed;
    private Image imgExpanded;
    private Image imgEntry;
    private Image imgLeaf;
    
    private ScrollPane superscrollpane;
    private boolean use_IEfix;

    public VisualTable(EIRC eirc, TreeElement root) {
	this(eirc, root, Locale.getDefault());
	}

	public VisualTable(EIRC eirc, TreeElement root, Locale locale, ScrollPane scrollpane) {
	this(eirc, root, locale);
	superscrollpane = scrollpane;
	use_IEfix = true;
	}

    public VisualTable(EIRC eirc, TreeElement root, Locale locale) {
	this.eirc = eirc;
	this.root = root;
	this.images = ResourceBundle.getBundle("Images", locale);

	imgCollapsed = getResourceImage("channel_tree.collapsed");
	prepareImage(imgCollapsed,13,13,this);
	imgExpanded = getResourceImage("channel_tree.expanded");
	prepareImage(imgExpanded,13,13,this);
	imgEntry = getResourceImage("channel_tree.entry");
	prepareImage(imgEntry,13,13,this);
	imgLeaf = getResourceImage("channel_tree.leaf");
	prepareImage(imgLeaf,13,13,this);
	ResourceBundle lang = ResourceBundle.getBundle("eirc", locale);

	this.users_postfix = " " + lang.getString("channel_list.users");
	
	
	addMouseListener(this);
    }

    public void setRoot(TreeElement r) {
	this.root = r;
    }

    public boolean selectedIsChannel () {
	if (selected != null) {
	    return !selected.isNode();
	}

	return false;
    }

    public void joinSelected () {
	if (selected != null) {
	    joinChannel(selected.getName());
	}
    }

    public void joinChannel(String name) {
	if (null == eirc.getChannel(name)) {
	    String p[] = { name };
	    eirc.sendMessage("join", p);
	} else {
	    eirc.showPanel(name);
	}
    }

    void makeListFromTree(TreeElement node, Vector prefixes) {
	/*
	  Searches in the complete tree, only through expanded branches,
	  and returns them as a 2-D list.
	*/

	if (node.hasLeaves()) {
	    String[] leaves = node.getAllLeaveNames();
	    for (int i=0;i<leaves.length;i++) {
		TreeListEntry tle = new TreeListEntry(prefixes);
		tle.addPrefix (VisualTable.LEAF);
		tle.setContent(leaves[i]);
		tle.setNode((TreeElement) node.getLeaf(leaves[i]));
		treeList.addElement(tle);
	    }
	}

	if (node.hasNodes()) {
	    String[] nodes = node.getAllNodeNames();
	    for (int i=0;i<nodes.length;i++) {
		if (node.getNode(nodes[i]).isExpanded()) {
		    TreeListEntry tle = new TreeListEntry(prefixes);
		    tle.addPrefix(VisualTable.EXPANDED);
		    tle.setContent(nodes[i]);
		    tle.setNode((TreeElement) node.getNode(nodes[i]));
		    treeList.addElement(tle);

		    Vector newPrefixes = (Vector) prefixes.clone();
		    newPrefixes.addElement(new Integer(VisualTable.ENTRY));
		    makeListFromTree ((TreeElement) node.getNode(nodes[i]),
				      newPrefixes);
		} else {
		    TreeListEntry tle = new TreeListEntry(prefixes);
		    tle.addPrefix(VisualTable.COLLAPSED);
		    tle.setContent(nodes[i]);
		    tle.setNode((TreeElement) node.getNode(nodes[i]));
		    treeList.addElement(tle);
		}
	    }
	}
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

    private Image getResourceImage(String name) {
	String resource_name = images.getString(name);
	try {
	    return Resource.createImage(resource_name, this);
	} catch (IOException e) {
	    System.err.println(e);
	}
	return null;
    }

    public void paint(Graphics g) {
	/* JK's ToDo:
	 *  * Separate build and display functions.
	 * Christian's ToDo:
	 *  * avoid the damn flickering
	 *  * bg-image?
	 *  * icons?
	 */

	treeList.removeAllElements();
	cells.removeAllElements();
	makeListFromTree(root, new Vector(0, 1));

	Dimension size = getSize();

	if (null == img_gfx
	    || size.width != image.getWidth(this)
	    || size.height != image.getHeight(this)) {
	    createImageBuffer(size);
	}

	img_gfx.setColor(getBackground());
	img_gfx.fillRect(0, 0, size.width, size.height);
	img_gfx.setColor(getForeground());

	FontMetrics fm = img_gfx.getFontMetrics();

	int x = 0;
	int y = 0;
	int line = 0;
	int lineHeight = fm.getHeight() + 1;
	int baseline = fm.getMaxDescent();

	// Build and display the first column.
	for (Enumeration e = treeList.elements(); e.hasMoreElements(); ) {
	    TreeListEntry entry = (TreeListEntry) e.nextElement();

	    Vector prefixes = entry.getPrefixes();
	    String prestring = ""; //besser Stringbuffer
	    for (Enumeration p = prefixes.elements(); p.hasMoreElements(); ) {
		Integer i = (Integer) p.nextElement();

		switch (i.intValue()) {
		case VisualTable.COLLAPSED:
		    //img_gfx.drawImage(getResourceImage("channel_tree.collapsed"), 2, line * lineHeight,13,13,this);
		    img_gfx.drawImage(imgCollapsed, 2, line * lineHeight,
				      13, 13, this);
		    // prestring = prestring.concat("[+] ");
		    break;
		case VisualTable.EXPANDED:
		    //img_gfx.drawImage(getResourceImage("channel_tree.expanded"), 2, line * lineHeight,13,13,this);
		    img_gfx.drawImage(imgExpanded, 2, line * lineHeight,
				      13, 13, this);
		    // prestring = prestring.concat("[-] ");
		    break;
		case VisualTable.ENTRY:
		    // prestring = prestring.concat(" | ");
		    break;
		case VisualTable.LEAF:
		    //img_gfx.drawImage(getResourceImage("channel_tree.leaf"), 2, line * lineHeight,13,13,this);
		    img_gfx.drawImage(imgLeaf, 2, line * lineHeight,
				      13, 13, this);
		    // prestring = prestring.concat("--- ");
		    break;
		}
	    }

	    boolean node_selected = entry.getNode().isSelected();

	    String content = prestring + entry.getContent();

	    int x1 = 2 + 13 + 2; // One icon in front of the text - needs to be more flexible.
	    int x2 = x1 + fm.stringWidth(content);
	    int y1 = line * lineHeight;
	    int y2 = (line + 1) * lineHeight - 1;

	    cells.addElement(new SimpleRectangle(x1, y1, x2, y2,
						 entry.getNode()));

	    if (node_selected) {
		img_gfx.setColor(selbg);
		img_gfx.fillRect(x1, y1, size.width, lineHeight);
		img_gfx.setColor(getForeground());
	    }

	    // img_gfx.drawString(prestring, x1, y2 - baseline);
		
	    if (node_selected || null != eirc.getChannel(entry.getContent())) {
		img_gfx.setColor(selfg);
	    }

	    img_gfx.drawString(entry.getContent(),
			       x1 + fm.stringWidth(prestring), y2 - baseline);
	    img_gfx.setColor(getForeground());

	    line++;
	}

	// Calculate width of first column.
	int maxindent = 0;
	for (Enumeration e = cells.elements(); e.hasMoreElements(); ) {
	    SimpleRectangle cell = (SimpleRectangle) e.nextElement();
	    if (maxindent < cell.getX2()) {
		maxindent = cell.getX2();
	    }
	}

	// Display second column.
	line = 0;
	for (Enumeration e = treeList.elements(); e.hasMoreElements(); ) {
	    TreeListEntry entry = (TreeListEntry) e.nextElement();

	    if (!entry.getNode().isNode()) {
		String users = entry.getNode().getUsers() + users_postfix;
		int x1 = maxindent + 6;
		int x2 = x1 + fm.stringWidth(users);
		int y1 = line * lineHeight;
		int y2 = (line + 1) * lineHeight;

		cells.addElement(new SimpleRectangle(x1, y1, x2, y2,
						     entry.getNode()));

		if (entry.getNode().isSelected()
		    || null != eirc.getChannel(entry.getContent())) {
		    img_gfx.setColor(selfg);
		}

		img_gfx.drawString(users, x1, y2 - baseline);
		img_gfx.setColor(getForeground());
	    }

	    line++;
	}

	// Calculate width of second column.
	for (Enumeration e = cells.elements(); e.hasMoreElements(); ) {
	    SimpleRectangle cell = (SimpleRectangle) e.nextElement();
	    if (maxindent < cell.getX2()) {
		maxindent = cell.getX2();
	    }
	}

	// Display third column.
	line = 0;
	for (Enumeration e = treeList.elements(); e.hasMoreElements(); ) {
	    TreeListEntry entry = (TreeListEntry) e.nextElement();

	    if (!entry.getNode().isNode()) {
		String topic = entry.getNode().getDescription();
		int x1 = maxindent + 6;
		int x2 = x1 + fm.stringWidth(topic);
		int y1 = line * lineHeight;
		int y2 = (line + 1) * lineHeight;

		cells.addElement(new SimpleRectangle(x1, y1, x2, y2,
						     entry.getNode()));

		if (entry.getNode().isSelected()
		    || null != eirc.getChannel(entry.getContent())) {
		    img_gfx.setColor(selfg);
		}

		img_gfx.drawString(topic, x1, y2 - baseline);
		img_gfx.setColor(getForeground());
	    }

	    line++;
	}

	Dimension preferred_size = getPreferredSize();
	if (!preferred_size.equals(size)) {
	    setSize(preferred_size);
	    repaint();
	    superscrollpane.doLayout(); //IE Bugfix, nasty but this works.
	    return;
	}

	g.drawImage(image, 0, 0, this);
    }

    public Dimension getPreferredSize() {
	int maxwidth = 0;
	int maxheight = 0;

	for (Enumeration e = cells.elements(); e.hasMoreElements(); ) {
	    SimpleRectangle cell = (SimpleRectangle) e.nextElement();
	    if (maxwidth < cell.getX2()) {
		maxwidth = cell.getX2();
	    }
	    if (maxheight < cell.getY2()) {
		maxheight = cell.getY2();
	    }
	}

	Container parent = getParent();
	Dimension preferred_size = new Dimension(maxwidth, maxheight);

	if (parent instanceof ScrollPane) {
	    Dimension vp_size = ((ScrollPane) parent).getViewportSize();
	    preferred_size.width = Math.max(preferred_size.width,
					    vp_size.width);
	    preferred_size.height = Math.max(preferred_size.height,
					     vp_size.height);
	}

	return preferred_size;
    }

    public Dimension getMinimumSize() {
	return getPreferredSize();
    }

    public Color getSelectedBackground() {
	return selbg;
    }

    public void setSelectedBackground(Color c) {
	this.selbg = c;
    }

    public Color getSelectedForeground() {
	return selfg;
    }

    public void setSelectedForeground(Color c) {
	this.selfg = c;
    }

    public void mouseClicked(MouseEvent ev) {
	Point p = ev.getPoint();

	for (Enumeration e = cells.elements(); e.hasMoreElements(); ) {
	    SimpleRectangle rect = (SimpleRectangle) e.nextElement();

	    // 	    if (p.x >= rect.getX1() && p.x <= rect.getX2()
	    // 		&& p.y >= rect.getY1() && p.y <= rect.getY2()) {
	    if (p.y >= rect.getY1() && p.y <= rect.getY2()) {
		TreeElement te = rect.te;
		te = rect.te;

		if (null != selected) {
		    selected.setSelected(false);
		}

		te.setSelected(true);
		selected = te;

		if (te.isNode()) {
		    te.setExpanded(!te.isExpanded());
		} else if (2 == ev.getClickCount()) {
		    joinSelected();
		}

		repaint();

		break;
	    }
	}
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
}
