package czsem.fs.depcfg;

import czsem.fs.FSSentenceWriter.TokenDependecy;
import czsem.gate.utils.GateAwareTreeIndex;
import gate.AnnotationSet;

public class DependencySourceFromCfg implements DependencySource {

	private final AnnotationSet annotations;
	private final DependencySetting configuration;

	public DependencySourceFromCfg(DependencySetting cfg, AnnotationSet annotations) {
		this.configuration = cfg;
		this.annotations = annotations;
	}

	@Override
	public void addDependenciesToIndex(GateAwareTreeIndex index) {
		for (String depName : configuration.getDependencyNames())
			index.addDependecies(annotations.get(depName));

		for (TokenDependecy tocDep : configuration.getTokenDepDefs())
			index.addTokenDependecies(annotations.get(tocDep.getTokenTypeName()), tocDep.getDepFeatureName());		
	}

}
