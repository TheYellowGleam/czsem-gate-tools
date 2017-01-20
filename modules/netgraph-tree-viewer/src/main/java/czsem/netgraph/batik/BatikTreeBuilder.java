/*******************************************************************************
 * Copyright (c) 2017 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.netgraph.batik;

import java.awt.Dimension;
import java.util.List;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGLocatable;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGSVGElement;

import czsem.netgraph.TreeComputation;
import czsem.netgraph.treesource.TreeSource;
import czsem.netgraph.treesource.TreeSource.NodeLabel;

public class BatikTreeBuilder<E> {
	
	public static final class Sizing {

		public static final int BORDER = 8;
		public static final int NODE_V_SPACE = 60;
		public static final int NODE_H_SPACE = 40;
		public static final int NODE_DIAM = 15; 

		public static final int LINE_HEIGHT = 20; 
		public static final int FIRST_LINE_Y = (int)(NODE_DIAM*1.7);
		
		public static final String FONT_SIZE = "16";
		public static final String FONT_STROKE = "3";
		public static final float TEXT_OFFSET_MIDDLE = 1.5f;
	}
	
	public static final class Color {
		public static final String TEXT_STROKE = "#99f";
		public static final String TEXT = "black";
		public static final String NODE_STROKE = "black";
		public static final String NODE_FILL = "red";
		public static final String FRAME_FILL = "lightgray";
		
	}
	
	private final TreeSource<E> treeSource;
	private int[] x;
	private int[] y;
	private int[] nodeOrder;
	private TreeComputation<E> cmp;
	
	public static final String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
	
	private SVGDocument doc;
	protected Dimension origSize;
	private E[] nodes;
	private SVGDocument tmpDoc;

	public BatikTreeBuilder(TreeSource<E> treeSource) {
		this.treeSource = treeSource;
	}
	
	public void buildNewSvgTree() {
		cmp = new TreeComputation<>(treeSource);
		cmp.compute();
		
		int[] edges = cmp.collectEdges();
		nodes = cmp.collectNodes();
		nodeOrder = cmp.contNodeOrder();
		
		//compute coordinates
		x = new int[nodes.length];
		y = new int[nodes.length];

		for (int j = 0; j < nodes.length; j++) {
			x[j] = nodeOrder[j] * Sizing.NODE_H_SPACE;
			y[j] = cmp.getDepth(j) * Sizing.NODE_V_SPACE;
		}
		
		
		//init batik
		
		DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
		doc = (SVGDocument) impl.createDocument(svgNS, "svg", null);
		tmpDoc = (SVGDocument) impl.createDocument(svgNS, "svg", null);
		
		// Get the root element (the 'svg' element).
		Element svgRoot = doc.getDocumentElement();

		Element groupRoot = doc.createElementNS(svgNS, "g");

		svgRoot.appendChild(groupRoot);
		
		
		
		
		//nodes
		for (int j = 0; j < nodes.length; j++) {
			groupRoot.appendChild(createNode(j));
		}
		
		
		
		//compute the final size
		buildSvgImage(doc);
		SVGRect box = getBBox(svgRoot);
		
		origSize = new Dimension((int)(box.getWidth()+Sizing.BORDER*2), (int)(box.getHeight()+Sizing.BORDER*2));
		
		Element frame = doc.createElementNS(svgNS, "rect");
		frame.setAttributeNS(null, "x", "0");
		frame.setAttributeNS(null, "y", "0");
		frame.setAttributeNS(null, "width", ""+origSize.getWidth());
		frame.setAttributeNS(null, "height", ""+origSize.getHeight());
		frame.setAttributeNS(null, "fill", Color.FRAME_FILL);
		//frame.setAttributeNS(null, "stroke-width", "10");
		//frame.setAttributeNS(null, "stroke", "gray");
		svgRoot.insertBefore(frame, groupRoot);
		
		float trX = Sizing.BORDER-box.getX();
		float trY = Sizing.BORDER-box.getY();
		groupRoot.setAttributeNS(null, "transform", "translate("+trX+","+trY+")");
	}
	
	public static void buildSvgImage(SVGDocument doc) {
		//build the image
		new GVTBuilder().build(new BridgeContext(new UserAgentAdapter()), doc);
	}
	
	public SVGRect getBBox(Element elem) {  

		SVGLocatable loc = (SVGLocatable) elem;
		SVGRect box = loc.getBBox();
		
		return box;
	}
	
	protected Node createNode(int j) {
		Element nodeGroup = doc.createElementNS(svgNS, "g");
		
		x[j] = nodeOrder[j] * Sizing.NODE_H_SPACE;
		y[j] = cmp.getDepth(j) * Sizing.NODE_V_SPACE;
		
		//TODO the final transform should be computed from the final sizes - including texts 
		nodeGroup.setAttributeNS(null, "transform", "translate("+x[j]+","+y[j]+")");

		// Create circle.
		Element circile = doc.createElementNS(svgNS, "circle");
		circile.setAttributeNS(null, "cx", "0");
		circile.setAttributeNS(null, "cy", "0");
		circile.setAttributeNS(null, "r", ""+Sizing.NODE_DIAM/2);
		circile.setAttributeNS(null, "stroke", Color.NODE_STROKE);
		circile.setAttributeNS(null, "fill", Color.NODE_FILL);
		nodeGroup.appendChild(circile);
		
		
		//labels
		List<NodeLabel> labels = treeSource.getLabels(nodes[j]);
		int line = 0;
		for (NodeLabel nodeLabel : labels) {
			appendLabel(nodeGroup, line, nodeLabel);
			line++;
		}
		
		return nodeGroup;
	}

	protected void appendLabel(Element nodeGroup, int line, NodeLabel nodeLabel) {
		int txtY = Sizing.FIRST_LINE_Y + line * Sizing.LINE_HEIGHT;
		
		float middleOffset = Sizing.TEXT_OFFSET_MIDDLE;

		String middleText = nodeLabel.getMiddle();
		if (middleText != null && !middleText.trim().isEmpty()) {
			middleOffset += getTextSize(middleText)/2;
		}
		
		
		createNiceText(nodeLabel.getMiddle(),    nodeGroup, "middle", 0,           txtY);
		createNiceText(nodeLabel.getLeftPart(),  nodeGroup, "end",    -middleOffset, txtY);
		createNiceText(nodeLabel.getRightPart(), nodeGroup, "start",  middleOffset,  txtY);
	}
	
	protected float getTextSize(String middleText) {
		SVGSVGElement root = tmpDoc.getRootElement();
		
		Element textElem = createTextElem(tmpDoc, middleText, "middle", "0", "0");
		root.appendChild(textElem);
		
		buildSvgImage(tmpDoc);
		SVGRect box = getBBox(textElem);
		root.removeChild(textElem);
		
		return box.getWidth();
	}

	protected void createNiceText(String textContent, Element nodeGroup, String anchor, float x, int y) {
		if (textContent == null || textContent.trim().isEmpty()) return;
		
		String xStr = Float.toString(x);
		String yStr = Integer.toString(y);
		
		// Create text stroke
		//https://www.w3.org/People/Dean/svg/texteffects/index.html
		Element textStroke = createTextElem(doc, textContent, anchor, xStr, yStr);
		textStroke.setAttributeNS(null, "fill", Color.TEXT_STROKE);
		textStroke.setAttributeNS(null, "stroke", Color.TEXT_STROKE);
		textStroke.setAttributeNS(null, "stroke-width", Sizing.FONT_STROKE);

		// Attach 
		nodeGroup.appendChild(textStroke);

		// Create text.
		Element text = createTextElem(doc, textContent, anchor, xStr, yStr);
		text.setAttributeNS(null, "fill", Color.TEXT);

		// Attach 
		nodeGroup.appendChild(text);
	}
	
	public static Element createTextElem(SVGDocument doc, String textContent, String anchor, String xStr, String yStr) {
		Element text = doc.createElementNS(svgNS, "text");
		text.setTextContent(textContent);
		text.setAttributeNS(null, "font-weight", "bold");
		text.setAttributeNS(null, "alignment-baseline", "text-after-edge");
		text.setAttributeNS(null, "text-anchor", anchor);
		text.setAttributeNS(null, "font-size", Sizing.FONT_SIZE );
		
		text.setAttributeNS(null, "x", xStr);
		text.setAttributeNS(null, "y", yStr);
		
		return text;
	}

	public SVGDocument getDoc() {
		return doc;
	}

	public Dimension getSize() {
		return origSize;
	}


}
