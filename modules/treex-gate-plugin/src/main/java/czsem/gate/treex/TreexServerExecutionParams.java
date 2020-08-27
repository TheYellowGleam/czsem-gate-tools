/*******************************************************************************
 * Copyright (c) 2016 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.gate.treex;

import czsem.utils.AbstractConfig.ConfigLoadException;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;

public class TreexServerExecutionParams implements StringLookup {

	protected final TreexServerExecution treexServerExecution;
	protected final StringSubstitutor subst;

	public TreexServerExecutionParams(TreexServerExecution treexServerExecution) {
		this.treexServerExecution = treexServerExecution;
		subst = new StringSubstitutor(this);
	}

	public String[] expandCmdArray(String[] cmdarray) {
		if (cmdarray == null) return null;
		
		
		String[] ret = new String[cmdarray.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = subst.replace(cmdarray[i]);
		}
		return ret;
	}

	@Override
	public String lookup(String key) {
		switch (key) {
		case "port":
			return Integer.toString(treexServerExecution.getPortNumber());

		case "handshakeCode":
			return treexServerExecution.getHandshakeCode();
		
		case "treexOnlineDir":
			try {
				return TreexConfig.getConfig().getTreexOnlineDir();
			} catch (ConfigLoadException e) {
				return null;
			}
		}
		
		return null;
	}

}
