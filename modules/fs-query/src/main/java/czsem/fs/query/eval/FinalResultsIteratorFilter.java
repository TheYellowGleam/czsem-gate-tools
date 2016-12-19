/*******************************************************************************
 * Copyright (c) 2016 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.fs.query.eval;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import czsem.fs.query.FSQuery.NodeMatch;
import czsem.fs.query.FSQuery.OptionalEval;
import czsem.fs.query.FSQuery.QueryData;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.fs.query.QueryNode;
import czsem.fs.query.restrictions.ReferencingRestriction;
import czsem.fs.query.utils.CloneableIterator;

public class FinalResultsIteratorFilter implements CloneableIterator<QueryMatch>{
	
	protected final CloneableIterator<QueryMatch> parent;
	protected final QueryData data;
	protected final int patternIndex;
	protected final Map<QueryNode, QueryNode> forbiddenSubtreeMap;
	protected final FsEvaluator forbiddenSubtreeEvaluator;


	public FinalResultsIteratorFilter(CloneableIterator<QueryMatch> parent, QueryData data, int patternIndex, Map<QueryNode, QueryNode> forbiddenSubtreeMap) {
		this.parent = parent;
		this.data = data;
		this.patternIndex = patternIndex;
		this.forbiddenSubtreeMap = forbiddenSubtreeMap;
		
		forbiddenSubtreeEvaluator = new FsEvaluator(null, null, data); 
	}
	
	protected QueryMatch cachedValue = null; 

	@Override
	public boolean hasNext() {
		
		while (cachedValue == null) {
			if (! parent.hasNext()) return false;
			
			cachedValue = parent.next();

			if (! evalReferencingRestrictions(cachedValue)) {
				cachedValue = null;
				continue;
			}

			if (! checkForbiddenNodes(cachedValue)) {
				cachedValue = null;
			}
		}
		
		return true;
	}

	@Override
	public QueryMatch next() {
		if (! hasNext())
			throw new NoSuchElementException();
		
		QueryMatch ret = cachedValue;
		cachedValue = null;
		ret.setPatternIndex(patternIndex);
		return ret;
	}

	public static Map<String, Integer>  createDataBindings(QueryMatch queryMatch) {
		return queryMatch.getMatchingNodes().stream()
				
			.filter(n -> n.getQueryNode().getName() != null)
			.collect(Collectors.toMap(
					n -> n.getQueryNode().getName(), 
					NodeMatch::getNodeId)) 
		;
	}

	protected boolean checkForbiddenNodes(QueryMatch queryMatch) {
		for (NodeMatch nodeMatch : queryMatch.getMatchingNodes()) {
			QueryNode qn = nodeMatch.getQueryNode();
			
			QueryNode forbiddenSubtree = forbiddenSubtreeMap.get(qn);
			if (forbiddenSubtree == null) continue;
			
			Set<Integer> dataChildren = data.getIndex().getChildren(nodeMatch.getNodeId());
			if (dataChildren == null) continue;
			
			List<QueryNode> optionalNodes = new ArrayList<>();
			FsEvaluator.findOptional(forbiddenSubtree, optionalNodes); 
			
			for (int dataChild : dataChildren) {
				
				//System.err.println(forbiddenSubtree.toStringDeep());

				Iterator<QueryMatch> res = forbiddenSubtreeEvaluator.getDirectResultsFor(forbiddenSubtree, dataChild);
				if (forbiddenSubtreeMatches(queryMatch, res)) return false;
				
				if (optionalNodes.isEmpty()) continue;
				
				Iterable<QueryNode> iter = 
						OptionalNodesRemoval.iterateModifiedQueries(
								forbiddenSubtree, optionalNodes, OptionalEval.MAXIMAL, true);
				
				for (QueryNode queryNode : iter) {
					res = forbiddenSubtreeEvaluator.getDirectResultsFor(queryNode, dataChild);
					if (forbiddenSubtreeMatches(queryMatch, res)) return false;
				}
			}
		}
		return true;
	}
	
	protected boolean forbiddenSubtreeMatches(QueryMatch queryMatch, Iterator<QueryMatch> res) {
		if (res == null || ! res.hasNext()) return false;
		
		while (res.hasNext()) {
			QueryMatch next = res.next();
			
			List<NodeMatch> matchingNodes = new ArrayList<>(next.getMatchingNodes());
			matchingNodes.addAll(queryMatch.getMatchingNodes());
			
			if (evalReferencingRestrictions(new QueryMatch(matchingNodes)))
				return true;
		}
		
		return false;
	}
	
	protected boolean evalReferencingRestrictions(QueryMatch queryMatch) {
		Map<String, Integer> dataBindings;
		
		try {
			dataBindings = createDataBindings(queryMatch);
		} catch (IllegalStateException e) {
			throw new IllegalStateException("Failed to collect dataBindings, check for duplicated node name: "+ queryMatch.getMatchingNodes());
		}
		
		for (NodeMatch nodeMatch : queryMatch.getMatchingNodes()) {
			if (!evalReferencingRestrictions(nodeMatch, dataBindings))
				return false;
		}
		return true;
	}

	protected boolean evalReferencingRestrictions(NodeMatch nodeMatch, Map<String, Integer>  dataBindings) {
		for (ReferencingRestriction r : nodeMatch.getQueryNode().getReferencingRestrictions()) {
			if (! r.evaluate(data, nodeMatch.getNodeId(), dataBindings))
				return false;
		}
		
		return true;
	}

	public static CloneableIterator<QueryMatch> filter(
			CloneableIterator<QueryMatch> resultsFor, 
			QueryData data, 
			int patternIndex, 
			Map<QueryNode, QueryNode> forbiddenSubtreeMap) 
	{
		if (resultsFor == null) return null;
		
		return new FinalResultsIteratorFilter(resultsFor, data, patternIndex, forbiddenSubtreeMap);
	}

	@Override
	public FinalResultsIteratorFilter cloneInitial() {
		return new FinalResultsIteratorFilter(
				parent.cloneInitial(), data, patternIndex, forbiddenSubtreeMap);	
	} 
		
}
