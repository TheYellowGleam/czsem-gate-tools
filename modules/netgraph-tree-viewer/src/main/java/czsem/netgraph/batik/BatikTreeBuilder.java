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
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGLocatable;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGSVGElement;

import czsem.netgraph.TreeComputation;
import czsem.netgraph.treesource.TreeSource;
import czsem.netgraph.treesource.TreeSource.NodeLabel;

public class BatikTreeBuilder<E> {
	
	public static final class Sizing {

		public static final int NODE_DIAM = 15; 

		public static final int BORDER = 8;
		public static final int NODE_V_SPACE = NODE_DIAM*2;
		public static final int NODE_H_SPACE = NODE_DIAM*2;

		public static final int LINE_HEIGHT = 18; 
		public static final int FIRST_LINE_Y = (int)(NODE_DIAM*1.7);
		
		public static final String FONT_SIZE = "16";
		public static final String FONT_STROKE = "3";
		public static final float TEXT_OFFSET_MIDDLE = 1.5f;

		public static final String EDGE_STROKE = "5";
	}
	
	public static final class Color {
		public static final String TEXT_STROKE = "#99f";
		public static final String TEXT = "black";
		public static final String NODE_STROKE = "black";
		public static final String NODE_FILL = "red";
		public static final String FRAME_FILL = "lightgray";
		public static final String EDGE_STROKE = "blue";
		
	}
	
	private final TreeSource<E> treeSource;
	private int[] x;
	private int[] y;
	private int[] nodeOrder;
	private TreeComputation<E> cmp;
	
	public static final String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
	
	private SVGDocument doc;
	protected Dimension origSize;
	private E[] srcNodes;
	private SVGDocument tmpDoc;
	private Element[] svgNodes;

	public BatikTreeBuilder(TreeSource<E> treeSource) {
		this.treeSource = treeSource;
	}
	
	public void buildNewSvgTree() {
		cmp = new TreeComputation<>(treeSource);
		cmp.compute();
		
		int[] edges = cmp.collectEdges();
		srcNodes = cmp.collectNodes();
		nodeOrder = cmp.contNodeOrder();
		
		svgNodes = new Element[srcNodes.length];

		//init batik
		
		DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
		doc = (SVGDocument) impl.createDocument(svgNS, "svg", null);
		tmpDoc = (SVGDocument) impl.createDocument(svgNS, "svg", null);
		
		// Get the root element (the 'svg' element).
		Element svgRoot = doc.getDocumentElement();

		Element groupRoot = doc.createElementNS(svgNS, "g");
		svgRoot.appendChild(groupRoot);

		Element edgesGroup = doc.createElementNS(svgNS, "g");
		groupRoot.appendChild(edgesGroup);
		
		
		//nodes
		for (int j = 0; j < srcNodes.length; j++) {
			Element n = createSvgNode(j);
			svgNodes[j] = n;
			groupRoot.appendChild(n);
		}

		//draw SvgNodes nodes
		buildSvgImage(doc);
		
		
		
		//compute coordinates
		x = new int[srcNodes.length];
		y = new int[srcNodes.length];

		for (int j = 0; j < srcNodes.length; j++) {
			x[j] = nodeOrder[j] * Sizing.NODE_H_SPACE;
			y[j] = cmp.getDepth(j) * Sizing.NODE_V_SPACE;
			svgNodes[j].setAttributeNS(null, "transform", "translate("+x[j]+","+y[j]+")");
		}
		
		//draw edges
		for (int i = 0; i < edges.length; i+=2) {
			int a = edges[i];
			int b = edges[i+1];
			//g.drawLine(x[a]+x_shift, y[a], x[b]+x_shift, y[b]);
			
			// Create line.
			Element line = buildElem("line")
				.attr("x1", x[a])
				.attr("y1", y[a])
				.attr("x2", x[b])
				.attr("y2", y[b])
				.attr("stroke", 	  Color.EDGE_STROKE)
				.attr("stroke-width", Sizing.EDGE_STROKE)
				.get();

			// Attach
			edgesGroup.appendChild(line);
		}
		
		
		//compute the final size
		buildSvgImage(doc);
		SVGRect box = getBBox(svgRoot);
		
		origSize = new Dimension((int)(box.getWidth()+Sizing.BORDER*2), (int)(box.getHeight()+Sizing.BORDER*2));
		
		Element frame = buildElem("rect")
				.attr("x", "0")
				.attr("y", "0")
				.attr("width", origSize.getWidth())
				.attr("height", origSize.getHeight())
				.attr("fill", Color.FRAME_FILL)
				.get();
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
	
	protected Element createSvgNode(int j) {
		Element nodeGroup = buildElem("g").get();
		Element strokeGroup = buildElem("g").get();
		nodeGroup.appendChild(strokeGroup);
		
		// Create circle.
		Element circile = buildElem("circle")
				.attr("cx", 	0)
				.attr("cy", 	0)
				.attr("r", 		Sizing.NODE_DIAM/2)
				.attr("stroke", Color.NODE_STROKE)
				.attr("fill", 	Color.NODE_FILL)
				.get(); 
				
		nodeGroup.appendChild(circile);
		
		
		//labels
		List<NodeLabel> labels = treeSource.getLabels(srcNodes[j]);
		int line = 0;
		for (NodeLabel nodeLabel : labels) {
			appendLabel(nodeGroup, strokeGroup, line, nodeLabel);
			line++;
		}
		
		return nodeGroup;
	}

	protected void appendLabel(Element nodeGroup, Element strokeGroup, int line, NodeLabel nodeLabel) {
		int txtY = Sizing.FIRST_LINE_Y + line * Sizing.LINE_HEIGHT;
		
		float middleOffset = Sizing.TEXT_OFFSET_MIDDLE;

		String middleText = nodeLabel.getMiddle();
		if (middleText != null && !middleText.trim().isEmpty()) {
			middleOffset += getTextSize(middleText)/2;
		}
		
		createNiceText(nodeGroup, strokeGroup, nodeLabel.getMiddle(),	"middle",0,            txtY);
		createNiceText(nodeGroup, strokeGroup, nodeLabel.getLeftPart(), "end",  -middleOffset, txtY);
		createNiceText(nodeGroup, strokeGroup, nodeLabel.getRightPart(),"start", middleOffset, txtY);
	}
	
	protected float getTextSize(String middleText) {
		SVGSVGElement root = tmpDoc.getRootElement();
		
		Element textElem = createTextElem(tmpDoc, middleText, "middle", "0", "0").get();
		root.appendChild(textElem);
		
		buildSvgImage(tmpDoc);
		SVGRect box = getBBox(textElem);
		root.removeChild(textElem);
		
		return box.getWidth();
	}

	protected void createNiceText(Element nodeGroup, Element strokeGroup, String textContent, String anchor, float x, int y) {
		if (textContent == null || textContent.trim().isEmpty()) return;
		
		String xStr = Float.toString(x);
		String yStr = Integer.toString(y);
		
		// Create text stroke
		//https://www.w3.org/People/Dean/svg/texteffects/index.html
		ElemBuilder textStroke = createTextElem(doc, textContent, anchor, xStr, yStr);
		textStroke.attr("fill", Color.TEXT_STROKE);
		textStroke.attr("stroke", Color.TEXT_STROKE);
		textStroke.attr("stroke-width", Sizing.FONT_STROKE);

		// Attach 
		strokeGroup.appendChild(textStroke.get());

		// Create text.
		ElemBuilder text = createTextElem(doc, textContent, anchor, xStr, yStr);
		text.attr("fill", Color.TEXT);

		// Attach 
		nodeGroup.appendChild(text.get());
	}
	
	public static ElemBuilder createTextElem(SVGDocument doc, String textContent, String anchor, String xStr, String yStr) {
		ElemBuilder text = new ElemBuilder(doc.createElementNS(svgNS, "text"));
		text.textContent(textContent);
		text.attr("font-weight", "bold");
		text.attr("alignment-baseline", "text-after-edge");
		text.attr("text-anchor", anchor);
		text.attr("font-size", Sizing.FONT_SIZE );
		
		text.attr("x", xStr);
		text.attr("y", yStr);
		
		return text;
	}

	public SVGDocument getDoc() {
		return doc;
	}

	public Dimension getSize() {
		return origSize;
	}
	
	public ElemBuilder buildElem(String elemName) {
		return new ElemBuilder(doc.createElementNS(svgNS, elemName));
	}
	
	public static class ElemBuilder {
		Element el;

		public ElemBuilder(Element el) {
			this.el = el;
		}

		public ElemBuilder textContent(String textContent) {
			el.setTextContent(textContent);
			return this;
		}

		public ElemBuilder attr(String attrName, Object attrValue) {
			el.setAttributeNS(null, attrName, attrValue.toString());
			return this;
		}

		public Element get() {
			return el;
		}
		
	}
	
}
