package gate_test;

import gate.Factory;
import gate.Gate;
import gate.Resource;
import gate.creole.Plugin;
import gate.creole.SerialAnalyserController;
import gate.util.ant.packager.PackageGappTask;
import gate.util.persistence.PersistenceManager;

import java.io.File;

import org.apache.tools.ant.Project;

import czsem.gate.utils.GateUtils;

public class PackMavenCache {

	public static void main(String[] args) throws Exception {
		/*
		System.err.println(System.getProperty("java.version"));
		
		System.setProperty("maven.compiler.source", "1.8");
		System.setProperty("maven.compiler.target", "1.8");
		*/
		
		
		Project project = new Project();

		File parentDir = new File("target/pack_tmp");
		parentDir.mkdirs();
		

		File originalGapp = new File(parentDir, "original.xgapp");
		File targetGapp = new File(parentDir, "application.xgapp");
		File mavenCache = new File(parentDir, "maven-cache.gate");

		GateUtils.initGateKeepLog();
		GateUtils.registerANNIE();
		Gate.getCreoleRegister().registerPlugin(new Plugin.Maven("uk.ac.gate.plugins", "stringannotation", "4.0"));
		Gate.getCreoleRegister().registerPlugin(new Plugin.Maven("net.sf.czsem", "czsem-gate-plugin", "3.1.0-SNAPSHOT"));
		Gate.getCreoleRegister().registerPlugin(new Plugin.Maven("net.sf.czsem", "treex-gate-plugin", "3.1.0-SNAPSHOT"));
		Gate.getCreoleRegister().registerPlugin(new Plugin.Maven("net.sf.czsem", "netgraph-tree-viewer", "3.1.0-SNAPSHOT"));
		

		Resource obj = Factory.createResource(SerialAnalyserController.class.getName());

		PersistenceManager.saveObjectToFile(obj, originalGapp);

		PackageGappTask task = new PackageGappTask();
		task.setProject(project);
		task.setSrc(originalGapp);
		task.setDestFile(targetGapp);
		task.setMavenCache(mavenCache);
		// sensible default settings
		task.setCopyPlugins(true);
		task.setCopyResourceDirs(true);
		task.setOnUnresolved(PackageGappTask.UnresolvedAction.recover);
		task.init();

		// run the task.
		task.perform();
		
		//TODO: use gate core from git 

	}

}
