package czsem.fs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeIndex {
	private static final Logger logger = LoggerFactory.getLogger(TreeIndex.class);
	
	protected Map<Integer, Integer> parentIndex;
	protected Map<Integer, Set<Integer>> childIndex;
	protected Set<Integer> nodes = new HashSet<Integer>();
	
	public TreeIndex ()
	{
		parentIndex = new HashMap<Integer, Integer>();
		childIndex = new HashMap<Integer, Set<Integer>>();		
	}



	public Integer getParent(Integer child)
	{
		return parentIndex.get(child);
	}

	public Set<Integer> getChildren(Integer parent)
	{
		return childIndex.get(parent);
	}
	
	protected void addDependency(Integer[] dep, String dependencyType)
	{
		addDependency(dep[0], dep[1], dependencyType);
	}
	
	protected void addNode(Integer id) { 
		nodes.add(id);
	}

	public void addDependency(Integer parent, Integer child) {
		addDependency(parent, child, null);
	}
	
	public void addDependency(Integer parent, Integer child, String dependencyType)
	{
		addNode(parent);
		addNode(child);
		
		//parentIndex
		parentIndex.put(child, parent);
		Set<Integer> children = childIndex.get(parent);
		
		//childIndex
		if (children == null) children = new HashSet<Integer>();
		children.add(child);
		childIndex.put(parent, children);
	}
	
	public int findRoot() {
		if (parentIndex.entrySet().isEmpty()) return -1;
		
		return findRootForNode(parentIndex.entrySet().iterator().next().getValue());
	};
	
	public Integer findRootOrNull() {
		if (parentIndex.entrySet().isEmpty()) return null;
		
		return findRootForNode(parentIndex.entrySet().iterator().next().getValue());
	};
	
	public Integer findRootForNode(Integer nodeParam)
	{
		Set<Integer> knownNodes = new HashSet<>();
		
		Integer root = nodeParam;
		for (Integer i = nodeParam; i != null; i = getParent(i))
		{
			boolean isNew = knownNodes.add(i);
			if (! isNew) {
				logger.warn("Failed to find root for node id "+nodeParam+", Cyclic graph detected.");
				return nodeParam;
				
			}
			//System.err.println(i);
			root = i;
		}
		return root;			
	}

	public Set<Integer> getAllNodes() {
		return nodes;
	}
}
