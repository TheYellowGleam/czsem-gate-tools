package czsem.gate.plugins;

import javax.swing.SwingUtilities;

import gate.Document;
import gate.Factory;
import gate.creole.splitter.SentenceSplitter;
import gate.creole.tokeniser.DefaultTokeniser;
import gate.gui.MainFrame;
import czsem.gate.utils.GateUtils;
import czsem.gate.utils.PRSetup;
import czsem.gate.utils.PRSetup.SinglePRSetup;

public class NetgraphTreeViewerTest {

	public static void main(String[] args) throws Exception {
		SwingUtilities.invokeAndWait(NetgraphTreeViewerTest::runCatch);
	}

	public static void runCatch() {
		try {
			runGate();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
	
	public static void runGate() throws Exception {
		
		GateUtils.initGateKeepLog();
		
		MainFrame mf = MainFrame.getInstance();
		mf.setVisible(true);
		
		GateUtils.registerPluginDirectory("ANNIE");
		GateUtils.registerPluginDirectory("Stanford_CoreNLP");
		GateUtils.registerComponentIfNot(NetgraphTreeViewer.class);
		
		
		PRSetup[] prs = {
				new SinglePRSetup(DefaultTokeniser.class),
				new SinglePRSetup(SentenceSplitter.class),
				new SinglePRSetup("gate.stanford.Parser"),
		};
		
		Document doc = Factory.newDocument("Bills on ports and immigration were submitted by Senator Brownback, Republican of Kansas. This is the second sentence.");
		PRSetup.execGatePipeline(prs, "NetgraphResultsBrowser", doc);
		
	}
}
