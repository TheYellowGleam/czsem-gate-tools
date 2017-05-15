package czsem.netgraph.treesource;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

import czsem.gate.utils.GateAwareTreeIndexExtended;
import czsem.netgraph.GateAnnotTableModel;
import czsem.netgraph.batik.BatikTreeBuilder;

public class TreeIndexTreeSource extends TreeSourceWithSelectionSupport<Integer> implements Comparator<Integer> {
	
	private GateAwareTreeIndexExtended index = new GateAwareTreeIndexExtended(null);
	private Document doc;
	protected Integer rootNode = null;
	
	protected final LinkedHashSet<Object> selectedAttributes 
		= new LinkedHashSet<>(Collections.singleton(GateAnnotTableModel.ATTR.STRING));

	@Override
	public Integer getRoot() {
		return rootNode;
	}

	@Override
	public Collection<Integer> getChildren(Integer parent) {
		return getIndex().getChildren(parent);
	}

	@Override
	public int getNodeType(Integer node) {
		return BatikTreeBuilder.NodeType.STANDARD;
	}

	@Override
	public List<TreeSource.NodeLabel> getLabels(Integer node) {
		List<TreeSource.NodeLabel> ret = new ArrayList<>(selectedAttributes.size());
		Annotation a = getIndex().getAnnIdMap().get(node);
		if (a == null) {
			return Collections.emptyList();
		}

		for (Object attr : selectedAttributes) {
			
			Object val = GateAnnotTableModel.getAnnotationAttr(getDoc(), a, attr);
			if (val == null) val = "";
			ret.add(new StaticLabel(val.toString()));
		}
		return ret;
	}

	@Override
	public Comparator<Integer> getOrderComparator() {
		return this;
	}

	@Override
	public int compare(Integer node1, Integer node2) {
		Annotation a1 = getIndex().getAnnIdMap().get(node1);
		Annotation a2 = getIndex().getAnnIdMap().get(node2);
		return Utils.OFFSET_COMPARATOR.compare(a1, a2);
	}

	public void setTreeAS(Document doc, AnnotationSet annotations) {
		GateAwareTreeIndexExtended i = new GateAwareTreeIndexExtended(doc);
		i.setNodesAS(annotations);
		i.addDependecies(annotations.get(null, Collections.singleton("args")));
		
		setIndex(doc, i);
	}

	public void setIndex(Document doc, GateAwareTreeIndexExtended index) {
		setDoc(doc);
		setIndex(index);
		
		rootNode = index.findRootOrNull();
		setSelectedNode(rootNode);
		
		fireViewChanged();
	}

	public Annotation getSelectedAnnot() {
		return getIndex().getAnnIdMap().get(getSelectedNode());
	}

	public Document getDoc() {
		return doc;
	}

	public LinkedHashSet<Object> getSelectedAttributes() {
		return selectedAttributes;
	}

	public void selectNode(int selectedNodeID) {
		setSelectedNode(selectedNodeID);
		rootNode = getIndex().findRootForNode(getSelectedNode());
	}

	public void setDoc(Document doc) {
		this.doc = doc;
	}

	public GateAwareTreeIndexExtended getIndex() {
		return index;
	}

	public void setIndex(GateAwareTreeIndexExtended index) {
		this.index = index;
	}
}
