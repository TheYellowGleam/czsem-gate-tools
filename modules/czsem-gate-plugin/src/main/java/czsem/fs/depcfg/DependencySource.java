package czsem.fs.depcfg;

import czsem.gate.utils.GateAwareTreeIndex;

public interface DependencySource {

	public void addDependenciesToIndex(GateAwareTreeIndex index);
}
