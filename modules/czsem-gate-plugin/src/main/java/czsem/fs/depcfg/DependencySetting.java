package czsem.fs.depcfg;

import gate.Gate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import czsem.fs.FSSentenceWriter.TokenDependecy;

public class DependencySetting {
	private static final Logger logger = LoggerFactory.getLogger(DependencySetting.class);

	protected static final ObjectMapper mapper = new ObjectMapper(); 

	private List<String> dependencyNames;
	private List<TokenDependecy> tokenDepDefs;

	public DependencySetting(List<String> dependencyNames,	List<TokenDependecy> tokenDepDefs) {
		this.setDependencyNames(dependencyNames);
		this.setTokenDepDefs(tokenDepDefs);
	}
	
	public DependencySetting() {
		this(new ArrayList<>(10), new ArrayList<>(10));
	}

	public List<String> getDependencyNames() {
		return dependencyNames;
	}

	public void setDependencyNames(List<String> dependencyNames) {
		this.dependencyNames = dependencyNames;
	}

	public List<TokenDependecy> getTokenDepDefs() {
		return tokenDepDefs;
	}

	public void setTokenDepDefs(List<TokenDependecy> tokenDepDefs) {
		this.tokenDepDefs = tokenDepDefs;
	}

	public void addAll(DependencySetting other) {
		if (other == null) return;
		
		dependencyNames.addAll(other.dependencyNames);
		tokenDepDefs.addAll(other.tokenDepDefs);
	}

	public boolean areBothEmpty() {
		return dependencyNames.isEmpty() && tokenDepDefs.isEmpty();
	}
	
	public void putToGateUserConfig(String key) throws IOException {
		String json = mapper.writeValueAsString(this);
		Gate.getUserConfig().put(key, json);
		//Gate.writeUserConfig();
	}

	public static DependencySetting getFromGateUserConfig(String key) throws IOException {
		String json = Gate.getUserConfig().getString(key);
		if (json == null) return null;
		DependencySetting ret = mapper.readValue(json, DependencySetting.class);
		return ret;
	}
	
	public static DependencySetting initSetting(String key, DependencySetting defaultCfg) {
		DependencySetting ret = new DependencySetting();
		DependencySetting fromGate = null;
		try {
			fromGate = DependencySetting.getFromGateUserConfig(key);
			ret.addAll(fromGate);
		} catch (IOException e) {
			logger.warn("Failed to load cfg from GATE user config {}", key, e);
		}
		
		if (fromGate == null)
			ret.addAll(defaultCfg);
			
		return ret;
	}
	
}
