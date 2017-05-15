package czsem.netgraph.batik;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;

import org.apache.batik.swing.JSVGCanvas;

public class JSVGCanvasUpdated extends JSVGCanvas {
	private static final long serialVersionUID = -2525482300192603444L;
	
	@Override
	public void setMySize(Dimension d) {
		// disabled
	}

	@Override
	public void setRenderingTransform(AffineTransform at, boolean performRedraw) {
		// disabled
		//super.setRenderingTransform(at, performRedraw);
	}

	public void setRenderingTransformExclusive(AffineTransform tr, boolean performRedraw) {
		// allowed
		super.setRenderingTransform(tr, performRedraw);
	};

	
	
}
