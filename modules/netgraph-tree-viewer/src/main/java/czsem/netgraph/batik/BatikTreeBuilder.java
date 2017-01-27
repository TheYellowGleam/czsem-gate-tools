package czsem.netgraph.batik;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGLocatable;
import org.w3c.dom.svg.SVGRect;

import czsem.netgraph.TreeComputation;
import czsem.netgraph.treesource.TreeSource;
import czsem.netgraph.treesource.TreeSource.NodeLabel;

public class BatikTreeBuilder<E> {
	
	public static final class Sizing {

		public static final int NODE_DIAM = 9; 

		public static final int BORDER = 8;
		public static final float NODE_V_SPACE = (int)(NODE_DIAM*1.3);
		public static final int NODE_H_SPACE = NODE_DIAM;
		public static final int TEXT_H_SPACE = NODE_DIAM;

		public static final int LINE_HEIGHT = 13; 
		public static final int FIRST_LINE_Y = (int)(NODE_DIAM*2.0);
		
		public static final String FONT_SIZE = "12";
		public static final String FONT_STROKE = "5";
		public static final float TEXT_OFFSET_MIDDLE = 1.5f;

		public static final String EDGE_STROKE = "2";

		public static final String NODE_STROKE = "2";
	}
	
	public static final class Color {
		public static final String TEXT_STROKE = "white";
		public static final String TEXT_STROKE_OPACITY = "0.9";
		public static final String TEXT = "#333";
		public static final String NODE_STROKE = "#555555";
		public static final String NODE_FILL = "#4488ff";
		public static final String EDGE_STROKE = "#336699";
		public static final String FRAME_FILL = "white";
		public static final String FRAME_OPACITY = "0";
		public static final java.awt.Color CANVAS_BACKGROUND = java.awt.Color.WHITE;
		public static final String NODE_FILL_SELECTED = "#EFC94C";
		public static final String NODE_STROKE_SELECTED = "#AD1B32";
		
		
		
		
	}
	
	private final TreeSource<E> treeSource;
	private final SelectionHandlder<E> selHandler;
		
	private float[] x;
	private float[] y;
	private Integer[] sortedNodes;
	private TreeComputation<E> cmp;
	
	public static final String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
	
	private SVGDocument doc;
	protected Dimension origSize;
	private E[] srcNodes;
	private Element[] svgNodes;
	private Element mainGroupRoot;
	
	public static class SelectionHandlder<E> {
		protected E[] nodes; 
		protected Element[] circles;
		protected int slectedNodeIndex = -1;
		
		public void fireSlectionChanged(int nodeIndex) {
			if (slectedNodeIndex >= 0 && slectedNodeIndex < circles.length) {
				circles[slectedNodeIndex].setAttributeNS(null, "fill", Color.NODE_FILL);
				circles[slectedNodeIndex].setAttributeNS(null, "stroke", Color.NODE_STROKE);
			}
			
			slectedNodeIndex = nodeIndex;
			
			circles[slectedNodeIndex].setAttributeNS(null, "fill", Color.NODE_FILL_SELECTED);
			circles[slectedNodeIndex].setAttributeNS(null, "stroke", Color.NODE_STROKE_SELECTED);
		}

		public void discardSeletion() {
			slectedNodeIndex = -1;
		}
		
		public E[] getNodes() {return nodes;}
		public void setNodes(E[] nodes) {this.nodes = nodes;}
		public Element[] getCircles() {return circles;}
		public void setCircles(Element[] circles) {this.circles = circles;}
	}
	
	public static class SelectNodeEvent implements EventListener {

		private SelectionHandlder<?> selectionHandlder;
		private int nodeIndex;

		public SelectNodeEvent(SelectionHandlder<?> selectionHandlder, int nodeIndex) {
			this.selectionHandlder = selectionHandlder;
			this.nodeIndex = nodeIndex;
		}

		@Override
		public void handleEvent(Event evt) {
			selectionHandlder.fireSlectionChanged(nodeIndex);
		}
		
	}
	
	public BatikTreeBuilder(SelectionHandlder<E> selHandler, TreeSource<E> treeSource) {
		this.selHandler = selHandler;
		this.treeSource = treeSource;
	}
	
	public void buildNewSvgTree() {
		cmp = new TreeComputation<>(treeSource);
		cmp.compute();
		
		int[] edges = cmp.collectEdges();
		srcNodes = cmp.collectNodes();
		sortedNodes = cmp.computeSortedNodes();
		
		selHandler.discardSeletion();
		selHandler.setNodes(srcNodes);

		
		svgNodes = new Element[srcNodes.length];
		selHandler.setCircles(new Element[srcNodes.length]);


		//init batik
		
		DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
		doc = (SVGDocument) impl.createDocument(svgNS, "svg", null);
		
		// Get the root element (the 'svg' element).
		Element svgRoot = doc.getDocumentElement();

		mainGroupRoot = doc.createElementNS(svgNS, "g");
		svgRoot.appendChild(mainGroupRoot);

		Element edgesGroup = doc.createElementNS(svgNS, "g");
		mainGroupRoot.appendChild(edgesGroup);
		

		
		setupDynamicSvgBridge(doc);
		
		//nodes
		for (int j = 0; j < srcNodes.length; j++) {
			Element n = createSvgNode(j);
			svgNodes[j] = n;
		}
		
		
		//compute coordinates
		//prepare sizes 
		SVGRect[] svgNodeBoxes = Arrays.stream(svgNodes)
				.map(BatikTreeBuilder::getBBox)
				.toArray(SVGRect[]::new);
		
		float maxHeightPerDepth[] = new float[cmp.getMaxDepth()+1];
		for (int i = 0; i < svgNodes.length; i++) {
			int d = cmp.getDepth(i);
			float h = svgNodeBoxes[i].getHeight();
			maxHeightPerDepth[d] = Math.max(maxHeightPerDepth[d], h);
		}

		float yOffsetForDepth[] = new float[cmp.getMaxDepth()+1];
		for (int d = 1; d < yOffsetForDepth.length; d++) {
			yOffsetForDepth[d] = Sizing.NODE_V_SPACE + yOffsetForDepth[d-1] + maxHeightPerDepth[d-1];
		}

		float lastDirtyXOffsetForDepth[] = new float[cmp.getMaxDepth()+1];
		for (int i = 0; i < lastDirtyXOffsetForDepth.length; i++) {
			lastDirtyXOffsetForDepth[i] = -Float.MAX_VALUE;
		}
		
		
		//compute
		//x
		//the initial position of each node is centered around 0 x coordinate 
		x = new float[srcNodes.length];
		
		int n0 = sortedNodes[0];
		float w0 = svgNodeBoxes[n0].getWidth();
		float x0 = svgNodeBoxes[n0].getX();
		x[n0] = 0f;
		lastDirtyXOffsetForDepth[cmp.getDepth(n0)] = x0 + w0;  
		
		for (int j = 1; j < srcNodes.length; j++) {
			float prevX = x[sortedNodes[j-1]];
			int n = sortedNodes[j];
			
			//System.err.println(srcNodes[n] +" "+ treeSource.getLabels(srcNodes[n]).get(0).getMiddle());
			
			float width = svgNodeBoxes[n].getWidth();
			float origXN = svgNodeBoxes[n].getX();
			int d = cmp.getDepth(n);
			
			x[n] = prevX + Sizing.NODE_H_SPACE;
			
			if (x[n]+origXN < lastDirtyXOffsetForDepth[d] + Sizing.TEXT_H_SPACE) {
				x[n] = lastDirtyXOffsetForDepth[d] - origXN + Sizing.TEXT_H_SPACE;
			}
			
			lastDirtyXOffsetForDepth[d] = x[n] + width + origXN;
		}

		//y
		y = new float[srcNodes.length];

		for (int j = 0; j < srcNodes.length; j++) {
			
			y[j] = yOffsetForDepth[cmp.getDepth(j)];
				
			svgNodes[j].setAttributeNS(null, "transform", "translate("+x[j]+","+y[j]+")");
		}
		
		
		
		//draw edges
		for (int i = 0; i < edges.length; i+=2) {
			int a = edges[i];
			int b = edges[i+1];
			
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
		
		
		//get the final size
		SVGRect box = getBBox(svgRoot);
		
		origSize = new Dimension((int)(box.getWidth()+Sizing.BORDER*2), (int)(box.getHeight()+Sizing.BORDER*2));
		
		Element frame = buildElem("rect")
				.attr("x", "0")
				.attr("y", "0")
				.attr("width", origSize.getWidth())
				.attr("height", origSize.getHeight())
				.attr("fill", Color.FRAME_FILL)
				.attr("opacity", Color.FRAME_OPACITY)				
				.get();
		svgRoot.insertBefore(frame, mainGroupRoot);
		
		float trX = Sizing.BORDER-box.getX();
		float trY = Sizing.BORDER-box.getY();
		mainGroupRoot.setAttributeNS(null, "transform", "translate("+trX+","+trY+")");
	}
	
	public static void setupDynamicSvgBridge(SVGDocument doc) {
		UserAgentAdapter userAgent = new UserAgentAdapter();
	    DocumentLoader loader = new DocumentLoader(userAgent);
	    BridgeContext ctx = new BridgeContext(userAgent, loader); 
	    ctx.setDynamicState(BridgeContext.DYNAMIC);
	    GVTBuilder builder = new GVTBuilder();
	    builder.build(ctx, doc); 		
	}
	
	public static SVGRect getBBox(Node elem) {  
		SVGLocatable loc = (SVGLocatable) elem;
		SVGRect box = loc.getBBox();
		return box;
	}
	
	protected Element createSvgNode(int j) {
		Element nodeGroup = buildElem("g").get();
		mainGroupRoot.appendChild(nodeGroup);
		Element strokeGroup = buildElem("g").get();
		nodeGroup.appendChild(strokeGroup);
		
		// Create circle.
		Element circile = buildElem("circle")
				.attr("cx", 	0)
				.attr("cy", 	0)
				.attr("r", 		Sizing.NODE_DIAM/2)
				.attr("stroke", Color.NODE_STROKE)
				.attr("stroke-width", Sizing.NODE_STROKE)
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
		
		
		//addEvent
		selHandler.getCircles()[j] = circile;
		EventTarget t = (EventTarget) nodeGroup;
		t.addEventListener("click", new SelectNodeEvent(selHandler, j), true);
		
		return nodeGroup;
	}

	protected void appendLabel(Element nodeGroup, Element strokeGroup, int line, NodeLabel nodeLabel) {
		int txtY = Sizing.FIRST_LINE_Y + line * Sizing.LINE_HEIGHT;
		
		float middleOffset = Sizing.TEXT_OFFSET_MIDDLE;

		String middleText = nodeLabel.getMiddle();
		
		if (middleText != null && !middleText.trim().isEmpty()) {
			Element txtElem = createNiceText(nodeGroup, strokeGroup, nodeLabel.getMiddle(),	"middle", 0, txtY);
			
			if (txtElem != null)
				middleOffset += getTextSize(txtElem)/2;
		}
		
		createNiceText(nodeGroup, strokeGroup, nodeLabel.getLeftPart(), "end",  -middleOffset, txtY);
		createNiceText(nodeGroup, strokeGroup, nodeLabel.getRightPart(),"start", middleOffset, txtY);
	}
	
	protected float getTextSize(Element txtElem) {
		SVGRect box = getBBox(txtElem);
		return box.getWidth();
	}

	protected Element createNiceText(Element nodeGroup, Element strokeGroup, String textContent, String anchor, float x, int y) {
		if (textContent == null || textContent.trim().isEmpty()) return null;
		
		String xStr = Float.toString(x);
		String yStr = Integer.toString(y);
		
		// Create text stroke
		//https://www.w3.org/People/Dean/svg/texteffects/index.html
		ElemBuilder textStroke = createTextElem(doc, textContent, anchor, xStr, yStr);
		textStroke.attr("fill", Color.TEXT_STROKE);
		textStroke.attr("stroke", Color.TEXT_STROKE);
		textStroke.attr("stroke-width", Sizing.FONT_STROKE);
		textStroke.attr("opacity", Color.TEXT_STROKE_OPACITY);				

		// Attach 
		strokeGroup.appendChild(textStroke.get());

		// Create text.
		ElemBuilder text = createTextElem(doc, textContent, anchor, xStr, yStr);
		text.attr("fill", Color.TEXT);

		// Attach 
		nodeGroup.appendChild(text.get());
		
		return text.get();
	}
	
	public static ElemBuilder createTextElem(SVGDocument doc, String textContent, String anchor, String xStr, String yStr) {
		ElemBuilder text = new ElemBuilder(doc.createElementNS(svgNS, "text"));
		text.textContent(textContent);
		//text.attr("font-weight", "bold");
		text.attr("alignment-baseline", "text-after-edge");
		text.attr("text-anchor", anchor);
		text.attr("font-family", "Arial");
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
