/*******************************************************************************
 * Copyright (c) 2015 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.utils;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import czsem.Utils;

public abstract class AbstractConfig {
	private static final Logger logger = LoggerFactory.getLogger(AbstractConfig.class);

	public static final String CONFIG_DIR_ENVP = "CZSEM_CONFIG_DIR";
	public static final String CONFIG_DIR = "configuration";
	public static final String TOOLS_DIR = "czsem-gate-tools";

	private static final Map<String,AbstractConfig> all_config_singletons = new HashMap<String, AbstractConfig>();

	public abstract String getConfigKey();
	protected abstract void updateDefaults();

	
	private Map<String, Object> map;
	private String loadedFrom;
	
	protected <E extends AbstractConfig> E getSingleton() throws ConfigLoadException {
		
		@SuppressWarnings("unchecked")
		E inst = (E) this;
		
		return getSingleton(inst);
	}
	
	protected static synchronized <E extends AbstractConfig> E getSingleton(E instance) throws ConfigLoadException {
		if (all_config_singletons.containsKey(instance.getConfigKey())) {
			
			@SuppressWarnings("unchecked")
			E old = (E) all_config_singletons.get(instance.getConfigKey());
			
			return old ;
		}
		
		instance.loadConfig();
		
		all_config_singletons.put(instance.getConfigKey(), instance);
		
		return instance;
	}
	
	public void loadConfig() throws ConfigLoadException 
	{
		loadConfig(null);
		updateDefaults();
	}

	public String getEnvOrSysProperty(String name) {
		return Utils.getEnvOrSysProperty(name);
	}

	public static Map<String, Object> loadFromFile(String filename, ClassLoader classLoader) throws IOException
	{
		FileInputStream is = new FileInputStream(filename);
		return loadFromStream(is, classLoader);
	}

	public static Map<String, Object> loadFromStream(InputStream inputStream, ClassLoader classLoader) throws IOException
	{
		XMLDecoder decoder = new XMLDecoder(inputStream, null, null, classLoader);
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) decoder.readObject();
		decoder.close();
		inputStream.close();
		return map;
	}

	public static void saveToFile(String filename, Map<String, Object> map) throws IOException
	{
		FileOutputStream os = new FileOutputStream(filename);
		XMLEncoder encoder = new XMLEncoder(os);
		encoder.writeObject(map);
		encoder.close();
		os.close();
	}
	
	public void loadClassPathConfig(String path, ClassLoader classLoader) throws IOException {
		InputStream stream;
		String from;
		
		if (classLoader != null) {
			stream = classLoader.getResourceAsStream(path);
		} else {
			stream = getClass().getResourceAsStream(path);
		}
		
		if (stream == null) throw new FileNotFoundException("File not found on classpath: "+path);

		if (classLoader != null) {
			from = classLoader.getResource(path).toString();
		} else {
			from = getClass().getResource(path).toString();
		}

		setMap(loadFromStream(stream, classLoader));
		
		setLoadedFrom(from);
		
		logger.info("Config '"+getConfigKey()+"' loaded from: "+from);
	}
	
	public void loadConfig(String filename, ClassLoader classLoader) throws IOException
	{
		setMap(loadFromFile(filename, classLoader));
		
		setLoadedFrom(new File(filename).getAbsolutePath());
		
		logger.info("Config '"+getConfigKey()+"' loaded from: "+new File(filename).getAbsolutePath());
	}
	
	
	public void loadConfig(ClassLoader classLoader) throws ConfigLoadException
	{
		ConfigLoadException fe = new ConfigLoadException();
		
		//first: try env specific
		try {
			String env_key = getConfigKey().toUpperCase();
			String env = getEnvOrSysProperty(env_key);
			if (env == null) throw new EnvOrSysPropertyNotSetException(String.format("Environment property  '%s' not set.", env_key));
			loadConfig(env, classLoader);
			return;
		}
		catch (IOException e)	{fe.add(e);}

		//second: try env_dir
		try {
			String env_key = CONFIG_DIR_ENVP;
			String env = getEnvOrSysProperty(env_key);
			if (env == null) throw new EnvOrSysPropertyNotSetException(String.format("Environment property  '%s' not set.", env_key));
			loadConfig(env + "/" + getConfigKey() + ".xml", classLoader);
			return;
		}
		catch (IOException e)	{fe.add(e);}
		
		String [] tryPaths = { 
			"",
			CONFIG_DIR+"/",
			"../",
			"../"+TOOLS_DIR+"/"+CONFIG_DIR+"/",
			"../../",
			"../../"+TOOLS_DIR+"/"+CONFIG_DIR+"/",
		};
		
		for (String tryPath : tryPaths) {
			try {
				loadConfig(tryPath + getDefaultLoc(), classLoader);				
				return;
			} catch (IOException e) {fe.add(e);}
		}
		
		//from classpath
		try {
			loadClassPathConfig(getDefaultLoc().replaceFirst("^..", ""), classLoader);				
			return;
		} catch (IOException e) {fe.add(e);}

		throw fe;
	}
	
	public String getDefaultLoc() {
		return "../" +CONFIG_DIR+ '/' + getConfigKey() + ".xml";
	}

	protected Map<String, Object> getMap() {
		return map;
	}

	protected void setMap(Map<String, Object> map) {
		this.map = map;
	}

	public String getLoadedFrom() {
		return loadedFrom;
	}

	public void setLoadedFrom(String loadedFrom) {
		this.loadedFrom = loadedFrom;
	}

	public static class ConfigLoadException extends FileNotFoundException
	{
		private static final long serialVersionUID = -5616178151757529473L;
		
		protected List<Exception> causes = new ArrayList<Exception>();
		
		public ConfigLoadException() {}

		public ConfigLoadException(Exception e) {
			add(e);
		}

		public void add(Exception e) {
			causes.add(e);
		}

		@Override
		public String getMessage() {
			StringBuilder sb = new StringBuilder(String.format("Configuration file could not be loaded for one of following (%d) reasons:\n", causes.size()));
			for (int a=0; a<causes.size(); a++)
			{
				sb.append(String.format("%d: %s\n", a+1, causes.get(a).toString()));
			}
			
			sb.append(String.format("Current working direcotry is: %s",  System.getProperty("user.dir")));
			
			return sb.toString();
		}
	}

	public static class EnvOrSysPropertyNotSetException extends IOException {
		private static final long serialVersionUID = -2037380563367210974L;
		public EnvOrSysPropertyNotSetException(String msg) {
			super(msg);
		}
	};

	protected void set(String key, Object value)
	{
		getMap().put(key, value);
	}

	protected String get(String key)
	{
		return (String) getObj(key);
	}

	protected Object getObj(String key) {
		return getMap().get(key);
	}
	
	public boolean hasKey(String key) {
		return getMap().containsKey(key);
	}
	
	public static interface  DefaultValueGetter {
		Object getDefaultValue();
	}
	
	protected void setDefaultFun(String key, DefaultValueGetter defaultValue) {
		if (! hasKey(key)) {
			forceDefault(key, defaultValue);
		}
	}

	protected void forceDefault(String key, DefaultValueGetter defaultValue) {
		setDefaultVal(key, defaultValue.getDefaultValue());
	}
	
	protected void setDefaultVal(String key, Object defaultValue)
	{
		if (! hasKey(key)) {
			logger.info("Config property '{}' not set, setting to default: '{}'.\n ...config file was: {}", key, defaultValue, getLoadedFrom());
			set(key, defaultValue);
		}
	}
		

}
