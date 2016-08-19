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

package de.bsvrz.pat.sysbed.dataEditor.plugins;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.DataModel;
import de.bsvrz.dav.daf.main.config.ReferenceAttributeType;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.pat.sysbed.dataEditor.ExtendedEditorPanel;
import de.bsvrz.pat.sysbed.preselection.panel.PreselectionDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * GTM-Editor-Plugin, dass für Arrays zuständig ist
 *
 * @author Kappich Systemberatung
 */
public class ArrayEditorPlugIn extends DataEditorPlugIn {

	@Override
	public Box createComponent(final Data data, final boolean editable, final List<JButton> additionalButtons) {
		final Box box;
		box = createValueBox(data, editable, additionalButtons, null);
		box.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(data.getName() + ": "), new EmptyBorder(0, 5, 0, 5)));
		return box;
	}

	@Override
	public Box createValueBox(final Data data, final boolean editable, final Collection<JButton> additionalButtons, final JLabel suffixBox) {
		final Box box;
		box = Box.createVerticalBox();
		createArrayComponent(data, box, editable);
		if(!additionalButtons.isEmpty()) {
			Box horizontalBox = Box.createHorizontalBox();
			horizontalBox.add(Box.createHorizontalGlue());
			for(JButton additionalButton : additionalButtons) {
				horizontalBox.add(Box.createHorizontalStrut(5));
				horizontalBox.add(additionalButton);
			}
			box.add(horizontalBox);
		}
		return box;
	}


	private void createArrayComponent(final Data data, final Box box, final boolean editable) {
		final JPanel arrayHeaderBox = new JPanel(new FlowLayout(FlowLayout.LEFT));
		arrayHeaderBox.add(new JLabel("Arraygröße:"));
		arrayHeaderBox.setOpaque(false);
		final Data.Array array = data.asArray();
		if(array.isCountVariable() && editable) {
			final JButton addEntryButton = new JButton(ExtendedEditorPanel._iconAdd);
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel(array.getLength(), 0, 9999, 1);
			if(array.isCountLimited()) spinnerModel.setMaximum(array.getMaxCount());
			final JSpinner arraySizeBox = new JSpinner(spinnerModel);
			arraySizeBox.addChangeListener(
					new ChangeListener() {
						public void stateChanged(ChangeEvent e) {
							setArrayLength(box, data.asArray(), ((Number)arraySizeBox.getValue()).intValue());
							box.removeAll();
							box.add(arrayHeaderBox);
							for(int i = 0; i < array.getLength(); i++) {
								Data d = array.getItem(i);
								box.add(createBoxWithArrayButtons(d, i, array, arraySizeBox, editable));
							}
							box.revalidate();
							box.repaint();
							addEntryButton.setEnabled(!array.isCountLimited() || array.getMaxCount() > array.getLength());
						}
					}
			);
			arrayHeaderBox.add(arraySizeBox);
			addEntryButton.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							setArrayLength(box, array, array.getLength()+1);
							for(int i = array.getLength() - 2; i >= 0; i--) {
								ExtendedEditorPanel.copyData(array.getItem(i), array.getItem(i+1));
							}
							array.getItem(0).setToDefault();
							arraySizeBox.setValue(((Number)arraySizeBox.getValue()).intValue() + 1);
						}
					});

			ExtendedEditorPanel.styleIconButton(addEntryButton);
			arrayHeaderBox.add(addEntryButton);
			if(data.getAttributeType() instanceof ReferenceAttributeType) {
				final JButton addMultipleEntriesButton = new JButton(ExtendedEditorPanel._iconAddFolder);
				addMultipleEntriesButton.setToolTipText("Objekte hinzufügen");
				ReferenceAttributeType att = (ReferenceAttributeType)data.getAttributeType();
				SystemObjectType objectType = att.getReferencedObjectType();
				final List<SystemObjectType> types = new LinkedList<SystemObjectType>();
				final String title;
				if(objectType == null) {
					DataModel configuration = _connection.getDataModel();
					types.add(configuration.getType("typ.konfigurationsObjekt"));
					types.add(configuration.getType("typ.dynamischesObjekt"));
					title = "Beliebige Objekte hinzufügen";
				}
				else {
					types.add(objectType);
					title = "Objekte vom Typ " + objectType.getNameOrPidOrId() + " hinzufügen";
				}
				addMultipleEntriesButton.addActionListener(
						new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								PreselectionDialog dialog = new PreselectionDialog(title, addMultipleEntriesButton, null, types);
								dialog.setMaximumSelectedAspects(0);
								dialog.setMaximumSelectedAttributeGroups(0);
								if(array.isCountLimited()) {
									final int objectsToChoose = array.getMaxCount() - array.getLength();
									if(objectsToChoose <= 0)
									{
										JOptionPane.showMessageDialog(addMultipleEntriesButton, "Das Array kann keine zusätzlichen Objekte mehr aufnehmen.");
										return;
									}
									dialog.setMaximumSelectedObjects(objectsToChoose);
								}
								if(dialog.show()) {

									final List<SystemObject> objects = dialog.getSelectedObjects();

									final int oldLength = array.getLength();
									setArrayLength(box, array, oldLength + objects.size());

									//Objekte nach hinten verschieben
									for(int i = array.getLength() - 1 - objects.size(); i >= 0; i--) {
										ExtendedEditorPanel.copyData(array.getItem(i), array.getItem(i + objects.size()));
									}


									for(int i = 0, objectsSize = objects.size(); i < objectsSize; i++) {
										final SystemObject object = objects.get(i);
										array.asReferenceArray().getReferenceValue(i).setSystemObject(object);
									}
									arraySizeBox.setValue(array.getLength());
								}
							}
						}
				);
				ExtendedEditorPanel.styleIconButton(addMultipleEntriesButton);
				arrayHeaderBox.add(addMultipleEntriesButton);
			}

			arrayHeaderBox.add(Box.createHorizontalGlue());
			box.add(arrayHeaderBox);
			final Data.Array arr = data.asArray();
			for(int i = 0; i < arr.getLength(); i++) {
				Data d = arr.getItem(i);
				box.add(createBoxWithArrayButtons(d, i, arr, arraySizeBox, editable));
			}
		}
		else {
			if(!array.isCountVariable()) {
				setArrayLength(box, array, array.getMaxCount());
			}
			arrayHeaderBox.add(new JLabel(String.valueOf(array.getLength())));
			arrayHeaderBox.add(Box.createHorizontalGlue());
			box.add(arrayHeaderBox);
			final Data.Array arr = data.asArray();
			for(int i = 0; i < arr.getLength(); i++) {
				Data d = arr.getItem(i);
				box.add(_registry.createBox(d, editable));
			}
		}
	}

	/**
	 * Erstellt eine Box für Daten in einem Array, bei denen zusätzlich Buttons für Kopieren, Löschen, einfügen usw. vorhanden sind
	 * @param data Daten-Objekt für das die Box erstellt werden soll
	 * @param index Index im Array
	 * @param array Array
	 * @param scrollbox Steuerelement, das für die Arrayeinträge zuständig ist
	 * @param editable
	 * @return Die erstellte Box
	 */
	private Box createBoxWithArrayButtons(final Data data, final int index, final Data.Array array, final JSpinner scrollbox, final boolean editable) {
		final Box box;

		List<JButton> buttons = new ArrayList<>();

		// Button zum einfügen von Elementen
		final JButton insertButton = new JButton(ExtendedEditorPanel._iconAdd);
		ExtendedEditorPanel.styleIconButton(insertButton);
		insertButton.setToolTipText("Eintrag einfügen");
		insertButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setArrayLength(scrollbox, array, array.getLength()+1);
						for(int i = array.getLength() - 2; i > index; i--) {
							ExtendedEditorPanel.copyData(array.getItem(i), array.getItem(i+1));
						}
						array.getItem(index+1).setToDefault();
						scrollbox.setValue(((Number)scrollbox.getValue()).intValue() + 1);
					}
				}
		);
		buttons.add(insertButton);

		// Button zum Einfügen von mehreren (Referenz-)Elementen
		if(data.getAttributeType() instanceof ReferenceAttributeType) {
			final JButton insertMultipleButton = new JButton(ExtendedEditorPanel._iconAddFolder);
			ExtendedEditorPanel.styleIconButton(insertMultipleButton);
			insertMultipleButton.setToolTipText("Objekte hinzufügen");
			insertMultipleButton.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							ReferenceAttributeType att = (ReferenceAttributeType)data.getAttributeType();
							SystemObjectType objectType = att.getReferencedObjectType();
							final List<SystemObjectType> types = new LinkedList<SystemObjectType>();
							final String title;
							if(objectType == null) {
								DataModel configuration = _connection.getDataModel();
								types.add(configuration.getType("typ.konfigurationsObjekt"));
								types.add(configuration.getType("typ.dynamischesObjekt"));
								title = "Beliebige Objekte hinzufügen";
							}
							else {
								types.add(objectType);
								title = "Objekte vom Typ " + objectType.getNameOrPidOrId() + " hinzufügen";
							}
							PreselectionDialog dialog = new PreselectionDialog(title, insertMultipleButton, null, types);
							dialog.setMaximumSelectedAspects(0);
							dialog.setMaximumSelectedAttributeGroups(0);
							if(array.isCountLimited()) {
								final int objectsToChoose = array.getMaxCount() - array.getLength();
								if(objectsToChoose <= 0) {
									JOptionPane.showMessageDialog(insertMultipleButton, "Das Array kann keine zusätzlichen Objekte mehr aufnehmen.");
									return;
								}
								dialog.setMaximumSelectedObjects(objectsToChoose);
							}
							if(dialog.show()) {

								final List<SystemObject> objects = dialog.getSelectedObjects();

								final int oldLength = array.getLength();
								setArrayLength(scrollbox, array, oldLength + objects.size());

								//Objekte nach hinten verschieben
								for(int i = array.getLength() - 1 - objects.size(); i > index; i--) {
									ExtendedEditorPanel.copyData(array.getItem(i), array.getItem(i + objects.size()));
								}


								for(int i = 0, objectsSize = objects.size(); i < objectsSize; i++) {
									final SystemObject object = objects.get(i);
									final int newPosition = i + index + 1;
									array.asReferenceArray().getReferenceValue(newPosition).setSystemObject(object);
								}
								scrollbox.setValue(array.getLength());
							}
						}
					}
			);
			buttons.add(insertMultipleButton);
		}

		// Button zum Klonen von Einträgen
		final JButton cloneButton = new JButton(ExtendedEditorPanel._iconCopy);
		ExtendedEditorPanel.styleIconButton(cloneButton);
		cloneButton.setToolTipText("Eintrag duplizieren");
		cloneButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setArrayLength(scrollbox, array, array.getLength()+1);
						for(int i = array.getLength() - 2; i >= index; i--) {
							ExtendedEditorPanel.copyData(array.getItem(i), array.getItem(i+1));
						}
						scrollbox.setValue(((Number)scrollbox.getValue()).intValue() + 1);
					}
				}
		);
		buttons.add(cloneButton);

		//Button zum Löschen von Einträgen
		final JButton removeButton = new JButton(ExtendedEditorPanel._iconRemove);
		ExtendedEditorPanel.styleIconButton(removeButton);
		removeButton.setToolTipText("Eintrag entfernen");
		removeButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						for(int i = index; i < array.getLength() - 1; i++) {
							ExtendedEditorPanel.copyData(array.getItem(i+1), array.getItem(i));
						}
						scrollbox.setValue(((Number)scrollbox.getValue()).intValue() - 1);
					}
				}
		);
		buttons.add(removeButton);
		box = _registry.createBox(data, editable, buttons);
		return box;
	}

	/**
	 * Hilfsmethode zum setzen einer Arraylänge. Damit keine unschönen Runtime-Exceptions entstehen wird hier der Bereich noch einmal geprüft.
	 * @param component Box
	 * @param array Array
	 * @param newLength neue Länge
	 */
	private void setArrayLength(final JComponent component, final Data.Array array, final int newLength) {
		if(array.isCountVariable() && array.getMaxCount() > 0){
			if(array.getMaxCount() < newLength || newLength < 0){
				JOptionPane.showMessageDialog(component, "Array-Länge " + newLength + " außerhalb des gültigen Bereichs: 0 - " + array.getMaxCount());

				
			}
		}
		array.setLength(newLength);
	}

	@Override
	public boolean supportsData(final Data data) {
		return data.isArray();
	}

	@Override
	public String toString() {
		return "Array";
	}

	@Override
	protected int getPriority() {
		return 0;
	}
}
