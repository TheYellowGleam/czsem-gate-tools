package czsem.netgraph;

import gate.Annotation;
import gate.FeatureMap;
import gate.Utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.table.AbstractTableModel;

import static czsem.fs.GateAnnotationsNodeAttributesExtended.*;
import czsem.netgraph.treesource.TreeIndexTreeSource;

public class GateAnnotTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -1999028584101610952L;
	
	public static final String [] DEFAULT_ATTRS = {
		META_ATTR_ANN_STRING,
		META_ATTR_ANN_TYPE,
		META_ATTR_ANN_START_OFFSET,
		META_ATTR_ANN_END_OFFSET,
		META_ATTR_ANN_LENGTH,
		META_ATTR_ANN_ID,
		META_ATTR_DEP_TYPE,
	};
	

	protected TreeIndexTreeSource treeSource;
	protected Object[] lastSortedKeys = new Object[0];
	protected Annotation lastSelectedAnnot;
	
	public GateAnnotTableModel(TreeIndexTreeSource treeSource) {
		this.treeSource = treeSource;
		treeSource.getSelectionChangeListeners().add(x -> fireTableDataChanged());
	}


	public static Object getAnnotationAttr(TreeIndexTreeSource treeSource, Annotation a, Object attr) {
		if (a == null) return null;
		
		FeatureMap fm = a.getFeatures();
		if (fm.containsKey(attr)) return fm.get(attr);
		
		String str = attr.toString();
		
		//TODO use GateAnnotationsNodeAttributesExtended
		switch (str) {
		case META_ATTR_ANN_STRING:	return Utils.stringFor(treeSource.getDoc(), a);
		case META_ATTR_ANN_TYPE:		return a.getType();
		case META_ATTR_ANN_START_OFFSET:	return a.getStartNode().getOffset();
		case META_ATTR_ANN_END_OFFSET:		return a.getEndNode().getOffset();
		case META_ATTR_ANN_LENGTH:		return Utils.length(a);
		case META_ATTR_ANN_ID:		return a.getId();
		case META_ATTR_DEP_TYPE:	return treeSource.getIndex().getDependecyTypeMap().get(a.getId());
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
			return getAnnotationAttr(treeSource, treeSource.getSelectedAnnot(), getAttrByIndex(rowIndex));
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