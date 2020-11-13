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
import java.util.*;
import ar.com.jkohen.irc.Channel;
import ar.com.jkohen.util.ConfigurationProperties;

public class ChannelTree extends Frame
    implements Observer, ActionListener, WindowListener {

    private EIRC eirc;

    private TreeElement root;
    private ParseTreeElement ptroot;

    private VisualTable visualTree;

    /* Configuration properties. */
    private String list_parameter;

    public ChannelTree(EIRC eirc) {
	this(eirc, Locale.getDefault());
    }

    public ChannelTree(EIRC eirc, Locale locale) {
	super();

	this.eirc = eirc;

	ResourceBundle lang = ResourceBundle.getBundle("eirc", locale);

	setTitle(lang.getString("channel_list.title"));

    	GridBagLayout gb = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
      	setLayout(gb);

        gbc.insets = new Insets(2, 2, 2, 2);

        ScrollPane scrollPane
	    = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
	scrollPane.setSize(320, 200);
	visualTree = new VisualTable(eirc, root, locale, scrollPane);
        scrollPane.add(visualTree);

       	gbc.fill = GridBagConstraints.BOTH;
	gbc.weighty = 1.0;
  	gbc.weightx = 1.0;
	gb.setConstraints(scrollPane, gbc);
	add(scrollPane);
  	gbc.weightx = 0.0;
	gbc.weighty = 0.0;
       	gbc.fill = GridBagConstraints.NONE;

	gbc.gridy = 1;

	Panel bottom_panel = new Panel();
	GridBagLayout gb2 = new GridBagLayout();
	GridBagConstraints gbc2 = new GridBagConstraints();
	bottom_panel.setLayout(gb2);

	gbc2.weightx = 1.0;
        gbc2.insets = new Insets(0, 2, 0, 2);

       	Button b = new Button(lang.getString("goto"));
	b.setActionCommand("goto");
        gb2.setConstraints(b, gbc2);
	bottom_panel.add(b);
    	b.addActionListener(this);

       	b = new Button(lang.getString("channel_list.refresh"));
	b.setActionCommand("refresh");
        gb2.setConstraints(b, gbc2);
	bottom_panel.add(b);
	b.addActionListener(this);

       	b = new Button(lang.getString("cancel"));
	b.setActionCommand("cancel");
        gb2.setConstraints(b, gbc2);
	bottom_panel.add(b);
    	b.addActionListener(this);

        gb.setConstraints(bottom_panel, gbc);
	add(bottom_panel);

	addWindowListener(this);
    }

    public ParseTreeElement makeParseTree(String pattern) {
	SimpleParser sp = new SimpleParser(pattern);
        ParseTreeElement pt = new ParseTreeElement(TreeElement.NODE, "root");

        sp.parse(pt);

        return pt;
    }

    public void loadChannels(Vector data) {
        this.root = new TreeElement(TreeElement.NODE, "root");
        makeTreeFromParseTree(ptroot, root);

      	for (Enumeration e = data.elements(); e.hasMoreElements(); ) {
    	    ChannelItem channel = (ChannelItem) e.nextElement();
            sortIntoTree(root, ptroot, channel);
        }

        if (null != visualTree) {
	    visualTree.setRoot(root);
  	    visualTree.repaint();
        }
    }

    private boolean sortIntoTree(TreeElement te, ParseTreeElement pte,
				 ChannelItem channel) {
        String [] leaves = pte.getAllLeaveNames();

        for (int i = 0; i < leaves.length; i++) {
            RegExp regexp = new RegExp(leaves[i]);

            if (regexp.matches(channel.getTag())) {
                te.addLeaf(channel.getTag());
                te.getLeaf(channel.getTag())
		    .setDescription(channel.getTopic());
                te.getLeaf(channel.getTag())
		    .setUsers(String.valueOf(channel.getUsersCount()));

                return true;
            }
        }

        if (pte.hasNodes()) {
            TreeComponent [] ptelements = pte.getAllNodes();
            for (int i = 0; i < ptelements.length; i++) {
                if (sortIntoTree(te.getNode(ptelements[i].getName()),
				 (ParseTreeElement) ptelements[i], channel)) {
		    return true;
		}
            }
        }

        return false;
    }

    private void makeTreeFromParseTree (ParseTreeElement pte, TreeElement te) {
        String [] nodeNames = pte.getAllNodeNames();

        te.addNodes(nodeNames);

        for (int i = 0; i < nodeNames.length; i++) {
            if (pte.getNode(nodeNames[i]).hasNodes()) {
                makeTreeFromParseTree((ParseTreeElement)
				      pte.getNode(nodeNames[i]),
				      te.getNode(nodeNames[i]));
            }
        }
    }

    public void update(Observable o, Object arg) {
	ConfigurationProperties props = (ConfigurationProperties) o;

	if (null == arg || arg.equals("channel_tree_pattern")) {
	    String pattern = props.getString("channel_tree_pattern");
	    this.ptroot = makeParseTree(pattern);
	}
	if (null == arg || arg.equals("list_parameter")) {
	    this.list_parameter = props.getString("list_parameter");
	}
    }

    public void setBackground(Color c) {
        super.setBackground(c);
        visualTree.setBackground(c);
    }

    public void setForeground(Color c) {
        super.setForeground(c);
        visualTree.setForeground(c);
    }

    public void setSelectedBackground(Color c) {
        visualTree.setSelectedBackground(c);
    }

    public void setSelectedForeground(Color c) {
        visualTree.setSelectedForeground(c);
    }

    public void actionPerformed(ActionEvent ev) {
	String ac = ev.getActionCommand();

	if (ac.equals("goto")) {
	    if (visualTree.selectedIsChannel()) {
		visualTree.joinSelected();
	    }
	} else if (ac.equals("refresh")) {
	    String p[] = { list_parameter };

	    eirc.sendMessage("list", p);
	} else if (ac.equals("cancel")) {
	    dispose();
	}
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
	dispose();
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }
}
