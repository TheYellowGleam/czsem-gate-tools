/*******************************************************************************
 * Copyright (c) 2017 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.netgraph.treesource;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import czsem.fs.query.FSQuery.NodeMatch;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.netgraph.batik.BatikTreeBuilder;

public class TreeSourceWithQueryMatch extends TreeIndexTreeSource {
	private QueryMatch queryMatch;
	protected Set<Integer> matchingNodes = Collections.emptySet();

	public QueryMatch getQueryMatch() {
		return queryMatch;
	}

	public void setQueryMatch(QueryMatch queryMatch) {
		this.queryMatch = queryMatch;
		
		updateForQueryMatch();
	}

	protected void updateForQueryMatch() {
		QueryMatch match = getQueryMatch();
		
		if (match == null || match.getMatchingNodes().isEmpty()) return;
		
		int firstMatchingNodeId = match.getMatchingNodes().iterator().next().getNodeId();
		
		matchingNodes = match.getMatchingNodes().stream().map(NodeMatch::getNodeId).collect(Collectors.toSet());
		
		selectNode(firstMatchingNodeId);
		
		onSlectionChanged(firstMatchingNodeId);
		fireViewChanged();
	}

	@Override
	public int getNodeType(Integer node) {
		if (matchingNodes.contains(node))
			return BatikTreeBuilder.NodeType.EMPHASIZED;
					
		return BatikTreeBuilder.NodeType.STANDARD;
	}

	@Override
	public int getEdgeType(Integer parent, Integer child) {
		if (matchingNodes.contains(parent) && matchingNodes.contains(child))
			return BatikTreeBuilder.NodeType.EMPHASIZED;
		
		return BatikTreeBuilder.NodeType.STANDARD;
	}
	
	
}
