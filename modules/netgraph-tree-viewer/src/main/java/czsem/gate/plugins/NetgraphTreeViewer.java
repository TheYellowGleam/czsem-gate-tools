package czsem.gate.plugins;

import java.util.Collections;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;

import javax.swing.JDialog;
import javax.swing.JTabbedPane;

import czsem.gate.utils.GateAwareTreeIndexExtended;
import czsem.netgraph.NetgraphQueryDesigner;
import czsem.netgraph.NetgraphResultsBrowser;
import czsem.netgraph.NetgraphTreeVisualize;
import czsem.netgraph.NetgraphQueryConfig;
import czsem.netgraph.treesource.TreeIndexTreeSource;
import czsem.netgraph.util.DialogBasedAnnotationEditor;

public class NetgraphTreeViewer extends DialogBasedAnnotationEditor {
	private static final long serialVersionUID = -7161633338395041139L;
	
	private JTabbedPane tabs; 
	
	private TreeIndexTreeSource srcViewer = new TreeIndexTreeSource(); 

	private NetgraphTreeVisualize tabViewer = new NetgraphTreeVisualize(srcViewer);
	private NetgraphQueryDesigner tabQuery;
	private NetgraphResultsBrowser tabResults;
	private NetgraphQueryConfig tabConfig;
	
	

	@Override
	public String getTitle() { return "Netgraph Tree Viewer"; }
	@Override
	protected void updateInitDialog(JDialog dialog) { dialog.add(tabs);	}

	
	@Override
	protected void initGui() {
		tabViewer.initComponents();
		
		tabs = new JTabbedPane(JTabbedPane.BOTTOM);
		tabs.addTab("Viewer", tabViewer);
	
	}
	@Override
	public void editAnnotation(Annotation ann, AnnotationSet set) {
		if (ann == null) return;
		
		if (! canDisplayAnnotationType(ann.getType())) return;

		setAnnotation(ann, set);
		
		updateViewerAnnot();
		
		tabs.setSelectedComponent(tabViewer);		
		dialog.setVisible(true);		

	}

	protected void updateViewerAnnot() {
		AnnotationSet set = getAnnotationSetCurrentlyEdited();
		Document doc = set.getDocument();
		Annotation ann = getAnnotationCurrentlyEdited();
		
		GateAwareTreeIndexExtended i = new GateAwareTreeIndexExtended(doc);
		i.setNodesAS(set);
		
		//TODO use config
		i.addDependecies(set.get(null, Collections.singleton("args")));
		
		srcViewer.setDoc(doc);
		srcViewer.setIndex(i);
		srcViewer.selectNode(ann.getId());
		srcViewer.fireViewChanged();
	}
	
	
	@Override
	public boolean canDisplayAnnotationType(String annotationType) {
		if (annotationType.equals("Token"))
			return true;
		if (annotationType.equals("tToken"))
			return true;
		if (annotationType.equals("t-node"))
			return true;
		if (annotationType.equals("Sentence"))
			return true;

		return false;
	}


}
