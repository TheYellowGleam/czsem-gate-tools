package czsem.gate.plugins;

import javax.swing.JTabbedPane;

import czsem.netgraph.NetgraphQueryDesigner;
import czsem.netgraph.NetgraphResultsBrowser;
import czsem.netgraph.NetgraphTreeVisualize;
import czsem.netgraph.NetgraphQueryConfig;
import czsem.netgraph.util.DialogBasedAnnotationEditor;

public class NetgraphTreeViewer extends DialogBasedAnnotationEditor {
	private static final long serialVersionUID = -7161633338395041139L;
	
	private JTabbedPane tabs; 
	
	private NetgraphTreeVisualize tabViewer;
	private NetgraphQueryDesigner tabQuery;
	private NetgraphResultsBrowser tabResults;
	private NetgraphQueryConfig tabConfig;

	@Override
	public String getTitle() { return "Netgraph Tree Viewer"; }

	@Override
	protected void initGui() {
		tabs = new JTabbedPane(JTabbedPane.BOTTOM);
		
	}

}
