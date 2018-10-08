/*******************************************************************************
 * Copyright (c) 2018 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.gate.utils;

import gate.Gate;
import gate.Resource;
import gate.creole.Plugin;
import gate.util.GateException;

import java.io.File;
import java.net.MalformedURLException;

public class GatePluginUtils {

	public static void registerPluginDirectory(File pluginDirectory) throws MalformedURLException, GateException
	{
		Gate.getCreoleRegister().registerPlugin(
				new Plugin.Directory(pluginDirectory.toURI().toURL()));
	}

	public static class PluginFromClass extends Plugin.Component {

		public PluginFromClass(Class<? extends Resource> resourceClass)	throws MalformedURLException {
			super(resourceClass);
			baseURL = resourceClass.getResource(resourceClass.getSimpleName()+".class");
		}
	}
	
	public static void registerComponentIfNot(Class<? extends Resource> class1) throws GateException {
		if (! GateUtils.isPrCalssRegisteredInCreole(class1)) {
			try {
				Gate.getCreoleRegister().registerPlugin(new PluginFromClass(class1));
			} catch (MalformedURLException e) {
				throw new RuntimeException("registerPlugin failed", e);
			}
		}
	}
	
	public static void addKnownPluginDir(File pluginDir) throws MalformedURLException {
		Gate.addKnownPlugin(new Plugin.Directory(pluginDir.toURI().toURL()));
	}
	
	public static void registerANNIE() throws GateException {
		Gate.getCreoleRegister().registerPlugin(new Plugin.Maven("uk.ac.gate.plugins", "annie", "8.5"));
	}

}
