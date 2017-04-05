/*******************************************************************************
 * Copyright (c) 2017 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.netgraph.treesource;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import czsem.netgraph.batik.BatikTreeBuilder;

public abstract class TreeSourceWithSelectionSupport<E> implements TreeSource<E> {
	public interface SelectionChangeListener<E> { 
		void onSlectionChanged(E node);
	}

	public interface ViewChangedListener { 
		void onViewChanged();
	}
	
	protected E[] nodes; 
	protected Element[] circles;
	
	//change consistently
	private int selectedNodeIndex = -1;
	private E selectedNode;
	
	
	private final List<SelectionChangeListener<E>> selectionChangeListeners = new ArrayList<>(); 
	private final List<ViewChangedListener> viewChangedListeners = new ArrayList<>(); 
	
	protected void onSlectionChanged(E node) {
		for (SelectionChangeListener<E> l : selectionChangeListeners) {
			l.onSlectionChanged(node);
		}
	}

	public void fireViewChanged() {
		for (ViewChangedListener l : viewChangedListeners) {
			l.onViewChanged();
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
					getNodeType(nodes[selectedNodeIndex]));
		}
		
		selectedNodeIndex = nodeIndex;
		selectedNode = nodes[selectedNodeIndex];
		
		BatikTreeBuilder.colorNodeAsSelected(getSelectedCicle());
	}

	public E[] getNodes() {return nodes;}
	public Element[] getCircles() {return circles;}
	public void setCircles(Element[] circles) {this.circles = circles;}

	public void setNodes(E[] nodes) {
		this.nodes = nodes;
		updateSelectedNodeIndex();
	}

	public List<SelectionChangeListener<E>> getSelectionChangeListeners() {
		return selectionChangeListeners;
	}

	public List<ViewChangedListener> getViewChangedListeners() {
		return viewChangedListeners;
	}

	public Element getSelectedCicle() {
		if (selectedNodeIndex >= 0 && selectedNodeIndex < circles.length) {
			return circles[selectedNodeIndex];
		}
		
		return null;
	}

	public E getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(E selectedNode) {
		this.selectedNode = selectedNode;
		updateSelectedNodeIndex();
	}

	protected void updateSelectedNodeIndex() {
		selectedNodeIndex = -1;

		if (selectedNode == null) return;
		if (nodes == null) return;
		
		for (int i = 0; i < nodes.length; i++) {
			if (selectedNode.equals(nodes[i])) {
				selectedNodeIndex = i;
				return;
			}
		}
	}
}