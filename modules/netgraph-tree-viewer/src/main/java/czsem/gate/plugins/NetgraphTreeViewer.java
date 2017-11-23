package czsem.gate.plugins;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Gate;
import gate.Utils;
import gate.creole.metadata.CreoleResource;

import javax.swing.JDialog;
import javax.swing.JTabbedPane;

import czsem.fs.GateAnnotationsNodeAttributes;
import czsem.fs.depcfg.DependencySettings;
import czsem.fs.depcfg.DependencySource;
import czsem.fs.depcfg.DependencySourceFromCfgAndSet;
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
			DependencySettings.getSelected(), DependencySettings.getAvailable()); 
	
	

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
	@Override
	public void editAnnotation(Annotation ann, AnnotationSet set) {
		if (ann == null) return;
		
		if (! canDisplayAnnotationType(ann.getType())) return;

		setAnnotation(ann, set);
		updateViewerAndResultsIndex();
		
		
		if ("Sentence".equals(ann.getType())) {
			setSentenceAnnotation(ann);
		} 
		
		updateViewerAnnotation();
		
		updateQueryAs();

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
		QueryData data = new FSQuery.QueryData(index, 
				new GateAnnotationsNodeAttributes(
						getAnnotationSetCurrentlyEdited()));
		
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
	
	public static boolean isDepSource(String clsName) {
		if (clsName == null) return false;
		
		try {
			Class<?> cls = Gate.getClassLoader().loadClass(clsName);
			return DependencySource.class.isAssignableFrom(cls);
		} catch (ClassNotFoundException e) {
			System.err.println(e);
			return false;
		}
	}


	protected void updateViewerAndResultsIndex() {
		AnnotationSet set = getAnnotationSetCurrentlyEdited();
		Document doc = set.getDocument();
		
		GateAwareTreeIndexExtended i = new GateAwareTreeIndexExtended(doc);
		i.setNodesAS(set);
		
		//i.addDependecies(set.get(null, Collections.singleton("args")));
		
		//use config:
		DependencySourceFromCfgAndSet depSrc = new DependencySourceFromCfgAndSet(
				DependencySettings.getSelectedConfigurationFromConfigOrDefault(), 
				set);
		depSrc.addDependenciesToIndex(doc, i);
		
		//TODO or use Grext
		/*
		CreoleRegister reg = Gate.getCreoleRegister();
		List<String> types = reg.getPublicPrTypes();
		for (String string : types) {
			if (isDepSource(string)) {
				System.err.println(string);
				try {
					List<Resource> insts = reg.getAllInstances(string);
					for (Resource resource : insts) {
						System.err.println(resource.getName());
						
						((DependencySource) resource).addDependenciesToIndex(doc, i);
					}
				} catch (GateException e) {
					throw new RuntimeException(e);
				}
				
			}
		}
		*/
		
		
		

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
