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
import czsem.netgraph.batik.TreeSourceWithSelection;

public class TreeIndexTreeSource extends TreeSourceWithSelection<Integer> implements Comparator<Integer> {
	
	protected GateAwareTreeIndexExtended index = new GateAwareTreeIndexExtended(null);
	protected Document doc;
	protected Integer rootNode = null;
	
	protected final LinkedHashSet<Object> selectedAttributes 
		= new LinkedHashSet<>(Collections.singleton(GateAnnotTableModel.ATTR.STRING));

	@Override
	public Integer getRoot() {
		return rootNode;
	}

	@Override
	public Collection<Integer> getChildren(Integer parent) {
		return index.getChildren(parent);
	}

	@Override
	public int getNodeType(Integer node) {
		return BatikTreeBuilder.NodeType.STANDARD;
	}

	@Override
	public List<TreeSource.NodeLabel> getLabels(Integer node) {
		List<TreeSource.NodeLabel> ret = new ArrayList<>(selectedAttributes.size());
		for (Object attr : selectedAttributes) {
			Annotation a = index.getAnnIdMap().get(node);
			Object val = GateAnnotTableModel.getAnnotationAttr(doc, a, attr);
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
		Annotation a1 = index.getAnnIdMap().get(node1);
		Annotation a2 = index.getAnnIdMap().get(node2);
		return Utils.OFFSET_COMPARATOR.compare(a1, a2);
	}

	public void setTreeAS(Document doc, AnnotationSet annotations) {
		GateAwareTreeIndexExtended i = new GateAwareTreeIndexExtended(doc);
		i.setNodesAS(annotations);
		i.addDependecies(annotations.get(null, Collections.singleton("args")));
		
		setIndex(doc, i);
	}

	public void setIndex(Document doc, GateAwareTreeIndexExtended index) {
		this.doc = doc;
		this.index = index;
		rootNode = index.findRootOrNull();
		setSelectedNode(rootNode);
		
		fireViewChanged();
	}

	public Annotation getSelectedAnnot() {
		return index.getAnnIdMap().get(getSelectedNode());
	}

	public Document getDoc() {
		return doc;
	}

	public LinkedHashSet<Object> getSelectedAttributes() {
		return selectedAttributes;
	}

	public void selectNode(int selectedNodeID) {
		setSelectedNode(selectedNodeID);
		rootNode = index.findRootForNode(getSelectedNode());
	}
}
