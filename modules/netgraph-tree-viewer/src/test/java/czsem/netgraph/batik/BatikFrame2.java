/*******************************************************************************
 * Copyright (c) 2016 Datlowe and/or its affiliates. All rights reserved.
 ******************************************************************************/
package czsem.netgraph.batik;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import czsem.netgraph.NetgraphFrame.TestSource;

public class BatikFrame2 {

	public static void main(String[] args) {
		JFrame frame = new JFrame("BatikFrame2");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		BatikView view = new BatikView(new TestSource());
        
		frame.setLocationRelativeTo(null);		
		frame.setSize(800, 600);
		frame.getContentPane().add(view.initComponent());
		
		view.reloadData();

		frame.pack();
		frame.setVisible(true);
	}

}
