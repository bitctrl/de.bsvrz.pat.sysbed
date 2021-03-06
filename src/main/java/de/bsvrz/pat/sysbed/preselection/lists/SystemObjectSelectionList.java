/*
 * Copyright 2014 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.pat.sysbed.
 * 
 * de.bsvrz.pat.sysbed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.pat.sysbed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.pat.sysbed.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.pat.sysbed.preselection.lists;

import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.pat.sysbed.main.TooltipAndContextUtil;
import de.bsvrz.sys.funclib.debug.Debug;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Klasse, die eine Gruppe zur Objektauswahl kapselt. Oben befindet sich eine Überschrift mit der Anzahl der ausgewählten Objekte
 * und einem Button zum deselektieren, darunter befindet sich die Liste der Objekte
 *
 * @author Kappich Systemberatung
 * @version $Revision: 000 $
 */
public class SystemObjectSelectionList extends Box {

	/** Icon für die Schalter zum Deselektieren */
	private final Icon _deselectIcon = new ImageIcon(PreselectionListsHandler.class.getResource("active-close-button.png"));

	private static final Debug _debug = Debug.getLogger();
	private final SystemObjectList _jlist;
	private final FilterTextField _filterTextField;
	private final MyListCellRenderer _cellRenderer;
	private List<? extends SystemObject> _preSelectedValues = Collections.emptyList();
	private final JLabel _numberOfSelectedObjects;
	private final JButton _deselectObjects;
	private List<? extends SystemObject> _objects = Collections.emptyList();
	private SystemObjectListRenderer _listRenderer;

	public SystemObjectSelectionList(final String header, String headerPlural) {
		super(BoxLayout.Y_AXIS);
		final Box headlineBox = Box.createHorizontalBox();
		// Label Anzahl der selektierten Objekte
		_numberOfSelectedObjects = new JLabel("0 / 0");
		_numberOfSelectedObjects.setBorder(new EtchedBorder());
		_numberOfSelectedObjects.setToolTipText("Anzahl der selektierten " + headerPlural);
		// Button deselektieren
		_deselectObjects = new JButton(_deselectIcon);
		_deselectObjects.setToolTipText("alle " + headerPlural + " deselektieren");
		_deselectObjects.setPreferredSize(new Dimension(20, 18));
		_deselectObjects.setEnabled(false);

		_filterTextField = new FilterTextField(header);

		_filterTextField.getDocument().addDocumentListener(
				new DocumentListener() {
					@Override
					public void insertUpdate(final DocumentEvent e) {
						updateFilter();
					}

					@Override
					public void removeUpdate(final DocumentEvent e) {
						updateFilter();
					}

					@Override
					public void changedUpdate(final DocumentEvent e) {
						updateFilter();
					}
				}
		);

		headlineBox.add(_filterTextField);
		headlineBox.add(Box.createRigidArea(new Dimension(5, 0)));
		headlineBox.add(Box.createHorizontalGlue());
		headlineBox.add(_numberOfSelectedObjects);
		headlineBox.add(Box.createRigidArea(new Dimension(5, 0)));
		headlineBox.add(_deselectObjects);
		// Liste der Objekte
		_jlist = new SystemObjectList();

		_deselectObjects.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						_jlist.clearSelection();
						_filterTextField.setText("");
					}
				}
		);

		_jlist.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		_jlist.addMouseMotionListener(
				new MouseMotionListener() {
					public void mouseDragged(MouseEvent e) {
					}

					public void mouseMoved(MouseEvent e) {
						int index = _jlist.locationToIndex(e.getPoint());
						if(index >= 0) {
							SystemObject object = _jlist.getModel().getElementAt(index);
							if(object != null) {
								try {
									SystemObject systemObject = object;
									String tooltip = TooltipAndContextUtil.getTooltip(systemObject);
									_jlist.setToolTipText(tooltip);
								}
								catch(Exception ex) {
									_debug.fine("Tooltip kann nicht angezeigt werden.");
									_debug.fine(ex.toString());
								}
							}
							else {
								_jlist.setToolTipText(null);
							}
						}
						else {
							_jlist.setToolTipText(null);
						}
					}
				}
		);
		_jlist.addKeyListener(
				new KeyListener() {
					public void keyPressed(KeyEvent e) {
					}

					public void keyReleased(KeyEvent e) {
					}

					public void keyTyped(KeyEvent e) {
						_jlist.ensureIndexIsVisible(_jlist.getSelectedIndex());
					}
				}
		);

		_jlist.addListSelectionListener(new ListSelectionListener() {
			                                @Override
			                                public void valueChanged(final ListSelectionEvent e) {
				                                if(!e.getValueIsAdjusting()) {
					                                updateHeader();
					                                _preSelectedValues = _jlist.getSelectedObjects();
				                                }
			                                }
		                                });

		_cellRenderer = new MyListCellRenderer();
		_jlist.setCellRenderer(_cellRenderer);

		add(headlineBox);
		add(Box.createRigidArea(new Dimension(0, 3)));
		add(new JScrollPane(_jlist));
		setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		setMinimumSize(new Dimension(0, 0));
	}

	public SystemObjectListRenderer getListRenderer() {
		return _listRenderer;
	}

	public void setListRenderer(final SystemObjectListRenderer listRenderer) {
		_listRenderer = listRenderer;
	}

	private void updateFilter() {
		setElements(_objects);
	}

	public void updateHeader() {
		int[] selectedIndices = _jlist.getSelectedIndices();
		_numberOfSelectedObjects.setText(
				selectedIndices.length + " / " + _jlist.getModel()
						.getSize()
		);
		_deselectObjects.setEnabled(selectedIndices.length > 0 || _filterTextField.getText().length() > 0);
	}

	/**
	 * Liefert alle sichtbaren selektierten Systemobekte zurück
	 * @return
	 */
	public List<SystemObject> getSelectedValues() {
		List list = Arrays.asList(_jlist.getSelectedValues());
		return list;
	}

	/**
	 * Liefert alle selektierten Systemobejkte zurück, auch solche, die durch einen aktiven Filter aktuell nicht sichtbar sind 
	 * @return Liste mit Systemobjekten
	 */
	public List<? extends SystemObject> getPreSelectedValues() {
		return Collections.unmodifiableList(_preSelectedValues);
	}

	public void addListSelectionListener(final ListSelectionListener listSelectionListener) {
		_jlist.addListSelectionListener(listSelectionListener);
	}

	public void clearSelection() {
		_jlist.clearSelection();
	}

	public void selectElements(final List<? extends SystemObject> objects) {
		ListSelectionModel selectionModel = _jlist.getSelectionModel();
		selectionModel.setValueIsAdjusting(true);
		selectionModel.clearSelection();
		DefaultListModel defaultListModel = (DefaultListModel)_jlist.getModel();
		for(int i = 0; i < objects.size(); i++) {
			Object object = objects.get(i);
			int position = defaultListModel.indexOf(object);
			if(position >= 0) {
				_jlist.addSelectionInterval(position, position);
			}
		}
		selectionModel.setValueIsAdjusting(false);
		_jlist.ensureIndexIsVisible(_jlist.getSelectedIndex());
		_preSelectedValues = objects;
	}

	public void setElements(final List<? extends SystemObject> objects) {
		_objects = objects;
		DefaultListModel<SystemObject> defaultListModel = makeListModel(_objects);
		List<? extends SystemObject> preSelectedValues = _preSelectedValues;
		_jlist.setPrototypeCellValue(computeLongestPid(defaultListModel));
		_jlist.setModel(defaultListModel);
		selectElements(preSelectedValues);
		updateHeader();
	}

	/**
	 * Gibt das Objekt mit der Pid zurück, die die breiteste Darstellung hat (um die Länge der horizontalen Scrollbar zu berechnen)
	 * @param model
	 * @return
	 */
	private SystemObject computeLongestPid(final DefaultListModel<SystemObject> model) {
		DefaultListCellRenderer renderer = new DefaultListCellRenderer(){
			@Override
			public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
				SystemObject obj = (SystemObject) value;
				Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if(_listRenderer != null) {
					setText(_listRenderer.getText(obj));
					setIcon(_listRenderer.getIcon(obj));
				}
				return component;
			}
		};
		int maxWidth = -1;
		int maxIndex = -1;
		for (int i = 0; i < model.size(); i++) {
			Component component = renderer.getListCellRendererComponent(_jlist, model.get(i), i, false, false);
			int width = component.getPreferredSize().width;
			if(width > maxWidth){
				maxWidth = width;
				maxIndex = i;
			}
		}
		if(maxIndex != -1){
			return model.get(maxIndex);
		}
		return null;
	}

	/**
	 * Erzeugt aus einer Liste von Objekten ein DefaultListModel zum Anzeigen der Objekte in einer JList.
	 *
	 * @param list Liste, die in einer JList angezeigt werden sollen
	 * @return DefaultListModel, welches in einer JList angezeigt werden kann
	 */
	private DefaultListModel<SystemObject> makeListModel(List<? extends SystemObject> list) {
		DefaultListModel<SystemObject> dlm = new DefaultListModel<>();
		String filter = _filterTextField.getText();
		final Pattern pattern;
		final long id;
		if(!filter.isEmpty()) {
			Pattern tmpPattern = null;
			try {
				if(filter.length() > 2 && filter.startsWith("/") && filter.endsWith("/")) {
					tmpPattern = Pattern.compile(filter.substring(1, filter.length() - 1), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
				}
				else {
					tmpPattern = Pattern.compile(filter, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.LITERAL);
				}
				_filterTextField.setToolTipText(null);
				_filterTextField.setForeground(null);
			}
			catch(PatternSyntaxException e) {
				_filterTextField.setToolTipText(e.getMessage());
				_filterTextField.setForeground(Color.red);
			}
			long tmpId = 0;
			try {
				tmpId = Long.parseLong(filter);
			}
			catch(NumberFormatException ignored) {
			}
			
			pattern = tmpPattern;
			id = tmpId;
		}
		else {
			pattern = null;
			id = 0;
		}
		_cellRenderer.setPattern(pattern);
		if(pattern != null || id != 0) {
			list.parallelStream().filter(it -> matches(pattern, id, it)).forEachOrdered(dlm::addElement);
		}
		else {
			dlm.ensureCapacity(list.size());
			list.forEach(dlm::addElement);
		}
		return dlm;
	}

	private static boolean matches(final Pattern pattern, final long id, final SystemObject object) {
		return pattern == null
				|| pattern.matcher(object.getPid()).find()
				|| pattern.matcher(object.getName()).find()
				|| id == object.getId();
	}

	public List<? extends SystemObject> getElements() {
		return Collections.unmodifiableList(_objects);
	}

	public void setSelectionMode(final int selectionMode) {
		_jlist.setSelectionMode(selectionMode);
	}

	private class MyListCellRenderer extends DefaultListCellRenderer {
		private Pattern _pattern;

		@Override
		public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
			SystemObject obj = (SystemObject) value;
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if(_listRenderer != null) {
				setText(_listRenderer.getText(obj));
				setIcon(_listRenderer.getIcon(obj));
			}
			return this;
		}

		public void setPattern(final Pattern pattern) {
			_pattern = pattern;
		}

		public Pattern getPattern() {
			return _pattern;
		}

		@Override
		public void setText(final String text) {
			super.setText(generateHtml(_pattern, text));
		}

		private String generateHtml(final Pattern pattern, final String text) {
			if(pattern == null) return text;
			StringBuilder builder = new StringBuilder(text.length() + 32);
			Matcher matcher = pattern.matcher(text);
			builder.append("<html>");
			builder.append(matcher.replaceFirst("<font color=\"red\">$0</font>"));
			return builder.toString();
		}
	}
}
