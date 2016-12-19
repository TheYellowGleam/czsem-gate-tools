package czsem.fs;

import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.Utils;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import czsem.gate.utils.GateAwareTreeIndexExtended;
import czsem.gate.utils.GateUtils;

public class GateAnnotationsNodeAttributesExtendedTest {
	@BeforeClass
	public void beforeClass() throws Exception {
		GateUtils.initGateKeepLog();

	}

	@Test
	public void getValue() throws Exception {
		
		Document doc = Factory.newDocument("test doc");
		AnnotationSet as = doc.getAnnotations();
		Integer a1 = as.add(0l, 4l, "Token1", Utils.featureMap("f", 0));
		Integer a2 = as.add(4l, 8l, "Token2", Utils.featureMap("f", 1));
		GateAwareTreeIndexExtended index = new GateAwareTreeIndexExtended(doc);
		index.addDependency(as.get(a1), as.get(a2), "testDepType");
		//index.addDependency(a1, a2, "testDepType");
		
		GateAnnotationsNodeAttributesExtended attrs = new GateAnnotationsNodeAttributesExtended(index);
		
		assertValue(attrs, a1, a1, GateAnnotationsNodeAttributesExtended.META_ATTR_ANN_ID);
		assertValue(attrs, a2, a2, GateAnnotationsNodeAttributesExtended.META_ATTR_ANN_ID);
		
		assertValue(attrs, a1, "Token1", GateAnnotationsNodeAttributesExtended.META_ATTR_ANN_TYPE);
		assertValue(attrs, a2, "Token2", GateAnnotationsNodeAttributesExtended.META_ATTR_ANN_TYPE);

		assertValue(attrs, a2, "testDepType", GateAnnotationsNodeAttributesExtended.META_ATTR_DEP_TYPE);
		assertValue(attrs, a1, null, GateAnnotationsNodeAttributesExtended.META_ATTR_DEP_TYPE);

		assertValue(attrs, a1, "test", GateAnnotationsNodeAttributesExtended.META_ATTR_ANN_STRING);
		assertValue(attrs, a2, " doc", GateAnnotationsNodeAttributesExtended.META_ATTR_ANN_STRING);
		assertValue(attrs, a2, "doc", GateAnnotationsNodeAttributesExtended.META_ATTR_ANN_CLEAN_STRING);

		assertValue(attrs, a2, 4l, GateAnnotationsNodeAttributesExtended.META_ATTR_ANN_START_OFFSET);
		assertValue(attrs, a2, 8l, GateAnnotationsNodeAttributesExtended.META_ATTR_ANN_END_OFFSET);
		assertValue(attrs, a2, 4, GateAnnotationsNodeAttributesExtended.META_ATTR_ANN_LENGTH);
	}

	public static void assertValue(GateAnnotationsNodeAttributesExtended attrs, Integer annId, Object expected, String attrName) {
		Assert.assertEquals(attrs.getValue(annId, attrName), expected);
	}
}
