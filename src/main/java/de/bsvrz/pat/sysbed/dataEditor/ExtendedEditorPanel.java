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
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.*;
import de.bsvrz.sys.funclib.debug.Debug;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Iterator;

/**
 * Diese Klasse stellt einen Datensatz in strukturierter Form (entsprechend der Attributgruppe) in einem JPanel dar. Die einzelnen Attribute sind entsprechend
 * der Einschränkungen des Datenmodells editierbar. Bei Bedarf kann auch eine nicht editierbar Form gewählt werden.
 *
 * @author Kappich Systemberatung
 * @version $Revision$
 * @see #setData(Data)
 * @see #setResultData(ResultData)
 */
public class ExtendedEditorPanel extends AbstractEditorPanel {

	/** ein Zahlenformat */
	private static final NumberFormat _integerNumberFormat = NumberFormat.getNumberInstance();

	/** Genauigkeit des Zahlenformats */
	private static final NumberFormat _precisionTestNumberFormat;

	/**
	 * Das Zahlenformat erhält eine Formatierung.
	 */
	public static final ImageIcon _iconAdd;

	public static final ImageIcon _iconFolder;

	public static final ImageIcon _iconAddFolder;

	public static final ImageIcon _iconRemove;

	public static final ImageIcon _iconCopy;

	static {
		_integerNumberFormat.setMinimumIntegerDigits(1);
		_integerNumberFormat.setMaximumIntegerDigits(999);
		_integerNumberFormat.setGroupingUsed(false);
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator(',');
		_precisionTestNumberFormat = new DecimalFormat("0.#", symbols);
		_precisionTestNumberFormat.setMaximumFractionDigits(999);

		_iconAdd = new ImageIcon(ExtendedEditorPanel.class.getResource("add.png"));
		_iconFolder = new ImageIcon(ExtendedEditorPanel.class.getResource("folder.png"));
		_iconAddFolder = new ImageIcon(ExtendedEditorPanel.class.getResource("addFolder.png"));
		_iconRemove = new ImageIcon(ExtendedEditorPanel.class.getResource("remove.png"));
		_iconCopy = new ImageIcon(ExtendedEditorPanel.class.getResource("copy.png"));
	}


	/** der Debug-Logger */
	private final Debug _debug = Debug.getLogger();

	private final PlugInRegistry _plugInRegistry;

	/** speichert die aktuellen Daten */
	private Data _data;

	/** Grafische Komponente zum Darstellen der Daten */
	private final JPanel _dataPane;

	/** gibt an, ob die dargestellten Felder editierbar sein sollen */
	private boolean _editable = true;

	private boolean _overrideComplexityWarning = false;

	/* #################### public Methoden #################### */

	/**
	 * Der Konstruktor nimmt die aktuelle Verbindung zum Datenverteiler entgegen und stellt initial einen leeren Datensatz dar.
	 *
	 * @param connection Verbindung zum Datenverteiler
	 */
	public ExtendedEditorPanel(final ClientDavInterface connection) {
		_dataPane = new JPanel(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(_dataPane);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);

		_plugInRegistry = new PlugInRegistry(connection);
	}

	/**
	 * Diese Methode erstellt generisch eine Ansicht der Daten.
	 *
	 * @param data darzustellende Daten
	 */
	@Override
	public void setData(final Data data) {
		_debug.finer("data" , data);
		_data = data;
		_dataPane.removeAll();
		if(_data != null) {
			_debug.finer("data" , _data);
			showData();
		}
		else {
			_dataPane.add(new JLabel("keine Daten"), BorderLayout.NORTH);
		}
		_dataPane.revalidate();
		_dataPane.repaint();
	}

	/**
	 * Diese Methode erstellt generisch eine Ansicht der Daten.
	 *
	 * @param resultData Ergebnisdatensatz, welcher die darzustellenden Daten enthält
	 */
	@Override
	public void setResultData(final ResultData resultData) {
		final Data data = resultData.getData();
		_data = (data == null ? null : data.createModifiableCopy());
		_dataPane.removeAll();
		if(_data != null) {
			_debug.finer("data" , _data);
			showData();
		}
		else {
			String label = resultData.getDataState().toString();
			_dataPane.add(new JLabel(label), BorderLayout.NORTH);
		}
		_dataPane.revalidate();
		_dataPane.repaint();
	}

	private void showData() {
		int dataComplexity = getDataComplexity(_data);
		_debug.info("DataComplex", dataComplexity);
		if(!_overrideComplexityWarning && dataComplexity > 1000){
			_dataPane.add(createComplexityWarningPanel(), BorderLayout.NORTH);
			return;
		}
		Box box = createBox(_data);
		_dataPane.add(box, BorderLayout.NORTH);
	}

	private Component createComplexityWarningPanel() {
		JPanel jPanel = new JPanel();
		jPanel.add(new JLabel("Der Datensatz ist sehr komplex und kann zu Problemen bei der Darstellung führen."));
		JButton button = new JButton("Trotzdem anzeigen");
		jPanel.add(button);
		jPanel.setPreferredSize(new Dimension(400, 400));
		button.addActionListener(new ActionListener() {
			                         @Override
			                         public void actionPerformed(final ActionEvent e) {
				                         _overrideComplexityWarning = true;
				                         _dataPane.removeAll();
				                         showData();
				                         _dataPane.revalidate();
				                         _dataPane.repaint();
			                         }
		                         });
		return jPanel;
	}

	private static int getDataComplexity(final Data data) {
		if(data.isPlain()){
			return 1;
		}
		int num = 1;
		for(Data subData : data) {
			num += getDataComplexity(subData);
		}
		return num;
	}

	/**
	 * Hierüber kann bestimmt werden, ob die angezeigten Textfelder, etc. editierbar sind, oder nicht.
	 *
	 * @param editable gibt an, ob die angezeigten Komponenten editierbar sein sollen
	 */
	@Override
	public void setEditable(final boolean editable) {
		_editable = editable;
	}

	/**
	 * Gibt die Daten zurück, die aktuell angezeigt werden.
	 *
	 * @return die aktuellen Daten
	 */
	@Override
	public Data getData() {
		return _data;
	}

	/* ################ Private Methoden ################# */

	/**
	 * An dieser Stelle wird eine Komponente generisch zusammengestellt, die die übergebenen Daten darstellt.
	 *
	 * @param data die darzustellenden Daten
	 *
	 * @return die Daten darstellende Komponente
	 */
	private Box createBox(final Data data) {
		return _plugInRegistry.createBox(data, _editable);
	}


	public static void styleIconButton(JButton button){
		button.setPreferredSize(new Dimension(22,22));
		//button.setBorderPainted(true);
		//button.setBackground(new Color(0, true));
		button.setContentAreaFilled(false);
	}

	/**
	 * 	Kopiert die Inhalte von einem Data-Objekt zu einem anderen.
	 *
	 * @param from Quelle
	 * @param to Ziel
	 */
	public static void copyData(final Data from, final Data to) {
		if(from.isPlain()) {
			if(from.getAttributeType() instanceof IntegerAttributeType) {
				to.asUnscaledValue().set(from.asUnscaledValue().longValue());
			}
			else if(from.getAttributeType() instanceof DoubleAttributeType) {
				to.asUnscaledValue().set(from.asUnscaledValue().doubleValue());
			}
			else if(from.getAttributeType() instanceof TimeAttributeType) {
				to.asTimeValue().setMillis(from.asTimeValue().getMillis());
			}
			else if(from.getAttributeType() instanceof ReferenceAttributeType) {
				to.asReferenceValue().setSystemObject(from.asReferenceValue().getSystemObject());
			}
			else {
				to.asTextValue().setText(from.asTextValue().getText());
			}
		}
		else if(from.isArray()) {
			final Data.Array toArray = to.asArray();
			final Data.Array fromArray = from.asArray();
			toArray.setLength(fromArray.getLength());
			for(int i = 0; i < toArray.getLength(); i++) {
				copyData(fromArray.getItem(i), toArray.getItem(i));
			}
		}
		else if(from.isList()) {
			Iterator toIterator = to.iterator();
			Iterator fromIterator = from.iterator();
			while(toIterator.hasNext() && fromIterator.hasNext()) {
				copyData((Data)fromIterator.next(), (Data)toIterator.next());
			}
		}
	}

	public static String getScaledValueText(final long unscaledValue, double conversionFactor) {
		
		if(conversionFactor == 1) {
			return String.valueOf(unscaledValue);
		}
		else {
			int precision = 0;
			synchronized(_integerNumberFormat) {
				String formatted = _precisionTestNumberFormat.format(conversionFactor);
				int kommaPosition = formatted.lastIndexOf(',');
				if(kommaPosition >= 0) precision = formatted.length() - kommaPosition - 1;
				_integerNumberFormat.setMinimumFractionDigits(precision);
				_integerNumberFormat.setMaximumFractionDigits(precision);
				return _integerNumberFormat.format(unscaledValue * conversionFactor);
			}
		}
	}
}
