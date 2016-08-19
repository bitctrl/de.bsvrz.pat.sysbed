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

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.config.AttributeType;
import de.bsvrz.dav.daf.main.config.IntegerAttributeType;
import de.bsvrz.pat.sysbed.dataEditor.PlugInRegistry;
import de.bsvrz.sys.funclib.debug.Debug;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import java.awt.Color;
import java.util.Collection;
import java.util.List;

/**
 * Abstrakte Klasse für GTM-Editor-Plugins, die ein Data-Objekt visualisieren und editierbar machen.
 *
 * @author Kappich Systemberatung
 */
public abstract class DataEditorPlugIn implements Comparable<DataEditorPlugIn> {

	/** Hintergrundfarbe eines Attributwertes, der nicht definiert ist und nicht über den Datenverteiler verschickt werden kann. */
	public static final Color _backgroundUndefinedValue = new Color(Color.HSBtoRGB(0f, 0.75f, 1.0f));

	/** String der ausgegeben wird, wenn der Attributwert "undefiniert" ist. Der String entspricht dabei nicht dem wahren undefiniert Wert. */
	public static final String _undefinedString = "_Undefiniert_";

	/** String, der in Comboboxen angezeigt wird und anzeigt, dass der Default-Wert benutzt werden soll. */
	public static final String _defaultValueString = "Default-Wert";
	
	protected static final Debug _debug = Debug.getLogger();

	/**
	 * Diese Methode erstellt aus dem übergebenen Data-Objekt eine Komponente zur Darstellung in der Oberfläche
	 * @param data Datum
	 * @param editable Ist das Data-Objekt editierbar?
	 * @param additionalButtons Zusätzliche Buttons die im Element eingefügt werden sollen (z.B. bei Array-Elementen die Buttons zum Verdoppeln und Löschen)
	 * @return Box, die das Element darstellt.
	 */
	public abstract Box createComponent(Data data, final boolean editable, final List<JButton> additionalButtons);

	/** 
	 * Gibt <tt>true</tt> zurück, wenn dieses PlugIn das übergebene Data-Objekt unterstützt. Ein PlugIn, was Array-Daten darstellt wird bspw. keinen Plain-Textwert unterstützen.
	 * @param data Datum
	 * @return <tt>true</tt>, wenn dieses PlugIn das übergebene Data-Objekt unterstützt, sonst <tt>false</tt>
	 */
	public abstract boolean supportsData(Data data);

	/**
	 * Erstellt eine Komponente, die nur den Inhalt dieses Datums erhält (ohne Überschrift und Beschriftung)
	 * @param data Datum
	 * @param editable Soll die Komponente editierbar sein?
	 * @param additionalButtons Liste, in die zusätzliche Buttons eingefügt werden
	 * @param suffixBox Optional: Ein JLabel, dass den Suffix (also z.B. die Einheit) des Datums übergeben bekommt (oder null)
	 * @return Komponente für Inhalt
	 */
	public abstract JComponent createValueBox(final Data data, final boolean editable, final Collection<JButton> additionalButtons, final JLabel suffixBox);

	/** 
	 * Gibt die Priorität zurück. Wenn mehrere PlugIns ein Datum unterstützen wird standardmäßig das PlugIn mit der höchsten Priorität ausgewählt.
	 * @return die Priorität
	 */
	protected abstract int getPriority();

	/**
	 * Datenverteilerverbindung, sollte nur gelesen werden.
	 */
	public ClientDavInterface _connection;

	/**
	 * PlugIn-Verwaltung (zum erzeugen von Kind-PlugIns)
	 */
	public PlugInRegistry _registry;

	@Override
	public int compareTo(final DataEditorPlugIn o) {
		return Integer.compare(getPriority(), o.getPriority());
	}

	@Override
	public String toString() {
		return getClass().getName();
	}

	public static boolean isByteAttributeType(final AttributeType attributeType) {
		return attributeType instanceof IntegerAttributeType && (attributeType.getPid().equals("att.byte") || attributeType.getPid().equals("att.tlsByte"));
	}

	public static byte[] getByteArray(final Data data) {
		Data.NumberArray array = data.asUnscaledArray();
		byte[] bytes = new byte[array.getLength()];
		for(int i = 0; i < bytes.length; i++){
			bytes[i] = (byte) array.getValue(i).intValue();
		}
		return bytes;
	}
}
