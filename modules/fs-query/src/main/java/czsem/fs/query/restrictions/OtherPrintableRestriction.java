package czsem.fs.query.restrictions;

import czsem.fs.query.QueryNode;
import czsem.fs.query.constants.MetaAttribute;
import czsem.fs.query.eval.obsolete.IterateSubtreeEvaluator;

public abstract class OtherPrintableRestriction implements PrintableRestriction {
	
	protected final QueryNode n;

	public OtherPrintableRestriction(QueryNode n) {
		this.n = n;
	}

	@Override
	public String getComparator() {
		return "=";
	}
	
	
	public static class PrintName extends  OtherPrintableRestriction 
	{
		public PrintName(QueryNode n) {	super(n);}
		@Override
		public String getLeftArg() { return MetaAttribute.NODE_NAME; }
		@Override
		public String getRightArg() {return n.getName();} 
	}
	public static class PrintOptional extends  OtherPrintableRestriction 
	{
		public PrintOptional(QueryNode n) {	super(n);}
		@Override
		public String getLeftArg() { return MetaAttribute.OPTIONAL; }
		@Override
		public String getRightArg() {return Boolean.toString(n.isOptional());} 
	}
	public static class PrintOptionalSubtree extends  OtherPrintableRestriction 
	{
		public PrintOptionalSubtree(QueryNode n) {	super(n);}
		@Override
		public String getLeftArg() { return MetaAttribute.OPTIONAL_SUBTREE; }
		@Override
		public String getRightArg() {return Boolean.toString(n.isOptionalSubtree());} 
	}
	public static class PrintForbiddenSubtree extends  OtherPrintableRestriction 
	{
		public PrintForbiddenSubtree(QueryNode n) {	super(n);}
		@Override
		public String getLeftArg() { return MetaAttribute.FORBIDDEN_SUBTREE; }
		@Override
		public String getRightArg() {return Boolean.toString(n.isForbiddenSubtree());} 
	}
	public static class PrintSubtreeDepth extends  OtherPrintableRestriction 
	{
		public PrintSubtreeDepth(QueryNode n) {	super(n);}
		@Override
		public String getLeftArg() { return IterateSubtreeEvaluator.META_ATTR_SUBTREE_DEPTH; }
		@Override
		public String getRightArg() {return Integer.toString(n.getSubtreeDepth());} 
	}
}
