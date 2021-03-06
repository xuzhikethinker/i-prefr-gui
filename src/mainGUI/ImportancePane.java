package mainGUI;


import guiElements.tuples.ImportanceTuple;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import dataStructures.AbstractDocument;
import dataStructures.Attribute;
import dataStructures.AttributeList;
import dataStructures.Importance;
import dataStructures.maps.AttributeMap;
import dataStructures.maps.ImportanceMap;

/**
 * The ImportancePane is a PreferencePane with fields for entry
 * of a stakeholder's importances.
 */
@SuppressWarnings("serial")
public class ImportancePane extends PreferencePane implements ActionListener{

	private AttributeMap attributeMap;
	private ImportanceMap map;
	private JPanel importancePanel;
	private JButton plusButton;
	
	private SpringLayout layout;
	private LinkedList<ImportanceTuple> tuples;
	
		/**
		 * Create new instance of ImportancePane
		 * @param parent
		 * @param document
		 */
		public ImportancePane(PreferenceReasoner reasoner, AbstractDocument document) {
			super(reasoner, document);
			this.attributeMap = document.getAttributeMap();
			this.map = new ImportanceMap();
			tuples = new LinkedList<ImportanceTuple>();
			createGUI();
		}
		
		private void createGUI(){
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			
			initializePreferencePanel();
			
			createFileControls();
			
			createNoMemberField();
			
			super.update();
			
			add(fileControls);
			add(Box.createRigidArea(new Dimension(10, 10)));
			add(preferencePanel);
			add(noMembers);
		}

		@Override
		public void update() {
			updatePreferencePanel();
			super.update();
		}
		

		@Override
		protected void initializePreferencePanel() {
			preferencePanel = new JPanel();
			preferencePanel.setLayout(new BoxLayout(preferencePanel, BoxLayout.Y_AXIS));
			
			JTextField columnName = new JTextField("Conditional Importance Expression");
			columnName.setMaximumSize(new Dimension(450, 20));
			columnName.setPreferredSize(new Dimension(450, 20));
			columnName.setAlignmentX(CENTER_ALIGNMENT);
			columnName.setEditable(false);
			preferencePanel.add(columnName);
			
			
			layout = new SpringLayout();
			importancePanel = new JPanel(layout);
			JScrollPane importanceScrollPane = new JScrollPane(importancePanel);
			importanceScrollPane.setBorder(null);
			
			updatePreferencePanel();
			
			preferencePanel.add(importanceScrollPane);
			plusButton = new JButton("+");
			plusButton.addActionListener(this);
			plusButton.setAlignmentX(CENTER_ALIGNMENT);
			preferencePanel.add(plusButton);
		}
		
		@Override
		protected void updatePreferencePanel() {
			importancePanel.removeAll();
			tuples.clear();
			
			//for every map entry, add a tuple to the table,then one more
			Collection<Entry<Integer, Importance>> set = map.entrySet();
			System.out.println("set size: "+set.size());
			Attribute[] allAttributes = getAttributes();
			for (Entry<Integer, Importance> p : set) {
				addTuple(p.getKey());
			}
			
			addTuple(null);
			
			reasoner.getFrame().pack();	
		}

		private void addTuple(Integer key) {
			ImportanceTuple tuple;
			if(key == null) {
				tuple = new ImportanceTuple(map, reasoner.getFrame(), importancePanel, this, getAttributes());
			} else {
				tuple = new ImportanceTuple(key,map,reasoner.getFrame(),importancePanel, this, getAttributes());
			}
			 
			tuples.add(tuple);
			importancePanel.add(tuple);
			
			int lastTupleIndex = tuples.indexOf(tuple) - 1;
			if(lastTupleIndex >= 0) {
				layout.putConstraint(SpringLayout.NORTH, tuple, 0,
						SpringLayout.SOUTH, tuples.get(lastTupleIndex));
				layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, tuple, 0,
						SpringLayout.HORIZONTAL_CENTER, tuples.get(lastTupleIndex));
			} else {
				layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, importancePanel, 0,
						SpringLayout.HORIZONTAL_CENTER, tuple);
			}
			
			int height = (int)tuple.getPreferredSize().getHeight();
			importancePanel.setPreferredSize(new Dimension(700, height*tuples.size()));
			
		}
		
		public void removeTuple(ImportanceTuple tuple) {
			int index = tuples.indexOf(tuple);
			
			if (index == 0 && tuples.size() > 1) {
				layout.putConstraint(SpringLayout.NORTH, tuples.get(index+1), 0,
						SpringLayout.NORTH, importancePanel);
				layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, tuples.get(index+1), 0,
						SpringLayout.HORIZONTAL_CENTER, importancePanel);
			} else if(index == tuples.size()-1){
				layout.removeLayoutComponent(tuple);
			} else if (index > 0 && index != tuples.size()-1) {
				layout.putConstraint(SpringLayout.NORTH, tuples.get(index+1), 0,
						SpringLayout.SOUTH, tuples.get(index-1));
				layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, tuples.get(index+1), 0,
						SpringLayout.HORIZONTAL_CENTER, tuples.get(index-1));
			}
			
			tuples.remove(tuple);
			int height = (int)tuple.getPreferredSize().getHeight();
			importancePanel.setPreferredSize(new Dimension(700, height*tuples.size()));
			repaint();
		}
		
		/**
		 * Resets the importance map.
		 */
		public void clearPreferenceData() {
			this.map = new ImportanceMap();
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (plusButton == e.getSource()) {
				addTuple(null);
				reasoner.getFrame().pack();
			} else {
				super.actionPerformed(e);
			}
		}
		
		/**
		 * Returns an array of all attributes in the AttributeMap
		 * @return array of attributes
		 */
		private Attribute[] getAttributes(){
			return (Attribute[])attributeMap.values().toArray(new Attribute[attributeMap.size()]);
		}

		@Override
		public boolean loadMemberPreferences(File file) {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
			Document doc=null;
			try {
				dBuilder = dbFactory.newDocumentBuilder();
				doc = dBuilder.parse(file);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				return false;
			} catch (SAXException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			
			// create the importanceMap
			Element importances = (Element) ((doc
					.getElementsByTagName("IMPORTANCES")).item(0));
			int uniqueID = Integer.parseInt(Util.getOnlyChildText(importances,
					"UNIQUEMAPID"));

			// populate it with importances
			NodeList importanceList = importances
					.getElementsByTagName("IMPORTANCE");
			int nImportances = importanceList.getLength();
			for (int i = 0; i < nImportances; i++) {

				// each importance has 4 lists
				Element importance = (Element) importanceList.item(i);
				int thisKey = Integer.parseInt(importance.getAttribute("ID"));
				NodeList listList = ((Element) ((importance
						.getElementsByTagName("LISTS")).item(0)))
						.getElementsByTagName("LIST");
				int nLists = listList.getLength();
				Importance thisValue = new Importance(thisKey);
				for (int j = 0; j < nLists; j++) {
					Element list = (Element) listList.item(j);
					int index = Integer.parseInt(Util.getOnlyChildText(list,
							"INDEX"));

					// and each list has 0 to many attribute keys
					AttributeList thisList = new AttributeList();
					NodeList attributeKeyList = list
							.getElementsByTagName("ATTRIBUTEKEY");
					int nAttributeKeys = attributeKeyList.getLength();
					for (int k = 0; k < nAttributeKeys; k++) {
						Element thisAttributeKey = (Element) attributeKeyList
								.item(k);
						Integer attributeKey = Integer.parseInt(thisAttributeKey
								.getTextContent());
						thisList.add(attributeMap.get(attributeKey));
					}
					thisValue.setList(index, thisList);
				}
				map.put(thisKey, thisValue);
			}
			
			// set loaded importance map as saved
			map.setSaved(true);
			
			return true;
		}

		@Override
		public boolean saveMemberPreferences(File preferenceFile) {
			Document doc = map.toXML();
			if(document == null) {
				return false;
			}
			
			// write xml to file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer;
			try {
				transformer = transformerFactory.newTransformer();
			} catch (TransformerConfigurationException e) {
				e.printStackTrace();
				return false;
			}
			
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(preferenceFile);
			
			try {
				transformer.transform(source,  result);
			} catch (TransformerException e) {
				e.printStackTrace();
				return false;
			}
			
			map.setSaved(true);
			
			// set role map as unsaved as preferences have been
			// added to a member, but not yet saved to the project
			document.getRoleMap().setSaved(false);
			
			return true;
		}

		@Override
		public boolean existUnsavedChanges() {
			return map.existUnsavedChanges();
		}

}
