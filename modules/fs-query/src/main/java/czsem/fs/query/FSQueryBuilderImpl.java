package czsem.fs.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import czsem.fs.query.constants.MetaAttribute;
import czsem.fs.query.eval.obsolete.IterateSubtreeEvaluator;
import czsem.fs.query.restrictions.OtherPrintableRestriction.*;

public class FSQueryBuilderImpl implements FSQueryBuilder {
	private static final Logger logger = LoggerFactory.getLogger(FSQueryBuilderImpl.class);
	
	public FSQueryBuilderImpl() {
		curentParent = new QueryNode(); 
		curentNode = curentParent;
	}
	
	protected Stack<QueryNode> nodeStack = new Stack<QueryNode>();
	
	protected QueryNode curentParent; 
	protected QueryNode curentNode;
	protected List<QueryNode> optionalNodes = new ArrayList<>();

	@Override
	public void addNode() {
		logger.debug("addNode");

		curentNode = new QueryNode();
		curentParent.addChild(curentNode);		
	}

	@Override
	public void beginChildren() {
		logger.debug("beginChildren");
		
		nodeStack.push(curentParent);
		curentParent = curentNode;		
	}

	@Override
	public void endChildren() {
		logger.debug("endChildren");
		
		curentParent = nodeStack.pop();
	}

	@Override
	public void addRestriction(String comparartor, String arg1,	String arg2) {
		logger.debug(String.format("addRestriction %s %s %s", arg1, comparartor, arg2));
		
		switch (arg1) {
			case MetaAttribute.NODE_NAME:
				curentNode.setName(arg2);
				curentNode.addOtherPrintableRestriction(new PrintName(curentNode));
				break;
				
			case MetaAttribute.OPTIONAL:
				if (MetaAttribute.TRUE.equals(arg2)) {
					curentNode.setOptional(true);
					getOptionalNodes().add(curentNode);
				}
				curentNode.addOtherPrintableRestriction(new PrintOptional(curentNode));
				break;
				
			case MetaAttribute.OPTIONAL_SUBTREE:
				if (MetaAttribute.TRUE.equals(arg2)) {
					curentNode.setOptionalSubtree(true);
					getOptionalNodes().add(curentNode);
				}
				curentNode.addOtherPrintableRestriction(new PrintOptionalSubtree(curentNode));
				break;
				
			case MetaAttribute.FORBIDDEN_SUBTREE:
				if (MetaAttribute.TRUE.equals(arg2)) {
					curentNode.setForbiddenSubtree(true);
				}
				curentNode.addOtherPrintableRestriction(new PrintForbiddenSubtree(curentNode));
				break;
				
			case IterateSubtreeEvaluator.META_ATTR_SUBTREE_DEPTH:
				int depth = Integer.parseInt(arg2);
				curentNode.setSubtreeDepth(depth);
				curentNode.addOtherPrintableRestriction(new PrintSubtreeDepth(curentNode));
				break;
				
			default:
				curentNode.addRestriction(comparartor, arg1, arg2);					
		}
	}

	public QueryNode getRootNode() {
		QueryNode ret = curentParent.children.iterator().next();
		ret.setPrent(null);
		return ret;
	}

	public List<QueryNode> getOptionalNodes() {
		return optionalNodes;
	}

	public void setOptionalNodes(List<QueryNode> optionalNodes) {
		this.optionalNodes = optionalNodes;
	}
}
