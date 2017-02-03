/*******************************************************************************
 * Copyright (c) 2017 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.netgraph.batik;

import java.util.Observable;

import org.w3c.dom.Element;

import czsem.netgraph.treesource.TreeSource;

public class SelectionHandlder<E> extends Observable {
	protected final TreeSource<E> treeSource;
	
	protected E[] nodes; 
	protected Element[] circles;
	protected int slectedNodeIndex = -1;
	
	public SelectionHandlder(TreeSource<E> treeSource) {
		this.treeSource = treeSource;
	}

	public void fireSlectionChanged(int nodeIndex) {
		if (slectedNodeIndex >= 0 && slectedNodeIndex < circles.length) {
			BatikTreeBuilder.colorNodeAsNotSelected(
					circles[slectedNodeIndex], 
					treeSource.getNodeType(nodes[slectedNodeIndex]));
		}
		
		slectedNodeIndex = nodeIndex;
		
		BatikTreeBuilder.colorNodeAsSelected(circles[slectedNodeIndex]);
	}

	public void discardSeletion() {
		slectedNodeIndex = -1;
	}
	
	public E[] getNodes() {return nodes;}
	public void setNodes(E[] nodes) {this.nodes = nodes;}
	public Element[] getCircles() {return circles;}
	public void setCircles(Element[] circles) {this.circles = circles;}
}