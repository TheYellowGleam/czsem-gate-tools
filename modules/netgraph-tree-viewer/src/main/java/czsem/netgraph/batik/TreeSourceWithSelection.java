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

	public interface ViewChangedListener { 
		void onViewChanged(boolean keepSelectedNode);
	}
	
	protected E[] nodes; 
	protected Element[] circles;
	protected int slectedNodeIndex = -1;
	
	private final List<SelectionChangeListener<E>> selectionChangeListeners = new ArrayList<>(); 
	private final List<ViewChangedListener> viewChangedListeners = new ArrayList<>(); 
	
	protected void onSlectionChanged(E node) {
		for (SelectionChangeListener<E> l : selectionChangeListeners) {
			l.onSlectionChanged(node);
		}
	}

	public void fireViewChanged(boolean keepSelectedNode) {
		for (ViewChangedListener l : viewChangedListeners) {
			l.onViewChanged(keepSelectedNode);
		}
	}
	
	public void fireSlectionChanged(int nodeIndex) {
		performSlectionChanged(nodeIndex);
		onSlectionChanged(nodes[nodeIndex]);
	}

	public void performSlectionChanged(int nodeIndex) {
		Element sc = getSelectedCicle();
		if (sc != null) {
			BatikTreeBuilder.colorNodeAsNotSelected(sc,
					getNodeType(nodes[slectedNodeIndex]));
		}
		
		slectedNodeIndex = nodeIndex;
		
		BatikTreeBuilder.colorNodeAsSelected(getSelectedCicle());
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

	public List<ViewChangedListener> getViewChangedListeners() {
		return viewChangedListeners;
	}

	public Element getSelectedCicle() {
		if (slectedNodeIndex >= 0 && slectedNodeIndex < circles.length) {
			return circles[slectedNodeIndex];
		}
		
		return null;
	}
}