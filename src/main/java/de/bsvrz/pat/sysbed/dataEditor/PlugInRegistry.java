/*
 * Copyright 2015 by Kappich Systemberatung Aachen
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

package de.bsvrz.pat.sysbed.dataEditor;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.pat.sysbed.dataEditor.plugins.*;
import de.bsvrz.pat.sysbed.main.JsonSerializer;
import de.kappich.sys.funclib.json.Json;

import javax.swing.*;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Datenbank für GTM-Plugins
 *
 * @author Kappich Systemberatung
 */
public class PlugInRegistry {

	private final ClientDavInterface _connection;
	
	private List<DataEditorPlugIn> _plugIns = new ArrayList<>();

	public PlugInRegistry(ClientDavInterface connection) {
		_connection = connection;
		addPlugIn(TextEditorPlugIn.class);
		addPlugIn(ComboBoxEditorPlugIn.class);
		addPlugIn(ReferenceEditorPlugIn.class);
		addPlugIn(DateEditorPlugIn.class);
		addPlugIn(TimeEditorPlugIn.class);
		addPlugIn(ArrayEditorPlugIn.class);
		addPlugIn(ListEditorPlugIn.class);
		addPlugIn(CheckBoxEditorPlugIn.class);
		
		Collections.sort(_plugIns);
	}

	private void addPlugIn(Class<?extends DataEditorPlugIn> clazz) {
		try {
			DataEditorPlugIn plugIn = clazz.getConstructor().newInstance();
			plugIn._connection = _connection;
			plugIn._registry = this;
			_plugIns.add(plugIn);
		}
		catch(InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public Collection<DataEditorPlugIn> getPlugIns(Data data){
		ArrayDeque<DataEditorPlugIn> result = new ArrayDeque<>();
		for(DataEditorPlugIn plugIn : _plugIns) {
			if(plugIn.supportsData(data)){
				result.addFirst(plugIn);
			}
		}
		return Collections.unmodifiableCollection(result);
	}

	public Box createBox(final Data data, final boolean editable) {
		return createBox(data, editable, Collections.emptyList());
	}

	public Box createBox(final Data data, final boolean editable, final List<JButton> contextPanel) {
		Collection<DataEditorPlugIn> plugIns = getPlugIns(data);
		DataEditorPlugIn next = plugIns.iterator().next();
		Box tmpBox = Box.createHorizontalBox();
		//tmpBox.setComponentPopupMenu(createPopupMenu(tmpBox, plugIns, data, editable, contextPanel)); TBD unfertig
		Box component = next.createComponent(data, editable, contextPanel);
		component.setInheritsPopupMenu(true);
		tmpBox.add(component);
		return tmpBox; 
	}

	private JPopupMenu createPopupMenu(final Box tmpBox, final Collection<DataEditorPlugIn> plugIns, final Data data, final boolean editable, final List<JButton> contextPanel) {
		JPopupMenu jPopupMenu = new JPopupMenu();

		final Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

		jPopupMenu.add(new AbstractAction("Kopieren") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				Object json = JsonSerializer.serializeData(data);
				systemClipboard.setContents(
						new StringSelection(Json.getInstance().writeObject(json)), (clipboard, contents) -> {}
				);
			}
		});
		JMenuItem insert = jPopupMenu.add(new AbstractAction("Einfügen") {
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					String clipData = (String) systemClipboard.getData(DataFlavor.stringFlavor);
					Object json = Json.getInstance().readObject(clipData);
					JsonSerializer.deserializeData(json, data);
					tmpBox.removeAll();
					Box component = plugIns.iterator().next().createComponent(data, editable, contextPanel);
					component.setInheritsPopupMenu(true);
					tmpBox.add(component);
					component.revalidate();
				}
				catch(Exception e1) {
					e1.printStackTrace();
					String message = e1.getMessage();
					if(message != null) {
						JOptionPane.showMessageDialog(tmpBox, message);
					}
				}
			}
		});
		insert.setEnabled(editable);
		if(plugIns.size()>1) {
			jPopupMenu.addSeparator();
			JMenu item = new JMenu("Anzeigen als");

			for(DataEditorPlugIn plugIn : plugIns) {
				item.add(new AbstractAction(plugIn.toString()) {
					@Override
					public void actionPerformed(final ActionEvent e) {
						tmpBox.removeAll();
						Box component = plugIn.createComponent(data, editable, contextPanel);
						component.setInheritsPopupMenu(true);
						tmpBox.add(component);
						component.revalidate();
					}
				});
				
				jPopupMenu.add(item);
			}
		}
		return jPopupMenu;
	}
}
