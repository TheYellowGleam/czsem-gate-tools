/*******************************************************************************
 * Copyright (c) 2016 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.netgraph.batik;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import czsem.netgraph.NetgraphFrameAwtGraphics.TestSource;

public class NetgraphFrameBatik {

	public static void main(String[] args) {
		JFrame frame = new JFrame("NetgraphFrameBatik");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		NetgraphViewBatik view = new NetgraphViewBatik(new TestSource());
        
		frame.setLocationRelativeTo(null);		
		frame.setSize(800, 600);
		frame.getContentPane().add(view.initComponent());
		
		view.reloadData();

		frame.pack();
		frame.setVisible(true);
	}

}
