/*******************************************************************************
 * Copyright (c) 2016 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.netgraph.batik;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.w3c.dom.svg.SVGDocument;

import czsem.netgraph.treesource.TreeSourceWithSelectionSupport;
import czsem.netgraph.treesource.TreeSourceWithSelectionSupport.ViewChangedListener;

public class NetgraphViewBatik extends GVTTreeRendererAdapter implements MouseWheelListener, ViewChangedListener {
	
	protected final TreeSourceWithSelectionSupport<?> treeSource;
	public static final double scaleIncrement = 0.1; 
	
	protected double currentScale = 1.0;
	protected Dimension origSize;
	
	private JScrollPane pane;
	
	private final JSVGCanvasUpdated svgCanvas = new JSVGCanvasUpdated();
	private boolean setRenderingTransformLater = false;

	public NetgraphViewBatik(TreeSourceWithSelectionSupport<?> treeSource) {
		this.treeSource = treeSource;
		treeSource.getViewChangedListeners().add(this);
	}

	protected <E> void fillCanvas(TreeSourceWithSelectionSupport<E> treeSource) {
		BatikTreeBuilder<E> b = new BatikTreeBuilder<>(treeSource);
		b.buildNewSvgTree();
		
		origSize = b.getSize();
		svgCanvas.setBackground(BatikTreeBuilder.Color.CANVAS_BACKGROUND);
		svgCanvas.setSVGDocument(b.getDoc());

		//svgCanvas.setPreferredSize(origSize);
		applyScale(false);
	}
	
	public Component initComponent() {
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
		
		svgCanvas.addGVTTreeRendererListener(this);
		svgCanvas.addMouseWheelListener(this);
		
		svgCanvas.addMouseListener(initPopupMenu());
			
		
		pane = new JScrollPane(panel); 
		pane.setWheelScrollingEnabled(true);
		pane.getVerticalScrollBar().setUnitIncrement(16);		
		return pane;
	}

	protected MouseListener initPopupMenu() {
		MouseListener ret = new MouseAdapter() {
			JPopupMenu popup = new JPopupMenu();
			
			{
				UIManager.getIcon("FileView.floppyDriveIcon");
				JMenuItem menuItem;
			    menuItem = new JMenuItem("Zoom in");
			    menuItem.addActionListener(e -> zoom(-1.0));
			    popup.add(menuItem);				
				menuItem = new JMenuItem("Zoom out");
			    menuItem.addActionListener(e -> zoom(1.0));
			    popup.add(menuItem);
				menuItem = new JMenuItem("Zoom reset");
			    menuItem.addActionListener(e -> zoomReset());
			    popup.add(menuItem);
			    popup.addSeparator();
				menuItem = new JMenuItem("Save to SVG file...");
			    menuItem.addActionListener(e -> saveAsSvg());
			    popup.add(menuItem);
			}

			@Override
			public void mousePressed(MouseEvent e) {maybeShowPopup(e);}
			@Override
			public void mouseReleased(MouseEvent e) {maybeShowPopup(e);}

			private void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		};
		return ret;
	}

	protected void applyScale(boolean performImmediateRedraw) {
		if (origSize == null) return;
		
		svgCanvas.setPreferredSize(new Dimension(
				(int) (origSize.getWidth()*currentScale), 
				(int) (origSize.getHeight()*currentScale)));
		
		if (performImmediateRedraw) {
			AffineTransform tr = AffineTransform.getScaleInstance(currentScale, currentScale);
			svgCanvas.setRenderingTransformExclusive(tr, performImmediateRedraw);
		} else {
			setRenderingTransformLater = true;
		}

		pane.getViewport().getView().revalidate();
	}
	
	protected void zoomReset() {
		currentScale = 1.0;
		applyScale(true);
	}
	
	protected void zoom(double delta) {
		currentScale -= delta*scaleIncrement;
		applyScale(true);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
			
			//zoom
			zoom(e.getPreciseWheelRotation());
			
			e.consume();
			
			return;
		}

		//needed to make scroll pane wheel events work
		//probably because of the strange JSVGCanvas component
		if (pane != null) pane.dispatchEvent(e);
	}

	public void reloadData() {
		fillCanvas(treeSource);
	}

	@Override
	public void onViewChanged() {
		reloadData();
	}

	@Override
	public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
		if (! setRenderingTransformLater) return;
		
		UpdateManager um = svgCanvas.getUpdateManager();
		if (um != null) {
			AffineTransform tr = AffineTransform.getScaleInstance(currentScale, currentScale);
			um.getUpdateRunnableQueue().invokeLater(() -> svgCanvas.setRenderingTransformExclusive(tr, false));
			setRenderingTransformLater = false;
		}
	}
	
	protected void saveAsSvg() {
		if (svgCanvas == null) return;
		SVGDocument doc = svgCanvas.getSVGDocument();
		if (doc == null) return;
		
		JFileChooser fc = new JFileChooser();
		fc.setSelectedFile(new File("netgraph_tree.svg"));		
		int returnVal = fc.showSaveDialog(svgCanvas);
		
		if (returnVal != JFileChooser.APPROVE_OPTION) return;

		try {
			Writer w = new OutputStreamWriter(new FileOutputStream(fc.getSelectedFile()), StandardCharsets.UTF_8);
			DOMUtilities.writeDocument(doc, w);
			w.flush();
			w.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
