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
import java.io.IOException;
import java.net.URL;
import java.text.Collator;
import java.util.*;
import ar.com.jkohen.awt.*;
import ar.com.jkohen.irc.*;
import ar.com.jkohen.text.Completer;
import ar.com.jkohen.util.CollatedHashtable;
import ar.com.jkohen.util.ConfigurationProperties;
import ar.com.jkohen.util.Resource;
import com.splendid.awtchat.SmileyTextArea;
//  import htmllayout.HtmlLayout;

class ChannelWindow extends ChatWindow
    implements ActionListener, MouseListener, KeyListener {

    protected TextField topic;
    protected TextFieldHistory entry;
    protected ScrollPane nick_list_pane;
    protected NickList nick_list;
    protected ImageButton whois;
    protected ImageButton close;
    protected PopupMenu popup_menu;
    private TextAttributePicker text_attr_picker;

    /* Configuration properties. */
    private boolean have_halfops;
    private boolean text_attr_memory;

    private EIRC eirc;
    private Channel channel;

    private ResourceBundle lang;
    private ResourceBundle images;

    private int item_renderer;
    private Collator collator;
    private CollatedHashtable nick_item_list;

    private User popup_user;
    private boolean is_op;

    /* Nick Completion. */
    private Completer nick_completer;
    private boolean completion_in_progress;
    private boolean complete_at_bol;

    public ChannelWindow(EIRC eirc, Channel channel) {
	this(eirc, channel, Locale.getDefault());
    }

    public ChannelWindow(EIRC eirc, Channel channel, Locale locale) {
	super(channel.getTag(), locale);

  	this.eirc = eirc;
	this.channel = channel;

	this.lang = ResourceBundle.getBundle("eirc", locale);
	this.images = ResourceBundle.getBundle("Images", locale);

    	this.item_renderer = NickItem.BULLET_RENDERER;
  	this.collator = RFC1459.getCollator();
  	this.nick_item_list = new CollatedHashtable(collator);

	// It'll be initialized in completeNick when needed.
	this.nick_completer = new Completer(null);

//  	String html_layout;

//  	try {
//  	    html_layout = new String(Resource.getResource("ChannelWindow.html", this));
//  	} catch (java.io.IOException e) {
//  	    System.err.println(e);
//  	    return;
//  	}

//  	setLayout(new HtmlLayout(html_layout));

	GridBagLayout gb = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();

	setLayout(gb);

	gbc.gridy = 0;

	/* Component's initialization
	 */

	topic = new TextField();
	gbc.weightx = 1.0;
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gb.setConstraints(topic, gbc);
	add(topic/*, "topic"*/);
	gbc.fill = GridBagConstraints.NONE;
	gbc.weightx = 0.0;
	gbc.gridwidth = 1;

	/* Next Row */
	gbc.gridy++;

	gbc.weightx = 1.0;
	gbc.weighty = 1.0;
	gbc.fill = GridBagConstraints.BOTH;
	gb.setConstraints(text_canvas, gbc);
	add(text_canvas/*, "text_canvas"*/);
	gbc.fill = GridBagConstraints.NONE;
	gbc.weighty = 0.0;
	gbc.weightx = 0.0;

	Panel right_panel = new Panel();
	GridBagLayout right_gb = new GridBagLayout();
	right_panel.setLayout(right_gb);

	/* Backup Grid's Y-Pos. */
	int old_gridy = gbc.gridy;

	/* Right Panel begins here. */
	gbc.weightx = 1.0;

	this.nick_list_pane = new ScrollPane();
  	Adjustable adj;
	adj = nick_list_pane.getHAdjustable();
	adj.setUnitIncrement(10);
	adj = nick_list_pane.getVAdjustable();
	adj.setUnitIncrement(50);

	this.nick_list = new NickList();
	nick_list_pane.add(nick_list);

	gbc.weighty = 1.0;
	gbc.fill = GridBagConstraints.BOTH;
//  	gbc.gridwidth = GridBagConstraints.REMAINDER; -- Paulo's Design
	right_gb.setConstraints(nick_list_pane, gbc);
	right_panel.add(nick_list_pane/*, "nick_list"*/);
//  	gbc.gridwidth = 1; -- Paulo's Design
	gbc.fill = GridBagConstraints.NONE;
	gbc.weighty = 0.0;

	/* Next Row */
	gbc.gridy++;

	whois = new ImageButton();
	whois.setImage(ImageButton.ENTERED,
		       getResourceImage("channel.whois.entered"));
	whois.setImage(ImageButton.EXITED,
		       getResourceImage("channel.whois.exited"));
	whois.setImage(ImageButton.PRESSED,
		       getResourceImage("channel.whois.pressed"));
//  	gbc.anchor = GridBagConstraints.EAST; -- Paulo's Design
	gbc.anchor = GridBagConstraints.WEST;
	right_gb.setConstraints(whois, gbc);
	right_panel.add(whois/*, "whois"*/);

	/* Next Row */
	gbc.gridy++;

	close = new ImageButton();
	close.setImage(ImageButton.ENTERED,
		       getResourceImage("channel.close.entered"));
	close.setImage(ImageButton.EXITED,
		       getResourceImage("channel.close.exited"));
	close.setImage(ImageButton.PRESSED,
		       getResourceImage("channel.close.pressed"));
	gbc.anchor = GridBagConstraints.WEST;
	right_gb.setConstraints(close, gbc);
	right_panel.add(close/*, "close"*/);

	gbc.anchor = GridBagConstraints.CENTER;
	gbc.weightx = 0.0;

	/* Right Panel ends here. */

	/* Restore Grid's Y-Pos. */
	gbc.gridy = old_gridy;

	gbc.weighty = 1.0;
	gbc.gridheight = 2;
	gbc.fill = GridBagConstraints.BOTH;
	gb.setConstraints(right_panel, gbc);
	add(right_panel);
	gbc.fill = GridBagConstraints.NONE;
	gbc.gridheight = 1;
	gbc.weighty = 0.0;


	// From here below, everything fills the space horizontally.
  	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.weightx = 1.0;

	/* Next Row */
	gbc.gridy++;

	// Build picker to use indexed colors.
	this.text_attr_picker
	    = new TextAttributePicker(SmileyTextArea.getColorPalette(), 16);
	gbc.gridx = 0;
	gb.setConstraints(text_attr_picker, gbc);
	add(text_attr_picker);
	gbc.gridx = -1;

	IndexedColorPicker color_picker
	    = (IndexedColorPicker) text_attr_picker.getColorPicker();

	color_picker.setDisposition(0, 1);
	color_picker.setItemSize(16, 12);
	color_picker.setGap(2, 2);

	/* Next Row */
	gbc.gridy++;

	this.entry = new TextFieldHistory();
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gb.setConstraints(entry, gbc);
	add(entry/*, "entry"*/);


	popup_menu = new PopupMenu();
	// We delay the building of the menu up to the moment when our nick is added to the list. This is needed in order to know whether we are opped or not.
	add(popup_menu);


	/* Event Listeners
	 */

	channel.addObserver(this);
	topic.addActionListener(this);
	popup_menu.addActionListener(this);
	nick_list.addActionListener(this);
	nick_list.addMouseListener(this);
	entry.addActionListener(this);
	entry.addKeyListener(this);
  	whois.addMouseListener(this);
  	close.addMouseListener(this);
	text_attr_picker.addActionListener(this);
    }

    public void requestFocus() {
	entry.requestFocus();
    }

    public Channel getChannel() {
	return channel;
    }

    protected String getNick() {
	return eirc.getNick();
    }

    protected void visitURL(URL url) {
	eirc.visitURL(url);
    }

    protected void joinChannel(String name) {
	if (null == eirc.getChannel(name)) {
	    String p[] = { name };
	    eirc.sendMessage("join", p);
	} else {
	    eirc.showPanel(name);
	}
    }


    /*
     * Nick completion methods.
     */

    private void resetNickCompletion() {
	// Avoid the burden of invoking nick_completer.reset() if it's not needed.
	if (completion_in_progress) {
	    this.completion_in_progress = false;
	    this.complete_at_bol = false;
	    nick_completer.reset();
	}
    }

    /**
     * Returns a <code>String []</code> containing all nick names in the <code>Channel</code>.
     */
    private String [] getAllTags() {
	User [] users = channel.elements();
	String [] tags = new String [users.length];

	for (int i = 0; i < users.length; i++) {
	    tags[i] = users[i].getTag();
	}

	return tags;
    }

    private void completeNick() {
	char [] text = entry.getText().toCharArray();
	int pos = entry.getCaretPosition();
	int begin = pos - 1;
  	int end = pos;

	/* Find nick boundaries. */

	// If last completion was at the beginning of line, special values are needed for begin and end because ": " has been appended to the completion, and the normal code to search for nick boundaries can't get it right.
	if (complete_at_bol) {
	    begin = 0;
	    end = pos;
	} else {
	    while (begin >= 0 && RFC1459.isDeclaredChar(text[begin])) {
		begin--;
	    }
	    while (end < text.length && RFC1459.isDeclaredChar(text[end])) {
		end++;
	    }
	    begin++;
	}
	/* Try to find a completion. */

	if (!completion_in_progress) {
	    String pattern = String.valueOf(text, begin, end - begin);
	    nick_completer.loadList(getAllTags());
	    nick_completer.setPattern(pattern);

	    this.completion_in_progress = true;
	}

	String completion = nick_completer.nextCompletion(collator);
	if (null == completion) { // None found.
	    return;
	}

	/* Insert the completion into the text entry. */

	// If it's the first word in the line, append ": ".
	if (0 == begin) {
	    completion += ": ";
	    this.complete_at_bol = true;
	}

	String new_text = String.valueOf(text, 0, begin)
	    .concat(completion)
	    .concat(String.valueOf(text, end, text.length - end));

	entry.setText(new_text);

	// Set the caret position to the end of the completed nick.
	entry.setCaretPosition(begin + completion.length());
    }

    public int getItemRenderer() {
	return item_renderer;
    }

    public void setItemRenderer(int renderer) {
	this.item_renderer = renderer;

	synchronized (nick_item_list) {
	    Component [] comps = nick_list.getComponents();

	    nick_list.removeAll();

	    for (int i = 0; i < comps.length; i++) {
		NickItem old_ni = (NickItem) comps[i];
		User user = old_ni.getUser();
		String nick = user.getTag();
		NickItem ni = NickItem.newInstance(user, item_renderer);

		nick_list.add(ni);
		nick_item_list.put(nick, ni);
	    }
	}
	nick_list.validate();
	nick_list_pane.validate();
	nick_list.repaint();
    }


    private void update_channel(Object hints) {
	// Parse Channel update hints.
	StringTokenizer st = new StringTokenizer(hints.toString());
	String action = st.nextToken();

	if (null == action || action.equals("topic")) {
	    topic.setText(channel.getTopic());
	}

	if (null == action || action.equals("mode")) {
	    User user = channel.get(eirc.getNick());
	    if (null != user) { // It's possible to receive other commands before NAMES on JOIN.
		this.is_op = user.isOp();
	    }

	    topic.setEditable(is_op || channel.isTopicSettable());

	    buildPopupMenu(popup_menu);
	}

	if (action.equals("add")) {
	    String nick = st.nextToken();
	    User user = channel.get(nick);
	    NickItem ni = NickItem.newInstance(user, item_renderer);

	    synchronized (nick_item_list) {
		nick_list.add(ni);
		nick_item_list.put(nick, ni);
	    }
    	    nick_list.validate();
  	    nick_list_pane.validate();
    	    nick_list.repaint();

	    // Update the nick completer, if there's a completion in progress.
	    if (completion_in_progress) {
		nick_completer.loadList(getAllTags());
	    }
  	} else if (action.equals("remove")) {
	    String nick = st.nextToken();

	    synchronized (nick_item_list) {
		nick_list.remove((Component) nick_item_list.remove(nick));
	    }
  	    nick_list.validate();
  	    nick_list_pane.validate();
    	    nick_list.repaint();

	    // Update the nick completer, if there's a completion in progress.
	    if (completion_in_progress) {
		nick_completer.loadList(getAllTags());
	    }
  	} else if (action.equals("rename")) {
	    String nick = st.nextToken();
	    String new_nick = st.nextToken();

	    synchronized (nick_item_list) {
		NickItem ni = (NickItem) nick_item_list.remove(nick);
		nick_item_list.put(new_nick, ni);
	    }
	    // The nick list isn't modified directly, therefore the Container thinks it's valid. Hence, doLayout needs to be implicitly invoked.
    	    nick_list.doLayout();
  	    nick_list_pane.validate();
    	    nick_list.repaint();

	    // Update the nick completer, if there's a completion in progress.
	    if (completion_in_progress) {
		nick_completer.loadList(getAllTags());
	    }
  	} else if (action.equals("mode")) {
	    // The nick list isn't modified directly, therefore the Container thinks it's valid. Hence, doLayout needs to be implicitly invoked.
    	    nick_list.doLayout();
  	    nick_list_pane.validate();
    	    nick_list.repaint();
	}
    }

    private void update_properties(ConfigurationProperties props, Object arg) {
	super.update(props, arg);

	if (null == arg || arg.equals("have_halfops")) {
	    this.have_halfops = props.getBoolean("have_halfops");
	}
	if (null == arg || arg.equals("text_attr_memory")) {
	    this.text_attr_memory = props.getBoolean("text_attr_memory");
	}
	if (null == arg || arg.equals("nick_item_renderer")) {
	    setItemRenderer(props.getInt("nick_item_renderer"));
	}
	if (null == arg || arg.equals("nick_item_sort_method")) {
	    nick_list.setSortMethod(props.getInt("nick_item_sort_method"));
	}
    }

    public void update(Observable o, Object arg) {
	if (o instanceof Channel) {
	    update_channel(arg);
	} else {
	    update_properties((ConfigurationProperties) o, arg);
	}
    }

    /**
     * Sends the specified text to the channel.
     *
     * @param text the text to send to the channel.
     */
    public void sendText(String text) {
	String [] p = { channel.getTag(), text };
	eirc.sendMessage("privmsg", p);
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

    public void setBackground(Color c) {
	super.setBackground(c);
  	nick_list.setBackground(c);
    }

    public void setForeground(Color c) {
	super.setForeground(c);
  	nick_list.setForeground(c);
    }

    public void setTextBackground(Color c) {
	text_canvas.setBackground(c);
	entry.setBackground(c);
	topic.setBackground(c);
  	nick_list.setTextBackground(c);
    }

    public void setTextForeground(Color c) {
	text_canvas.setForeground(c);
	entry.setForeground(c);
	topic.setForeground(c);
  	nick_list.setTextForeground(c);
    }

    public void setSelectedBackground(Color c) {
  	nick_list.setSelectedBackground(c);
    }

    public void setSelectedForeground(Color c) {
  	nick_list.setSelectedForeground(c);
    }

    private synchronized void buildPopupMenu(PopupMenu pm) {
	String tag = channel.getTag();

	Menu mn;
	MenuItem mi;

	pm.removeAll();

	mi = new MenuItem(lang.getString("nicklist.popup.query"));
	mi.setActionCommand("query");
	pm.add(mi);
	pm.addSeparator();
	mi = new MenuItem(lang.getString("nicklist.popup.ping"));
	mi.setActionCommand("ping");
	pm.add(mi);
	mi = new MenuItem(lang.getString("nicklist.popup.whois"));
	mi.setActionCommand("whois");
	pm.add(mi);
	pm.addSeparator();
	mi = new MenuItem(lang.getString("nicklist.popup.ignore"));
	mi.setActionCommand("ignore");
	pm.add(mi);
	mi = new MenuItem(lang.getString("nicklist.popup.unignore"));
	mi.setActionCommand("unignore");
	pm.add(mi);

	if (is_op) {
	    pm.addSeparator();
	    mn = new Menu(lang.getString("nicklist.popup.operators"));
	    mn.addActionListener(this);
	    mi = new MenuItem(lang.getString("nicklist.popup.kick"));
	    mi.setActionCommand("kick " + tag);
	    mn.add(mi);
	    mi = new MenuItem(lang.getString("nicklist.popup.kban"));
	    mi.setActionCommand("kban");
	    mn.add(mi);
	    mn.addSeparator();
	    mi = new MenuItem(lang.getString("nicklist.popup.ban"));
	    mi.setActionCommand("mode " + tag + " +b");
	    mn.add(mi);
	    mi = new MenuItem(lang.getString("nicklist.popup.unban"));
	    mi.setActionCommand("mode " + tag + " -b");
	    mn.add(mi);
	    mn.addSeparator();
	    mi = new MenuItem(lang.getString("nicklist.popup.voice"));
	    mi.setActionCommand("mode " + tag + " +v");
	    mn.add(mi);
	    mi = new MenuItem(lang.getString("nicklist.popup.unvoice"));
	    mi.setActionCommand("mode " + tag + " -v");
	    mn.add(mi);
	    mn.addSeparator();
	    mi = new MenuItem(lang.getString("nicklist.popup.op"));
	    mi.setActionCommand("mode " + tag + " +o");
	    mn.add(mi);
	    mi = new MenuItem(lang.getString("nicklist.popup.deop"));
	    mi.setActionCommand("mode " + tag + " -o");
	    mn.add(mi);
	    if (have_halfops) {
		mn.addSeparator();
		mi = new MenuItem(lang.getString("nicklist.popup.hop"));
		mi.setActionCommand("mode " + tag + " +h");
		mn.add(mi);
		mi = new MenuItem(lang.getString("nicklist.popup.dehop"));
		mi.setActionCommand("mode " + tag + " -h");
		mn.add(mi);
	    }
	    pm.add(mn);
	}
    }

    public void actionPerformed(ActionEvent ev) {
	Object comp = ev.getSource();

	if (comp instanceof MenuItem) {
	    eirc.sendCommand(ev.getActionCommand() + " " + popup_user.getTag(),
			     this);
	} else if (comp.equals(topic)) {
	    String p[] = { channel.getTag(), topic.getText() };
	    eirc.sendMessage("topic", p);
	} else if (comp.equals(nick_list)) {
	    String item = ev.getActionCommand();
	    eirc.openPrivate(item);
	    eirc.showPanel(item);
	} else if (comp.equals(entry)) {
	    String text = entry.getText();
	    if (text.length() <= 0) {
		return;
	    }

	    // See if it's a command.
	    if (text.charAt(0) == '/') {
		// Remove slash.
		text = text.substring(1);

		// Avoid empty (or pure white space) input.
		if (text.trim().length() > 0) {
		    eirc.sendCommand(text, this);
		}
	    } else {
		printMyPrivmsg(entry.getText());
		sendText(entry.getText());
	    }

//  	    if (text_attr_memory) {
//  	    } else {
		entry.setText("");
//  	    }
	} else if (comp.equals(text_attr_picker)) {
	    String text = entry.getText();
	    String new_text = ev.getActionCommand();

	    // Insert attributes at caret position.
	    int pos = entry.getCaretPosition();

	    text = text.substring(0, pos)
		.concat(new_text)
		.concat(text.substring(pos));

	    entry.setText(text);
	    entry.setCaretPosition(pos + new_text.length());
	}
    }

    public void mouseClicked(MouseEvent ev) {
	Component comp = ev.getComponent();

	if (comp.equals(whois)) {
	    Object [] sel_items = nick_list.getSelectedObjects();
	    boolean selected = false;
	    String s = "";

	    for (int i = 0; i < sel_items.length; i++) {
		if (selected) {
		    s = s.concat(",");
		}
		selected = true;

		NickItem ni = (NickItem) sel_items[i];
		s = s.concat(ni.getUser().getTag());
	    }

	    // Was a nick selected?
	    if (selected) {
		String p[] = { s };
		eirc.sendMessage("whois", p);
	    } else {
		printError(lang.getString("eirc.select_nicks"));
	    }
	} else if (comp.equals(close)) {
	    String p[] = { getPanelTag() };
	    eirc.sendMessage("part", p);
	}
    }

    public void mousePressed(MouseEvent ev) {
	// The following block processes NickList's events.
	if (ev.isPopupTrigger()
	    || 0 != (ev.getModifiers() & MouseEvent.BUTTON3_MASK)) { // WORKAROUND: #27 MSIE doesn't set popupTrigger.
	    Component comp = nick_list.getComponentAt(ev.getPoint());
	    if (comp instanceof NickItem) {
		NickItem item = (NickItem) comp;
  		this.popup_user = item.getUser();
  		popup_menu.show(nick_list, ev.getX(), ev.getY());
	    }
	}
    }

    public void mouseReleased(MouseEvent ev) {
    }

    public void mouseEntered(MouseEvent ev) {
    }

    public void mouseExited(MouseEvent ev) {
    }

    public void keyPressed(KeyEvent ev) {
	switch (ev.getKeyCode()) {
	case KeyEvent.VK_TAB:
	    ev.consume();
	    completeNick();
	    break;
	default:
	    resetNickCompletion();
	    break;
	}
    }

    public void keyReleased(KeyEvent ev) {
    }

    public void keyTyped(KeyEvent ev) {
    }
}
