/*******************************************************************************
 * Copyright (c) 2016 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.netgraph.batik;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.JSVGScrollPane;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

import czsem.netgraph.NetgraphView.Sizing;
import czsem.netgraph.TreeComputation;
import czsem.netgraph.treesource.TreeSource;

public class BatikView<E> {
	private final TreeSource<E> treeSource;
	private final JSVGCanvas svgCanvas = new JSVGCanvas();
	
	public BatikView(TreeSource<E> treeSource) {
		this.treeSource = treeSource;
		
		fillCanvas();
	}

	protected void fillCanvas() {
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
		groupRoot.setAttributeNS(null, "transform", "translate(30,20)");
		

		Element frame = doc.createElementNS(svgNS, "rect");
		frame.setAttributeNS(null, "x", "0");
		frame.setAttributeNS(null, "y", "0");
		frame.setAttributeNS(null, "width", "400");
		frame.setAttributeNS(null, "height", "300");
		frame.setAttributeNS(null, "fill", "gray");
		svgRoot.appendChild(frame);

		svgRoot.appendChild(groupRoot);
		
		
		svgRoot.setAttributeNS(null, "viewBox", "0 0 400 300");
		svgRoot.setAttributeNS(null, "preserveAspectRatio", "xMinYMin");
		//groupRoot.setAttributeNS(null, "preserveAspectRatio", "none");
		
		// Set the width and height attributes on the root 'svg' element.
		svgRoot.setAttributeNS(null, "width", "400");
		svgRoot.setAttributeNS(null, "height", "300");
		
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
		
		new GVTBuilder().build(new BridgeContext(new UserAgentAdapter()), doc);
		
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
		
		svgCanvas.setSVGDocument((SVGDocument) doc);
	}

	public Component getComponent() {
		svgCanvas.setRecenterOnResize(false);
		svgCanvas.setMinimumSize(new Dimension(800, 600));
		svgCanvas.setMaximumSize(new Dimension(800, 600));
		svgCanvas.setPreferredSize(new Dimension(800, 600));
		svgCanvas.setEnableImageZoomInteractor(true);
		//svgCanvas.setRecenterOnResize(true);
		
	    //svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);		
		//JSVGScrollPane scroll = new JSVGScrollPane(svgCanvas);
		//scroll.setScrollbarsAlwaysVisible(true);
		//scroll.setPreferredSize(new Dimension(600, 400));
		
		JPanel panel = new JPanel(true);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(svgCanvas);
		svgCanvas.setAlignmentX(Component.LEFT_ALIGNMENT);
		svgCanvas.setAlignmentY(Component.TOP_ALIGNMENT);
		
		JScrollPane pane = new JScrollPane(panel);
		pane.setWheelScrollingEnabled(true);
		return pane;
	}

}
