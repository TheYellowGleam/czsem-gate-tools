/*******************************************************************************
 * Copyright (c) 2017 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.netgraph.batik;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import czsem.netgraph.treesource.TreeSource;

public abstract class TreeSourceWithSelection<E> implements TreeSource<E> {
	public interface SelectionChangeListener<E> { 
		void onSlectionChanged(E node);
	}
	
	protected E[] nodes; 
	protected Element[] circles;
	protected int slectedNodeIndex = -1;
	
	private final List<SelectionChangeListener<E>> selectionChangeListeners = new ArrayList<>(); 
	
	protected void onSlectionChanged(E node) {
		for (SelectionChangeListener<E> l : selectionChangeListeners) {
			l.onSlectionChanged(node);
		}
	}
	
	public void fireSlectionChanged(int nodeIndex) {
		performSlectionChanged(nodeIndex);
		onSlectionChanged(nodes[nodeIndex]);
	}

	public void performSlectionChanged(int nodeIndex) {
		if (slectedNodeIndex >= 0 && slectedNodeIndex < circles.length) {
			BatikTreeBuilder.colorNodeAsNotSelected(
					circles[slectedNodeIndex], 
					getNodeType(nodes[slectedNodeIndex]));
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

	public List<SelectionChangeListener<E>> getSelectionChangeListeners() {
		return selectionChangeListeners;
	}
}