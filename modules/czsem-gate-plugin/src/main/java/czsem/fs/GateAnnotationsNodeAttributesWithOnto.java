package czsem.fs;

import gate.creole.ontology.OClass;
import gate.creole.ontology.OConstants;
import gate.creole.ontology.Ontology;
import czsem.gate.utils.GateAwareTreeIndexExtended;

public class GateAnnotationsNodeAttributesWithOnto extends
		GateAnnotationsNodeAttributesExtended {

	protected Ontology ontology;

	public GateAnnotationsNodeAttributesWithOnto(GateAwareTreeIndexExtended index) {
		super(index);
	}

	public Ontology getOntology() {
		return ontology;
	}

	public void setOntology(Ontology ontology) {
		this.ontology = ontology;
	}

	/** copied from {@link gate.util.SimpleFeatureMapImpl#subsumes(Ontology, gate.FeatureMap)}	 */
	@Override
	public boolean isSubClassOf(Object dataValue, String restricitonString) {
		if (dataValue == null) return false;
		if (restricitonString == null) return false;

		Ontology ontologyLR = getOntology();
		if (ontologyLR == null) return false;
		
		OClass superClass = getClassForURIOrName(ontologyLR, restricitonString);
		OClass subClass = getClassForURIOrName(ontologyLR, dataValue.toString());

		if (superClass == null || subClass == null)
			return false;

		return subClass.equals(superClass)
				|| subClass.isSubClassOf(superClass, OConstants.Closure.TRANSITIVE_CLOSURE);

	}

	/** copied from {@link gate.util.SimpleFeatureMapImpl#getClassForURIOrName}	 */
	public static OClass getClassForURIOrName(Ontology ontologyLR, String name) {
		OClass cls = null;
		try {
			cls = ontologyLR.getOClass(ontologyLR.createOURI(name));
		} catch (Exception e) {
			// do nothing, but leave cls == null
		}
		if (cls == null) {
			try {
				cls = ontologyLR.getOClass(ontologyLR.createOURIForName(name));
			} catch (Exception e) {
				// do nothing, but leave cls == null
			}
		}
		return cls;
	}

}
