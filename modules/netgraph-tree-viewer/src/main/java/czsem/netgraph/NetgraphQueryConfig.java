package czsem.netgraph;

import gate.util.GateException;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import czsem.fs.depcfg.DependencySetting;
import czsem.fs.depcfg.DependencySettings;
import czsem.netgraph.util.AddRemoveListsManager;
import czsem.netgraph.util.AddRemoveListsManagerForTocDep;

public class NetgraphQueryConfig extends Container {
	private static final long serialVersionUID = 8676767227162395664L;
	private static final Logger logger = LoggerFactory.getLogger(NetgraphQueryConfig.class);
	
	protected final DependencySetting selected;
	protected final DependencySetting available;
	
	public NetgraphQueryConfig(DependencySetting selected, DependencySetting available) {
		this.selected = selected;
		this.available = available;
	}

	public NetgraphQueryConfig() {
		this(new DependencySetting(), new DependencySetting());
	}

	public static void main(String[] args) throws Exception {
		JFrame fr = new JFrame(NetgraphQueryConfig.class.getName());
	    fr.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	
		
	    NetgraphQueryConfig qd = new NetgraphQueryConfig();
		qd.initComponents();
		
		fr.add(qd);
		
		fr.pack();
		fr.setVisible(true);

	}

	protected static <E> JPanel embedDependencyManager(AddRemoveListsManager<E> man, String title) {
        JPanel panel_dependencies = new JPanel(new BorderLayout());
        panel_dependencies.setBorder(
        		BorderFactory.createCompoundBorder(
        				BorderFactory.createEmptyBorder(15, 15, 15, 15), 
        				BorderFactory.createTitledBorder(title)));
        man.initComponents();
		panel_dependencies.add(man);
		
		return panel_dependencies;
	}

	public void initComponents() {
		setLayout(new BorderLayout());

		JPanel panel_center = new JPanel(new GridLayout(1, 2));
        add(panel_center, BorderLayout.CENTER);

        JPanel panel_south = new JPanel();
        add(panel_south, BorderLayout.SOUTH);
		

        final AddRemoveListsManager<String> depMan = new AddRemoveListsManager<>();
        panel_center.add(embedDependencyManager(depMan, "Dependencies"));
        depMan.addLeftModelSynchronization(selected.getDependencyNames());
        depMan.addRightModelSynchronization(available.getDependencyNames());
		depMan.synchronizeModels();

        final AddRemoveListsManagerForTocDep tocDepMan = new AddRemoveListsManagerForTocDep();
        panel_center.add(embedDependencyManager(tocDepMan, "Token Dependencies"));
        tocDepMan.addLeftModelSynchronization(selected.getTokenDepDefs());
        tocDepMan.addRightModelSynchronization(available.getTokenDepDefs());
		tocDepMan.synchronizeModels();
        
        
		JButton buttonDefaults = new JButton("Defaults");
		buttonDefaults.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selected.replaceBy(DependencySettings.defaultConfigSelected);
				available.replaceBy(DependencySettings.defaultConfigAvailable);
				depMan.synchronizeModels();
				tocDepMan.synchronizeModels();
			}
		});
		panel_south.add(buttonDefaults);

		
        JButton buttonSave = new JButton("Save");
		buttonSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				performSave();
			}
		});
		panel_south.add(buttonSave);
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				depMan.synchronizeModels();
				tocDepMan.synchronizeModels();				
			}			
		});

	}

	/** can be overridden... **/
	protected void performSave() {
		try {
			DependencySettings.saveSettings();
		} catch (IOException | GateException e) {
			logger.warn("Save failed...", e);
		}
	}

}
