package czsem.gate.utils;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;

import java.util.HashMap;
import java.util.Map;

public class GateAwareTreeIndexExtended extends GateAwareTreeIndex {
	
	protected AnnotationSet nodesAS;
	
	protected Map<Integer, Annotation> annIdMap = new HashMap<>();
	protected Map<Integer, String> annIdDependecyKindMap = new HashMap<>();
	protected final Document document;
	
	public GateAwareTreeIndexExtended(Document document) {
		this.document = document;
	}

	public Map<Integer, Annotation> getAnnIdMap() {
		return annIdMap;
	}

	public Map<Integer, String> getDependecyTypeMap() {
		return annIdDependecyKindMap;
	}

	public AnnotationSet getNodesAS() {
		return nodesAS;
	}

	public void setNodesAS(AnnotationSet nodesAS) {
		this.nodesAS = nodesAS;
	}

	@Override
	protected void addNode(Integer id) {
		super.addNode(id);
		
		if (nodesAS != null)
			annIdMap.put(id, nodesAS.get(id));
	}

	@Override
	public void addDependency(Annotation parentAnn, Annotation childAnn, String dependencyType) {
		super.addDependency(parentAnn, childAnn, dependencyType);
		addNodeAnnotation(parentAnn);
		addNodeAnnotation(childAnn);
	}

	protected void addNodeAnnotation(Annotation a) {
		annIdMap.put(a.getId(), a);
	}

	@Override
	public void addDependency(Integer parent, Integer child, String dependencyType) {
		super.addDependency(parent, child, dependencyType);
		annIdDependecyKindMap.put(child, dependencyType);
	}

	public Document getDocument() {
		return document;
	}

}
