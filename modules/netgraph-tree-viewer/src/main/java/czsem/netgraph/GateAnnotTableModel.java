package czsem.netgraph;

import gate.Annotation;
import gate.Document;
import gate.FeatureMap;
import gate.Utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.table.AbstractTableModel;

import czsem.netgraph.treesource.TreeIndexTreeSource;

public class GateAnnotTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -1999028584101610952L;
	
	public static class ATTR {
		//TODO use GateAnnotationsNodeAttributesExtended

		public static final String STRING = "_string";
		public static final String TYPE = "_type";
		public static final String STRAT = "_start";
		public static final String END = "_end";
		public static final String ID = "_id";
		
	}
	
	public static final String [] DEFAULT_ATTRS = {
		ATTR.STRING,
		ATTR.TYPE,
		ATTR.STRAT,
		ATTR.END,
		ATTR.ID,
	};
	

	protected TreeIndexTreeSource treeSource;
	protected Object[] lastSortedKeys = new Object[0];
	protected Annotation lastSelectedAnnot;
	
	public GateAnnotTableModel(TreeIndexTreeSource treeSource) {
		this.treeSource = treeSource;
		treeSource.getSelectionChangeListeners().add(x -> fireTableDataChanged());
	}


	public static Object getAnnotationAttr(Document d, Annotation a, Object attr) {
		if (a == null) return null;
		
		FeatureMap fm = a.getFeatures();
		if (fm.containsKey(attr)) return fm.get(attr);
		
		String str = attr.toString();
		
		//TODO use GateAnnotationsNodeAttributesExtended
		switch (str) {
		case ATTR.STRING:	return Utils.stringFor(d, a);
		case ATTR.TYPE:		return a.getType();
		case ATTR.STRAT:	return a.getStartNode().getOffset();
		case ATTR.END:		return a.getEndNode().getOffset();
		case ATTR.ID:		return a.getId();
		default:			return null;
		}
	}
	

	@Override
	public int getRowCount() {
		
		updateLastSortedKeys();
		
		return DEFAULT_ATTRS.length + lastSortedKeys.length;
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return treeSource.getSelectedAttributes().contains(getAttrByIndex(rowIndex));
		case 1:
			return getAttrByIndex(rowIndex);
		case 2:
			return getAnnotationAttr(treeSource.getDoc(), treeSource.getSelectedAnnot(), getAttrByIndex(rowIndex));
		}
		
		return null;
	}

	public Object getAttrByIndex(int index) {
		if (index < DEFAULT_ATTRS.length)
			return DEFAULT_ATTRS[index];
		
		updateLastSortedKeys();
		
		return lastSortedKeys[index-DEFAULT_ATTRS.length];
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void updateLastSortedKeys(Annotation curr, Collection ... keysToJoin) {
		Set<Object> union = new TreeSet<>((a,b) -> a.toString().compareTo(b.toString()));
		
		for (Collection keys : keysToJoin) {
			union.addAll(keys);
		}
		
		union.removeAll(Arrays.asList(DEFAULT_ATTRS));
		
		lastSortedKeys = union.toArray();
		lastSelectedAnnot = curr; 
		
	}

	protected void updateLastSortedKeys() {
		Annotation curr = treeSource.getSelectedAnnot();
		Set<Object> selection = treeSource.getSelectedAttributes();
		
		if (curr == null) {
			if (lastSelectedAnnot == null) return;
			
			updateLastSortedKeys(curr, selection);
			return;
		}

		if (curr.equals(lastSelectedAnnot)) return;

		Set<Object> keys = curr.getFeatures().keySet();
		updateLastSortedKeys(curr, selection, keys);
	}


	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 0;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return ">";
		case 1:
			return "Attribute";
		default:
		case 2:
			return "Value";
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return Boolean.class;
		default:
			return String.class;
		}
	}


	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex != 0) return;
		
		Boolean selected = (Boolean) aValue;
		
		Object attr = getAttrByIndex(rowIndex);
		
		if (selected)
			treeSource.getSelectedAttributes().add(attr);
		else
			treeSource.getSelectedAttributes().remove(attr);
		
		treeSource.fireViewChanged();
	}
	
}