package czsem.netgraph.treesource;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import czsem.fs.query.FSQuery.QueryObject;
import czsem.fs.query.QueryNode;
import czsem.fs.query.restrictions.PrintableRestriction;
import czsem.netgraph.batik.BatikTreeBuilder;

public class FSQueryTreeSource extends TreeSourceWithSelectionSupport<QueryNode> {
	
	protected QueryObject queryObject;

	public FSQueryTreeSource() {
		this(null);
	}
	
	public FSQueryTreeSource(QueryObject qo) {
		this.queryObject = qo;
	}

	@Override
	public QueryNode getRoot() {
		return queryObject.getRootNode();
	}

	@Override
	public int getNodeType(QueryNode node) {
		return BatikTreeBuilder.NodeType.STANDARD;
	}

	@Override
	public List<QueryNode> getChildren(QueryNode parent) {
		return parent.getChildren();
	}
	
	public static class RestricitonLabel implements NodeLabel {

		protected final PrintableRestriction r;

		public RestricitonLabel(PrintableRestriction r) {
			this.r = r;
		}

		@Override
		public String getLeftPart() {
			return r.getLeftArg();
		}

		@Override
		public String getMiddle() {
			return r.getComparator();
		}

		@Override
		public String getRightPart() {
			return r.getRightArg();
		} 
		
	}

	@Override
	public List<NodeLabel> getLabels(QueryNode node) {
		return node.getAllRestricitions().stream()
				.map(r -> new RestricitonLabel(r))
			.collect(Collectors.toList());
	}

	@Override
	public Comparator<QueryNode> getOrderComparator() {
		return null;
	}

	public QueryObject getQueryObject() {
		return queryObject;
	}

	public void setQueryObject(QueryObject queryObject) {
		this.queryObject = queryObject;
	}

	public void updateForNewQuery() {
		/*
		if (match == null || match.getMatchingNodes().isEmpty()) return;
		
		int firstMatchingNodeId = match.getMatchingNodes().iterator().next().getNodeId();
		
		matchingNodes = match.getMatchingNodes().stream().map(NodeMatch::getNodeId).collect(Collectors.toSet());
		
		selectNode(firstMatchingNodeId);
		
		onSlectionChanged(firstMatchingNodeId);
		*/
		setSelectedNode(getRoot());
		fireViewChanged();
	}

}
