/*******************************************************************************
 * Copyright (c) 2016 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.netgraph.batik;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.batik.swing.JSVGCanvas;

import czsem.netgraph.treesource.TreeSourceWithSelection;
import czsem.netgraph.treesource.TreeSourceWithSelection.ViewChangedListener;

public class BatikView implements MouseWheelListener, ViewChangedListener {
	
	protected final TreeSourceWithSelection<?> treeSource;
	public static final double scaleIncrement = 0.1; 
	
	protected double currentScale = 1.0;
	protected Dimension origSize;
	
	private JScrollPane pane;
	
	private final JSVGCanvas svgCanvas = new JSVGCanvas() {
		private static final long serialVersionUID = -4362953581038733653L;
		
		@Override
		public void setMySize(Dimension d) {};
		
	};

	public BatikView(TreeSourceWithSelection<?> treeSource) {
		this.treeSource = treeSource;
		treeSource.getViewChangedListeners().add(this);
	}

	protected <E> void fillCanvas(TreeSourceWithSelection<E> treeSource) {
		BatikTreeBuilder<E> b = new BatikTreeBuilder<>(treeSource);
		b.buildNewSvgTree();
		
		origSize = b.getSize();
		svgCanvas.setBackground(BatikTreeBuilder.Color.CANVAS_BACKGROUND);
		svgCanvas.setSVGDocument(b.getDoc());

		svgCanvas.setPreferredSize(origSize);
	}
	
	public Component getComponent() {
		svgCanvas.setRecenterOnResize(false);
		//svgCanvas.setMinimumSize(new Dimension(800, 600));
		//svgCanvas.setEnableImageZoomInteractor(true);
		//svgCanvas.setRequestFocusEnabled(true);
		svgCanvas.setDoubleBufferedRendering(true);
		
	    svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
		
		JPanel panel = new JPanel(true);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(svgCanvas);

		svgCanvas.setAlignmentX(Component.LEFT_ALIGNMENT);
		svgCanvas.setAlignmentY(Component.TOP_ALIGNMENT);
		
		svgCanvas.addMouseWheelListener(this);
			
		
		pane = new JScrollPane(panel); 
		pane.setWheelScrollingEnabled(true);
		return pane;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == 0)
			return;
		//TODO scroll the pane
		
		e.consume();
		currentScale -= e.getPreciseWheelRotation()*scaleIncrement;
		svgCanvas.setRenderingTransform(AffineTransform.getScaleInstance(currentScale, currentScale));
		
		svgCanvas.setPreferredSize(new Dimension(
				(int) (origSize.getWidth()*currentScale), 
				(int) (origSize.getHeight()*currentScale)));
		
		pane.getViewport().getView().revalidate();
	}

	public void reloadData() {
		fillCanvas(treeSource);
	}

	@Override
	public void onViewChanged() {
		reloadData();
	}

}
