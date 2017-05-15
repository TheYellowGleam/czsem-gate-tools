package czsem.fs.depcfg;

import gate.Document;
import czsem.gate.utils.GateAwareTreeIndex;

public interface DependencySource {

	public void addDependenciesToIndex(Document document, GateAwareTreeIndex index);
}
