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

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.swing.JSVGCanvas;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGLocatable;
import org.w3c.dom.svg.SVGRect;

import czsem.netgraph.NetgraphView.Sizing;
import czsem.netgraph.TreeComputation;
import czsem.netgraph.treesource.TreeSource;

public class BatikView<E> {
	private final TreeSource<E> treeSource;
	
	protected double currentScale = 1.0;
	protected Dimension origSize;
	
	private JScrollPane pane;
	private final JSVGCanvas svgCanvas = new JSVGCanvas() {
		private static final long serialVersionUID = -4362953581038733653L;
		
		@Override
		public void setMySize(Dimension d) {};
		
	};
	
	public BatikView(TreeSource<E> treeSource) {
		this.treeSource = treeSource;
		
		fillCanvasNew();
	}

	protected void fillCanvasNew() {
		BatikTreeBuilder<E> b = new BatikTreeBuilder<>(treeSource);
		b.buildNewSvgTree();
		
		origSize = b.getSize();
		svgCanvas.setSVGDocument(b.getDoc());
	}
	
	protected void fillCanvasOld() {
		TreeComputation<E> cmp = new TreeComputation<>(treeSource);
		cmp.compute();
		
		int[] edges = cmp.collectEdges();
		E[] nodes = cmp.collectNodes();
		int [] nodeOrder = cmp.contNodeOrder();
		
		//compute coordinates
		int[] x = new int[nodes.length];
		int[] y = new int[nodes.length];

		for (int j = 0; j < nodes.length; j++) {
			x[j] = nodeOrder[j] * Sizing.NODE_H_SPACE;
			y[j] = cmp.getDepth(j) * Sizing.NODE_V_SPACE;
		}

		
		DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
		Document doc = impl.createDocument(svgNS, "svg", null);

		// Get the root element (the 'svg' element).
		Element svgRoot = doc.getDocumentElement();

		Element groupRoot = doc.createElementNS(svgNS, "g");

		svgRoot.appendChild(groupRoot);
		
		
		//svgRoot.setAttributeNS(null, "viewBox", "0 0 400 300");
		//svgRoot.setAttributeNS(null, "preserveAspectRatio", "xMinYMin");
		//groupRoot.setAttributeNS(null, "preserveAspectRatio", "none");
		
		// Set the width and height attributes on the root 'svg' element.
		//svgRoot.setAttributeNS(null, "width", "400");
		//svgRoot.setAttributeNS(null, "height", "300");
		
		//edges
		for (int i = 0; i < edges.length; i+=2) {
			int a = edges[i];
			int b = edges[i+1];
			//g.drawLine(x[a]+x_shift, y[a], x[b]+x_shift, y[b]);
			
			// Create line.
			Element line = doc.createElementNS(svgNS, "line");
			line.setAttributeNS(null, "x1", ""+x[a]);
			line.setAttributeNS(null, "y1", ""+y[a]);
			line.setAttributeNS(null, "x2", ""+x[b]);
			line.setAttributeNS(null, "y2", ""+y[b]);
			line.setAttributeNS(null, "stroke", "blue");
			line.setAttributeNS(null, "stroke-width", "5");

			// Attach
			groupRoot.appendChild(line);
		}
		
		//nodes
		for (int j = 0; j < nodes.length; j++) {
			x[j] = nodeOrder[j] * Sizing.NODE_H_SPACE;
			y[j] = cmp.getDepth(j) * Sizing.NODE_V_SPACE;
			// Create circle.
			Element circile = doc.createElementNS(svgNS, "circle");
			circile.setAttributeNS(null, "cx", ""+x[j]);
			circile.setAttributeNS(null, "cy", ""+y[j]);
			circile.setAttributeNS(null, "r", ""+Sizing.NODE_DIAM/2);
			circile.setAttributeNS(null, "stroke", "black");
			circile.setAttributeNS(null, "fill", "red");

			// Attach
			groupRoot.appendChild(circile);
		}

		/*
		

		// Create circle.
		Element circile = doc.createElementNS(svgNS, "circle");
		circile.setAttributeNS(null, "cx", "50");
		circile.setAttributeNS(null, "cy", "50");
		circile.setAttributeNS(null, "r", "10");
		circile.setAttributeNS(null, "stroke", "black");
		circile.setAttributeNS(null, "fill", "red");

		// Attach
		svgRoot.appendChild(circile);

		// Create text.
		//https://www.w3.org/People/Dean/svg/texteffects/index.html
		Element text = doc.createElementNS(svgNS, "text");
		text.setTextContent("my text");
		text.setAttributeNS(null, "style", "font-weight: bold;text-anchor: middle;font-size:24;");
		text.setAttributeNS(null, "x", "50");
		text.setAttributeNS(null, "y", "80");
		text.setAttributeNS(null, "fill", "#99f");
		text.setAttributeNS(null, "stroke", "#99f");
		text.setAttributeNS(null, "stroke-width", "3");

		// Attach 
		svgRoot.appendChild(text);

		// Create text stroke
		Element textStroke = doc.createElementNS(svgNS, "text");
		textStroke.setTextContent("my text");
		textStroke.setAttributeNS(null, "style", "font-weight: bold;text-anchor: middle;font-size:24;");
		textStroke.setAttributeNS(null, "x", "50");
		textStroke.setAttributeNS(null, "y", "80");
		textStroke.setAttributeNS(null, "fill", "black");

		// Attach 
		svgRoot.appendChild(textStroke);
		
		
		SVGLocatable loc = (SVGLocatable) text;
		SVGRect bbox = loc.getBBox();
		System.err.println("c "+text.getClass());
		System.err.println("h "+loc.getBBox().getHeight());
		System.err.println("w "+loc.getBBox().getWidth());
		
		// Create the rectangle.
		SVGOMRectElement rectangle = (SVGOMRectElement) doc.createElementNS(svgNS, "rect");
		rectangle.setAttributeNS(null, "x", ""+bbox.getX());
		rectangle.setAttributeNS(null, "y", ""+bbox.getY());
		rectangle.setAttributeNS(null, "width", ""+bbox.getWidth());
		rectangle.setAttributeNS(null, "height", ""+bbox.getHeight());
		rectangle.setAttributeNS(null, "stroke", "green");
		rectangle.setAttributeNS(null, "fill", "none");
		
		// Attach the rectangle to the root 'svg' element.
		svgRoot.appendChild(rectangle);		
		*/

		new GVTBuilder().build(new BridgeContext(new UserAgentAdapter()), doc);

		SVGLocatable loc = (SVGLocatable) svgRoot;
		SVGRect box = loc.getBBox();
		
		origSize = new Dimension((int)(box.getWidth()+4), (int)(box.getHeight()+4));
		
		Element frame = doc.createElementNS(svgNS, "rect");
		frame.setAttributeNS(null, "x", "0");
		frame.setAttributeNS(null, "y", "0");
		frame.setAttributeNS(null, "width", ""+origSize.getWidth());
		frame.setAttributeNS(null, "height", ""+origSize.getHeight());
		frame.setAttributeNS(null, "fill", "lightgray");
		//frame.setAttributeNS(null, "stroke-width", "10");
		//frame.setAttributeNS(null, "stroke", "gray");
		svgRoot.insertBefore(frame, groupRoot);
		
		groupRoot.setAttributeNS(null, "transform", "translate("+(2-box.getX())+","+(2-box.getY())+")");
		
		svgCanvas.setSVGDocument((SVGDocument) doc);
	}

	public Component getComponent() {
		svgCanvas.setRecenterOnResize(false);
		//svgCanvas.setMinimumSize(new Dimension(800, 600));
		svgCanvas.setPreferredSize(origSize);
		//svgCanvas.setEnableImageZoomInteractor(true);
		//svgCanvas.setRequestFocusEnabled(true);
		svgCanvas.setDoubleBufferedRendering(true);
		
		//svgCanvas.setRecenterOnResize(true);
		
	    //svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);		
		//JSVGScrollPane scroll = new JSVGScrollPane(svgCanvas);
		//scroll.setScrollbarsAlwaysVisible(true);
		//scroll.setPreferredSize(new Dimension(600, 400));
		
		JPanel panel = new JPanel(true);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(svgCanvas);
		//panel.add(new JButton("Můůůůj douhýý textttttt"));
		svgCanvas.setAlignmentX(Component.LEFT_ALIGNMENT);
		svgCanvas.setAlignmentY(Component.TOP_ALIGNMENT);
		
		svgCanvas.addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == 0)
					return;
				//TODO scroll the pane
				
				e.consume();
				currentScale -= e.getPreciseWheelRotation()*0.2;
				svgCanvas.setRenderingTransform(AffineTransform.getScaleInstance(currentScale, currentScale));
				
				svgCanvas.setPreferredSize(new Dimension(
						(int) (origSize.getWidth()*currentScale), 
						(int) (origSize.getHeight()*currentScale)));
				
				pane.getViewport().getView().revalidate();
			}
		});
		
		pane = new JScrollPane(panel); 
		pane.setWheelScrollingEnabled(true);
		return pane;
	}

}
