package czsem.fs.query.eval;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import com.google.common.collect.Iterables;

import czsem.fs.query.FSQuery.NodeMatch;
import czsem.fs.query.FSQuery.OptionalEval;
import czsem.fs.query.FSQuery.QueryData;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.fs.query.QueryNode;
import czsem.fs.query.restrictions.DirectAttrRestriction;
import czsem.fs.query.utils.CloneableIterator;
import czsem.fs.query.utils.CloneableIteratorList;
import czsem.fs.query.utils.SingletonIterator;

public class FsEvaluator {
	
	protected QueryNode rootNode;
	protected List<QueryNode> optionalNodes;
	protected QueryData data;
	protected OptionalEval optionalEval = OptionalEval.MAXIMAL;
	protected int patternIndex = 0;

	public FsEvaluator(QueryNode rootNode, List<QueryNode> optionalNodes, QueryData data) {
		this.rootNode = rootNode;
		this.optionalNodes = optionalNodes;
		this.data = data;
	}

	public static Iterable<QueryMatch> evaluatePatternPriorityList(
			List<FsEvaluator> evaluators, QueryData data) {
		
		PriorityQueue<Integer> sortedDataNodes = new PriorityQueue<>(data.getIndex().getAllNodes());
		List<Iterable<QueryMatch>> iterables = new ArrayList<>();

		
		while (! sortedDataNodes.isEmpty())
		{
			int dataNodeId = sortedDataNodes.remove();
			CloneableIterator<QueryMatch> r = null;
			
			//tODO debug only
			//System.err.format("------- %d -------\n", dataNodeId);
			
			for (FsEvaluator evaluator : evaluators) {
				r = evaluator.getFinalResultsFor(dataNodeId);
				if (r != null && r.hasNext())
					break; //return matches from the first evaluator  
			}
			
			if (r != null && r.hasNext()) 
				iterables.add(r.toIterable());

		}

		@SuppressWarnings("unchecked")
		Iterable<QueryMatch>[] array = new Iterable[iterables.size()];
		
		return Iterables.concat(iterables.toArray(array));
	}
	
	public Iterable<QueryMatch> evaluate() {
		PriorityQueue<Integer> sortedDataNodes = new PriorityQueue<>(data.getIndex().getAllNodes());
		List<Iterable<QueryMatch>> iterables = new ArrayList<>();

		
		while (! sortedDataNodes.isEmpty())
		{
			int dataNodeId = sortedDataNodes.remove();
			
			CloneableIterator<QueryMatch> r = getFinalResultsFor(dataNodeId);
			if (r != null) iterables.add(r.toIterable());

		}

		@SuppressWarnings("unchecked")
		Iterable<QueryMatch>[] array = new Iterable[iterables.size()];
		
		return Iterables.concat(iterables.toArray(array));
	}

	public CloneableIterator<QueryMatch> getFinalResultsFor(int dataNodeId) {
		List<CloneableIterator<QueryMatch>> list = new ArrayList<>();
		CloneableIterator<QueryMatch> res = null; 
		
		if (optionalNodes.isEmpty() || ! OptionalEval.MINIMAL.equals(getOptionalEval())) {

			res = getFilteredResultsFor(rootNode, dataNodeId);
			
			if ((res != null && res.hasNext()) || optionalNodes.isEmpty()) {
				
				if (OptionalEval.ALL.equals(getOptionalEval())) {
					
					//collect all matches
					list.add(res);
				
				} else {
					//return the first match found
					return res;
				}
			}
		}
		
		if (optionalNodes.isEmpty()) return res;
		
		
		for (QueryNode queryNode : OptionalNodesRemoval.iterateModifiedQueries(rootNode, optionalNodes, getOptionalEval())) {
			
			//tODO debug only
			//System.err.println(queryNode.toStringDeep());

			res = getFilteredResultsFor(queryNode, dataNodeId);
			
			if (res != null && res.hasNext()) {
				
				if (OptionalEval.ALL.equals(getOptionalEval())) {
					
					//collect all matches
					list.add(res);
					
				} else {
					
					//return the first match found
					return res;
				}
			}
		}
		
		if (list.isEmpty()) {
			if (OptionalEval.MINIMAL.equals(getOptionalEval())) {
				return getFilteredResultsFor(rootNode, dataNodeId);
			} else {
				return null; 
			}
		}
		
		return new CloneableIteratorList<>(list);		
	}

	public CloneableIterator<QueryMatch> getFilteredResultsFor(QueryNode queryNode, int dataNodeId) {
		return ReferencingRestrictionsResultsIteratorFilter.filter(
				getDirectResultsFor(queryNode, dataNodeId), 
				data, getPatternIndex());
	}

	protected CloneableIterator<QueryMatch> getDirectResultsFor(QueryNode queryNode, int dataNodeId) {
		if (! evalDirectRestricitons(queryNode, dataNodeId))
			return null;
		
		NodeMatch thisMatch = new NodeMatch(dataNodeId, queryNode);
				
		if (queryNode.getChildren().isEmpty())
			return new SingletonIterator<>(new QueryMatch(thisMatch));
			
		List<QueryNode> chQueryNodes = queryNode.getChildren();
		Set<Integer> chDataNodes = data.getIndex().getChildren(dataNodeId);
		if (chDataNodes == null || chDataNodes.isEmpty()) return null;
		
		ChildrenMatchesIterator childrenMatches = 
				ChildrenMatchesIterator.getNonEmpty(thisMatch, chQueryNodes, chDataNodes, this);
		
		if (childrenMatches == null || ! childrenMatches.hasNext())
			return null;
		
		return childrenMatches;
	}
	
	public boolean evalDirectRestricitons(QueryNode queryNode, int dataNodeId)
	{
		for (DirectAttrRestriction r : queryNode.getDirectRestrictions())
		{
			if (! r.evaluate(data, dataNodeId)) return false;
		}
		return true;
	}

	public OptionalEval getOptionalEval() {
		return optionalEval;
	}

	public void setOptionalEval(OptionalEval optionalEval) {
		this.optionalEval = optionalEval;
	}

	public int getPatternIndex() {
		return patternIndex;
	}

	public void setPatternIndex(int patternIndex) {
		this.patternIndex = patternIndex;
	}


}
