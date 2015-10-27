package czsem.fs.query.restrictions;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import czsem.fs.query.FSQuery.AbstractEvaluator;
import czsem.fs.query.FSQuery.NodeMatch;
import czsem.fs.query.FSQuery.QueryData;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.fs.query.ParentQueryNodeIterator;
import czsem.fs.query.QueryNode;

public class ChildrenEvaluator extends AbstractEvaluator {
	
	public static ChildrenEvaluator childrenEvaluatorInstance = new ChildrenEvaluator();
	
	protected Iterable<QueryMatch> getChildernResultsFor(NodeMatch parentNodeMatch, QueryData data, QueryNode node, int nodeId) {
		Collection<Integer> chDataNodes = data.getIndex().getChildren(nodeId);
		
		List<QueryNode> chQueryNodes = node.getChildren();
		
		if (chDataNodes == null && chQueryNodes.size() > 0) return null;
		
		
		final ParentQueryNodeIterator mainIterator = new ParentQueryNodeIterator(parentNodeMatch, chQueryNodes, chDataNodes, data);
		
		if (! mainIterator.hasNext()) return null;
		
		return new Iterable<QueryMatch>(){

			@Override
			public Iterator<QueryMatch> iterator() {
				return mainIterator.createCopyOfInitialIteratorState();
			}
			
		};		
	}

	@Override
	public Iterable<QueryMatch> getResultsFor(QueryData data, QueryNode queryNode, int dataNodeId) {
		if (! RestrictioinsConjunctionEvaluator.evalRestricitons(data, queryNode, dataNodeId)) return null;
		
		NodeMatch parentNodeMatch = new NodeMatch(dataNodeId, queryNode);
		
		return getChildernResultsFor(parentNodeMatch, data, queryNode, dataNodeId);

		
	}
}
