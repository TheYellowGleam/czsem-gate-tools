package czsem.netgraph;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import gate.Document;
import gate.Factory;
import gate.creole.splitter.SentenceSplitter;
import gate.creole.tokeniser.DefaultTokeniser;
import czsem.gate.utils.GateUtils;
import czsem.gate.utils.PRSetup;
import czsem.gate.utils.PRSetup.*;
import czsem.netgraph.treesource.TreeIndexTreeSource;

public class NetgraphTreeVisualizeTest {
	
	public static void main(String[] args) throws Exception {
		GateUtils.initGateKeepLog();
		GateUtils.registerPluginDirectory("ANNIE");
		GateUtils.registerPluginDirectory("Stanford_CoreNLP");
		
		
		PRSetup[] prs = {
				new SinglePRSetup(DefaultTokeniser.class),
				new SinglePRSetup(SentenceSplitter.class),
				new SinglePRSetup("gate.stanford.Parser"),
		};
		
		Document doc = Factory.newDocument("Bills on ports and immigration were submitted by Senator Brownback, Republican of Kansas.");
		PRSetup.execGatePipeline(prs, "NetgraphTreeVisualize", doc);
		
		JFrame fr = new JFrame(NetgraphTreeVisualize.class.getName());
	    fr.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	
		
	    TreeIndexTreeSource src = new TreeIndexTreeSource(); 
	    NetgraphTreeVisualize tv = new NetgraphTreeVisualize(src);

		tv.initComponents();

		src.setTreeAS(doc, doc.getAnnotations());
		
		fr.add(tv);
		
		fr.pack();
		fr.setVisible(true);
		
	}
}
