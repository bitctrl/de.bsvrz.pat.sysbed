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
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.pat.sysbed.dataEditor.ExtendedEditorPanel;
import de.bsvrz.pat.sysbed.preselection.panel.PreselectionDialog;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * GTM-Editor-Plugin, dass für Referenzwerte zuständig ist
 *
 * @author Kappich Systemberatung
 */
public class ReferenceEditorPlugIn extends TextEditorPlugIn {

	@Override
	public JTextField createValueBox(final Data data, final boolean editable, final Collection<JButton> additionalButtons, final JLabel suffixBox) {
		JTextField valueBox = super.createValueBox(data, editable, additionalButtons, suffixBox);
		if(editable) {
			ReferenceAttributeType att = (ReferenceAttributeType)data.getAttributeType();
			SystemObjectType objectType = att.getReferencedObjectType();
			final List<SystemObjectType> types = new LinkedList<SystemObjectType>();
			if(objectType == null) {
				DataModel configuration = _connection.getDataModel();
				types.add(configuration.getType("typ.konfigurationsObjekt"));
				types.add(configuration.getType("typ.dynamischesObjekt"));
			}
			else {
				types.add(objectType);
			}
			final JButton changeButton = new JButton(ExtendedEditorPanel._iconFolder);
			ExtendedEditorPanel.styleIconButton(changeButton);
			changeButton.setToolTipText("Referenz ändern");
			changeButton.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							PreselectionDialog dialog = new PreselectionDialog("Objektauswahl", changeButton, null, types);
							dialog.setMaximumSelectedObjects(1);
							dialog.setMaximumSelectedAspects(0);
							dialog.setMaximumSelectedAttributeGroups(0);
							try {
								dialog.setSelectedObject(data.asReferenceValue().getSystemObject());
							}
							catch(Exception ignore) {
							}
							if(dialog.show()) {
								data.asReferenceValue().setSystemObject(dialog.getSelectedObjects().get(0));
								textBoxAttributeModified(valueBox, suffixBox, data, false);
							}
						}
					}
			);
			additionalButtons.add(changeButton);
			if(att.isUndefinedAllowed()){
				final JButton removeButton = new JButton(ExtendedEditorPanel._iconRemove);
				ExtendedEditorPanel.styleIconButton(removeButton);
				removeButton.setToolTipText("Eintrag entfernen");
				removeButton.addActionListener(
						new ActionListener() {
							@Override
							public void actionPerformed(final ActionEvent e) {
								data.asReferenceValue().setSystemObject(null);
								textBoxAttributeModified(valueBox, suffixBox, data, false);
							}
						}
				);
				additionalButtons.add(removeButton);
			}
		}
		return valueBox;
	}

	@Override
	public boolean supportsData(final Data data) {
		return super.supportsData(data) && data.getAttributeType() instanceof ReferenceAttributeType;
	}

	@Override
	protected int getPriority() {
		return 10;
	}

	@Override
	public String toString() {
		return "Referenzwert";
	}
}
