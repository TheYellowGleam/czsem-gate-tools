package czsem.gate.plugins;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Utils;
import gate.creole.metadata.CreoleResource;

import javax.swing.JDialog;
import javax.swing.JTabbedPane;

import czsem.fs.GateAnnotationsNodeAttributesWithOnto;
import czsem.fs.depcfg.DependencySettings;
import czsem.fs.depcfg.DependencySource;
import czsem.fs.query.FSQuery;
import czsem.fs.query.FSQuery.QueryData;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.fs.query.FSQueryParser.SyntaxError;
import czsem.gate.utils.GateAwareTreeIndexExtended;
import czsem.netgraph.NetgraphQueryConfig;
import czsem.netgraph.NetgraphQueryDesigner;
import czsem.netgraph.NetgraphResultsBrowser;
import czsem.netgraph.NetgraphTreeVisualize;
import czsem.netgraph.treesource.TreeIndexTreeSource;
import czsem.netgraph.treesource.TreeSourceWithQueryMatch;
import czsem.netgraph.util.DialogBasedAnnotationEditor;

@CreoleResource(name = "Netgraph TreeViewer")
public class NetgraphTreeViewer extends DialogBasedAnnotationEditor {
	private static final long serialVersionUID = -7161633338395041139L;
	
	private JTabbedPane tabs; 
	
	private TreeIndexTreeSource srcViewer = new TreeIndexTreeSource(); 
	private TreeSourceWithQueryMatch srcResults = new TreeSourceWithQueryMatch(); 

	
	private NetgraphTreeVisualize tabViewer = new NetgraphTreeVisualize(srcViewer);
	private NetgraphQueryDesigner tabQuery = new NetgraphQueryDesigner();
	private NetgraphResultsBrowser tabResults = new NetgraphResultsBrowser(srcResults);
	
	private NetgraphQueryConfig tabConfig = new NetgraphQueryConfig(
			DependencySettings.getSelected(), DependencySettings.getAvailable()) 
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected void applyChanges() {
			updateForCurrentAnnotation();
		}
	}; 
	
	

	@Override
	public String getTitle() { return "Netgraph Tree Viewer"; }
	@Override
	protected void updateInitDialog(JDialog dialog) { dialog.add(tabs);	}

	
	@Override
	protected void initGui() {
		tabViewer.initComponents();
		tabQuery.initComponents();
		tabQuery.addSearchButton().addActionListener(e -> this.search());
		tabResults.initComponents();
		tabConfig.initComponents();
		
		tabs = new JTabbedPane(JTabbedPane.BOTTOM);
		tabs.addTab("Viewer", tabViewer);
		tabs.addTab("Query", tabQuery);
		tabs.addTab("Results", tabResults);
		tabs.addTab("Config", tabConfig);
	
	}
	
	protected void updateForCurrentAnnotation() {
		updateViewerAndResultsIndex();
		
		
		if ("Sentence".equals(getAnnotationCurrentlyEdited().getType())) {
			setSentenceAnnotation(getAnnotationCurrentlyEdited());
		} 
		
		updateViewerAnnotation();
		
		updateQueryAs();
	}

	
	@Override
	public void editAnnotation(Annotation ann, AnnotationSet set) {
		if (ann == null) return;
		
		if (! canDisplayAnnotationType(ann.getType())) return;
		
		tabConfig.updatePrsCombo();

		setAnnotation(ann, set);
		
		updateForCurrentAnnotation();

		tabs.setSelectedComponent(tabViewer);		
		dialog.setVisible(true);		
	}

	protected void setSentenceAnnotation(Annotation sentence) {
		AnnotationSet set = getAnnotationSetCurrentlyEdited();
		GateAwareTreeIndexExtended index = srcViewer.getIndex();
		
		AnnotationSet contained = Utils.getContainedAnnotations(set, sentence);
		
		for (Annotation c : contained) {
			Integer cId = c.getId();
			Integer rootId = index.findRootForNode(cId);
			
			if (rootId != cId) {
				setAnnotation(set.get(rootId), set);
				return;
			}
		}
	}
	
	private void updateQueryAs() {
		//TODO use the set
		tabQuery.setAs(getAnnotationSetCurrentlyEdited());
	}
	
	protected void search() {
		GateAwareTreeIndexExtended index = srcResults.getIndex();
		
		GateAnnotationsNodeAttributesWithOnto attrs = new GateAnnotationsNodeAttributesWithOnto(index);
		attrs.setOntology(index.getOntology());
		QueryData data = new QueryData(index, attrs); 
		
		try {
			Iterable<QueryMatch> results = 
					FSQuery.buildQuery(
							tabQuery.getQueryString()).evaluate(data);
			
			tabResults.setResults(results);
			
			tabs.setSelectedComponent(tabResults);

			
		} catch (SyntaxError e) {
			//TODO handle syntax error
			throw new RuntimeException(e);
		}
	}
	
	protected void updateViewerAndResultsIndex() {
		AnnotationSet set = getAnnotationSetCurrentlyEdited();
		Document doc = set.getDocument();
		
		GateAwareTreeIndexExtended i = new GateAwareTreeIndexExtended(doc);
		i.setNodesAS(set);
		
		//use config:
		DependencySource depSrc = tabConfig.getDependencySource(set);
		depSrc.addDependenciesToIndex(doc, i);
		
		srcViewer.setIndex(i);
		srcViewer.setDoc(doc);

		srcResults.setIndex(doc, i);
	}

	
	protected void updateViewerAnnotation() {
		Annotation ann = getAnnotationCurrentlyEdited();

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
