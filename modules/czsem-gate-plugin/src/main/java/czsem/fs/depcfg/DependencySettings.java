package czsem.fs.depcfg;

import gate.Gate;
import gate.util.GateException;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import czsem.fs.FSSentenceWriter.TokenDependecy;

public class DependencySettings {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(DependencySettings.class);
	
	private static final String CFG_KEY_SELECTED = "dependency.configuration.selected";
	private static final String CFG_KEY_AVAILABLE = "dependency.configuration.available";

	protected static DependencySetting selected = null;
	protected static DependencySetting available = null;
	
	public static final DependencySetting defaultConfigSelected = 
		new DependencySetting (
			Arrays.asList(new String [] {
					"tDependency", "a/lex.rf", "Dependency", }), 
			Arrays.asList(new TokenDependecy [] {
					new TokenDependecy("tToken", "lex.rf"),}));

	public static final DependencySetting defaultConfigAvailable = 
			new DependencySetting (
				Arrays.asList(new String [] {
						"aDependency", "nDependency", "a/aux.rf", "auxRfDependency", "a.rf", "coref_gram.rf" }), 
				Arrays.asList(new TokenDependecy [0]));
	
	
	/*
	public void putToConfig(DependencySetting setting) {
		setting.clear();
		
		setting.getDependencyTypes().addAll(getDependencyNames());
		Set<String> tocs = setting.getTokenDependencies();
		for (TokenDependecy tocDep :getTokenDepDefs())
		{
			tocs.add(tocDep.tokenTypeName +"."+ tocDep.depFeatureName);				
		}
		
	}

	public static DependencyConfig getDependencyConfig() {
		DependencyConfig depsCfg;
		Config cfg = null; 
		
		try {
			cfg = Config.getConfig();
			depsCfg = cfg.getDependencyConfig();
			if (depsCfg != null) return depsCfg;
		} catch (ConfigLoadException e) {}
		
		depsCfg = new DependencyConfig();

		if (cfg != null) {
			cfg.setDependencyConfig(depsCfg);			
		}
		
		defaultConfigSelected.putToConfig(depsCfg.getSelected());		
		return depsCfg;
	}

	*/

	/** calls loadSettings(), if selected is empty return default **/
	public static DependencySetting getSelectedConfigurationFromConfigOrDefault() {
		/*
		DependencyConfig depsCfg = getDependencyConfig();
		List<TokenDependecy> tokenDepDefs = new ArrayList<FSSentenceWriter.TokenDependecy>(depsCfg.getSelected().getTokenDependencies().size());
		
		for (String s : depsCfg.getSelected().getTokenDependencies()) {
			String[] split = s.split("\\.", 2);
			if (split.length < 2) continue;
			tokenDepDefs.add(new TokenDependecy(split[0], split[1]));
		}
		return new DependencyConfiguration(depsCfg.getSelected().getDependencyTypes(), tokenDepDefs);
		*/
		
		loadSettings();
		
		if (selected.areBothEmpty())
			return defaultConfigSelected;
		else
			return selected;
	}
	
	/** Create empty settings, add loaded or default to it **/
	public static void loadSettings() {
		
		if (selected == null) { 
			selected = DependencySetting.initSetting(CFG_KEY_SELECTED, defaultConfigSelected);
		}
			
		if (available == null) { 
			available = DependencySetting.initSetting(CFG_KEY_AVAILABLE, defaultConfigAvailable);
		}
	}

	public static void saveSettings() throws IOException, GateException {
		loadSettings();
		
		selected.putToGateUserConfig(CFG_KEY_SELECTED);
		available.putToGateUserConfig(CFG_KEY_AVAILABLE);
		
		Gate.writeUserConfig();
	}

	public static DependencySetting getSelected() {
		loadSettings();
		return selected;
	}

	public static DependencySetting getAvailable() {
		loadSettings();
		return available;
	}

}