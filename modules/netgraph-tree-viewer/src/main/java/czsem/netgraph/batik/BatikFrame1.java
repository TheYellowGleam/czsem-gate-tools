/*******************************************************************************
 * Copyright (c) 2016 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.netgraph.batik;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.anim.dom.SVGOMRectElement;
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

public class BatikFrame1 {

	public static void main(String[] args) {
		  // Create a new JFrame.
        JFrame f = new JFrame("Batik");
        BatikFrame1 app = new BatikFrame1(f);

        // Add components to the frame.
        f.getContentPane().add(app.createComponents());

        // Display the frame.
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        f.setSize(400, 400);
        f.setVisible(true);
	}
	
	protected JFrame frame;
	
	protected JSVGCanvas svgCanvas = new JSVGCanvas();

	public BatikFrame1(JFrame frame) {
		this.frame = frame;
	}
	
	
	public JComponent createComponents() {
		final JPanel panel = new JPanel(new BorderLayout());
		
		panel.add(svgCanvas);
		
		svgCanvas.setSVGDocument(createSVGDocument());
		
		return panel;
	}


	protected SVGDocument createSVGDocument() {
		DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
		Document doc = impl.createDocument(svgNS, "svg", null);

		// Get the root element (the 'svg' element).
		Element svgRoot = doc.getDocumentElement();

		// Set the width and height attributes on the root 'svg' element.
		svgRoot.setAttributeNS(null, "width", "400");
		svgRoot.setAttributeNS(null, "height", "450");

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
		
		return (SVGDocument) doc;
	}

	
	
}
