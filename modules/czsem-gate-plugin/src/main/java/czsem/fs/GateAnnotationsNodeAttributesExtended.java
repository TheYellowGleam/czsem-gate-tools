package czsem.fs;

import gate.Annotation;
import gate.Utils;

import java.util.Map;

import czsem.gate.utils.GateAwareTreeIndexExtended;

public class GateAnnotationsNodeAttributesExtended extends GateAnnotationsNodeAttributesAbstract {
	
	public static final String META_ATTR_ANN_TYPE = "_annotation_type";
	public static final String META_ATTR_ANN_ID = "_annotation_id";

	public static final String META_ATTR_DEP_TYPE = "_dependency_type";

	public static final String META_ATTR_ANN_START_OFFSET = "_start_offset";
	public static final String META_ATTR_ANN_END_OFFSET = "_end_offset";
	public static final String META_ATTR_ANN_LENGTH = "_length";
	public static final String META_ATTR_ANN_STRING = "_string";
	public static final String META_ATTR_ANN_CLEAN_STRING = "_clean_string";

	protected GateAwareTreeIndexExtended index;
	protected boolean useMetaAttributes = true;

	public GateAnnotationsNodeAttributesExtended(GateAwareTreeIndexExtended index) {
		this.index = index;
	}

	@Override
	public Annotation getAnnotation(int node_id) {
		return index.getAnnIdMap().get(node_id);
	}

	public boolean isUseMetaAttributes() {
		return useMetaAttributes;
	}

	public void setUseMetaAttributes(boolean useMetaAttributes) {
		this.useMetaAttributes = useMetaAttributes;
	}
	
	@Override
	public Object getValue(int node_id, String attrName) {
		if (! isUseMetaAttributes()) return super.getValue(node_id, attrName);
		
		switch (attrName) {
		case META_ATTR_ANN_TYPE:
			return getAnnotation(node_id).getType();
			
		case META_ATTR_ANN_ID:
			return node_id;
			
		case META_ATTR_DEP_TYPE:
			return index.getDependecyTypeMap().get(node_id);
			
		case META_ATTR_ANN_START_OFFSET:
			return getAnnotation(node_id).getStartNode().getOffset();
			
		case META_ATTR_ANN_END_OFFSET:
			return getAnnotation(node_id).getEndNode().getOffset();
			
		case META_ATTR_ANN_LENGTH:
			return Utils.length(getAnnotation(node_id));
			
		case META_ATTR_ANN_STRING:
			return Utils.stringFor(index.getDocument(), getAnnotation(node_id));
			
		case META_ATTR_ANN_CLEAN_STRING:
			return Utils.cleanStringFor(index.getDocument(), getAnnotation(node_id));
			
		default:
			return super.getValue(node_id, attrName);
		}
	}

	@Override
	protected void addAdditionalAttributes(Map<String, Object> sorted,	int node_id, Annotation a) {
		sorted.put(META_ATTR_ANN_ID, node_id);
		sorted.put(META_ATTR_ANN_TYPE, a.getType());
	}
	
	
}
