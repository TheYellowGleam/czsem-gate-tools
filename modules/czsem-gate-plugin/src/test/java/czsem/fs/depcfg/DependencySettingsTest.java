package czsem.fs.depcfg;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import czsem.fs.FSSentenceWriter.TokenDependecy;
import czsem.gate.utils.GateUtils;

public class DependencySettingsTest {

	@BeforeClass
	public void beforeClass() throws Exception {
		GateUtils.initGateKeepLog();
	}

	@Test
	public void saveLoadSettings() throws Exception {
		/*
		DependencySettings.available = DependencySettings.defaultConfigAvailable;
		DependencySettings.selected = DependencySettings.defaultConfigSelected;
		DependencySettings.saveSettings();
		*/
		
		DependencySettings.loadSettings();
		DependencySetting prevSelected = DependencySettings.getSelected();
		DependencySetting prevAvailable = DependencySettings.getAvailable();
		
		DependencySettings.available = new DependencySetting(Arrays.asList("testDep"), Arrays.asList(new TokenDependecy("testToken", "testFeature")));
		DependencySettings.selected = new DependencySetting(Arrays.asList("testDep2"), Arrays.asList(new TokenDependecy("testToken2", "testFeature2")));
		DependencySettings.saveSettings();

		DependencySettings.available = null;
		DependencySettings.selected = null;
		DependencySettings.loadSettings();
		
		Assert.assertEquals(DependencySettings.available.getDependencyNames().get(0), "testDep");
		Assert.assertEquals(DependencySettings.selected.getDependencyNames().get(0), "testDep2");
		
		Assert.assertEquals(DependencySettings.selected.getTokenDepDefs().get(0).getDepFeatureName(), "testFeature2");
		
		DependencySettings.available = prevSelected;
		DependencySettings.selected = prevAvailable;
		DependencySettings.saveSettings();
	}

}
