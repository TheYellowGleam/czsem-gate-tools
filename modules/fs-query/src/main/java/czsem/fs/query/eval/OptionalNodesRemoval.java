package czsem.fs.query.eval;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import czsem.fs.query.FSQuery.OptionalEval;
import czsem.fs.query.QueryNode;
import czsem.fs.query.utils.Combinator;
import czsem.fs.query.utils.QueryNodeDuplicator;
import czsem.fs.query.utils.ReverseCombinator;

public class OptionalNodesRemoval implements Iterator<QueryNode> {

	protected final QueryNode rootNode;
	protected final List<QueryNode> optionalNodes;
	protected Combinator combinator;
	protected boolean loaded = false;
	
	public OptionalNodesRemoval(QueryNode rootNode, List<QueryNode> optionalNodes, Combinator combinator) {
		this.rootNode = rootNode;
		this.optionalNodes = optionalNodes;
		this.combinator = combinator;
		//this.combinator = new Combinator(optionalNodes.size());
	}

	public static Iterable<QueryNode> iterateModifiedQueries(QueryNode rootNode, List<QueryNode> optionalNodes, OptionalEval optionalEval) {
		Combinator combinator = 
				OptionalEval.MINIMAL.equals(optionalEval) ?
						new ReverseCombinator(optionalNodes.size()) :
						new Combinator(optionalNodes.size());
		
		return () -> new OptionalNodesRemoval(rootNode, optionalNodes, combinator);
	}
	
	@Override
	public boolean hasNext() {
		if (loaded) return true;
		loaded = combinator.tryMove(); 
		return loaded;
	}

	@Override
	public QueryNode next() {
		if (! hasNext()) throw new NoSuchElementException();
		loaded = false;
		
		int removalSize = combinator.getGroupSize();
		int[] removalIndcies = combinator.getStack();

		//tODO debug only
		//System.err.println(Arrays.toString(Arrays.copyOfRange(removalIndcies, 0, removalSize)));
		
		Set<QueryNode> toRemove = new HashSet<>();
		for (int i = 0; i < removalSize; i++) {
			toRemove.add(optionalNodes.get(removalIndcies[i]));
		}
		
		QueryNodeDuplicator dup = new QueryNodeDuplicator(toRemove);
		QueryNode dupNode = dup.duplicate(rootNode);
		
		//System.err.println("DUP: " + dupNode.toStringDeep());
		
		
		for (QueryNode toRemoveNode : dup.getToRemoveDup()) {
			dupNode = removeNode(dupNode, toRemoveNode);
			//System.err.println("REM("+toRemoveNode+"):  " + dupNode.toStringDeep());
		}

		return dupNode;
	}

	public static QueryNode removeNode(QueryNode rootNode, QueryNode toRemove) {
		QueryNode parent = toRemove.getPrent();
		List<QueryNode> children = toRemove.getChildren();
		
		if (parent == null) {
			if (toRemove.isOptionalOrForbiddenSubtree())
				throw new IllegalArgumentException("Root node cannot be marked as _[optional|forbidden]_subtree.");
			else if (children.size() != 1)
				throw new IllegalArgumentException("Optional or forbidden root node has to have exactly one child, but found: " + toRemove.getChildren());
			else {
				QueryNode onlyChild = children.get(0);
				onlyChild.setPrent(null);
				return onlyChild; 
			}
				
		}
		
		parent.getChildren().remove(toRemove);
		
		if (! toRemove.isOptionalOrForbiddenSubtree()) {
			for (QueryNode ch : children) {
				parent.addChild(ch);
			}
		}
			
		return rootNode;
	}

	public static void main(String[] args) {
		Combinator c = new ReverseCombinator(5);
		
		while (c.tryMove()) {
			int gs = c.getGroupSize();
			System.err.println(Arrays.toString(Arrays.copyOfRange(c.getStack(), 0, gs)));
		}
	}


}
