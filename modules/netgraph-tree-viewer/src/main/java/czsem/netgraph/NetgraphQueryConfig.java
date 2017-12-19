package czsem.netgraph;

import gate.AnnotationSet;
import gate.CreoleRegister;
import gate.Gate;
import gate.Resource;
import gate.gui.ResourceRenderer;
import gate.util.GateException;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import czsem.fs.depcfg.DependencySetting;
import czsem.fs.depcfg.DependencySettings;
import czsem.fs.depcfg.DependencySource;
import czsem.fs.depcfg.DependencySourceFromCfgAndSet;
import czsem.netgraph.util.AddRemoveListsManager;
import czsem.netgraph.util.AddRemoveListsManagerForTocDep;

public class NetgraphQueryConfig extends Container {
	private static final long serialVersionUID = 8676767227162395664L;
	private static final Logger logger = LoggerFactory.getLogger(NetgraphQueryConfig.class);
	
	protected final DependencySetting selected;
	protected final DependencySetting available;
	protected JComboBox<Object> prsCombo;
	
	public NetgraphQueryConfig(DependencySetting selected, DependencySetting available) {
		this.selected = selected;
		this.available = available;
	}

	public NetgraphQueryConfig() {
		this(new DependencySetting(), new DependencySetting());
	}

	public static void main(String[] args) throws Exception {
		Gate.runInSandbox(true);
		Gate.init();
		
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
        				BorderFactory.createEmptyBorder(10, 10, 10, 10), 
        				BorderFactory.createTitledBorder(title)));
        man.initComponents();
		panel_dependencies.add(man);
		
		return panel_dependencies;
	}

	public void initComponents() {
		setLayout(new BorderLayout());

		JPanel panel_center = new JPanel(new GridLayout(1, 2));
        add(panel_center, BorderLayout.CENTER);

        JPanel panel_south = new JPanel(new BorderLayout());
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
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
		panel_south.add(buttons, BorderLayout.LINE_END);
		buttons.add(buttonDefaults);

		
        JButton buttonSave = new JButton("Save");
        buttonSave.setPreferredSize(buttonDefaults.getPreferredSize());
		buttonSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				performSave();
			}
		});
		buttons.add(buttonSave);
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				depMan.synchronizeModels();
				tocDepMan.synchronizeModels();				
			}			
		});
		
		
		prsCombo = new JComboBox<Object>() {
			private static final long serialVersionUID = 1L;
			@Override
			public Dimension getPreferredSize() {
				Dimension ret = super.getPreferredSize();
				return new Dimension(Math.max(ret.width, 350), ret.height);
			}
		};
		
		@SuppressWarnings("unchecked")
		ListCellRenderer<Object> r = new ResourceRenderer();
		prsCombo.setRenderer(r);
		
		Object[] values = new Object [] {"<none>"};
		prsCombo.setModel(new DefaultComboBoxModel<Object>(values));
		prsCombo.setEditable(false);
		prsCombo.setSelectedItem("<none>");

		JPanel comboBorder = new JPanel();
		comboBorder.setBorder(
        		BorderFactory.createCompoundBorder(
        				BorderFactory.createEmptyBorder(0, 10, 10, 0), 
        				BorderFactory.createTitledBorder("Use config from PR (instead of manual)")));
		
		comboBorder.add(prsCombo);
		panel_south.add(comboBorder, BorderLayout.LINE_START);

	}

	/** can be overridden... **/
	protected void performSave() {
		try {
			DependencySettings.saveSettings();
		} catch (IOException | GateException e) {
			logger.warn("Save failed...", e);
		}
	}
	
	public void updatePrsCombo() {
		Object sel = prsCombo.getSelectedItem();
		
		List<Object> values = new ArrayList<>();
		values.add("<none>");
		
		CreoleRegister reg = Gate.getCreoleRegister();
		List<String> types = reg.getPublicPrTypes();
		for (String type : types) {
			if (isDepSource(type)) {
				try {
					List<Resource> insts = reg.getAllInstances(type);
					for (Resource resource : insts) {
						values.add(resource);
					}
				} catch (GateException e) {
					throw new RuntimeException(e);
				}
			}
		}
		prsCombo.setModel(new DefaultComboBoxModel<Object>(values.toArray()));
		
		prsCombo.setSelectedItem(sel);
		
	}
	
	public static boolean isDepSource(String clsName) {
		if (clsName == null) return false;
		
		try {
			Class<?> cls = Gate.getClassLoader().loadClass(clsName);
			return DependencySource.class.isAssignableFrom(cls);
		} catch (ClassNotFoundException e) {
			logger.warn("ClassNotFound when looking for DepSource {}", clsName, e);
			return false;
		}
	}

	public DependencySource getDependencySource(AnnotationSet annotationSet) {
		Object sel = prsCombo.getSelectedItem();
		if (sel != null && sel instanceof DependencySource) {
			return (DependencySource) sel;
		}

		DependencySourceFromCfgAndSet depSrc = new DependencySourceFromCfgAndSet(
				DependencySettings.getSelectedConfigurationFromConfigOrDefault(), 
				annotationSet);
		
		return depSrc;
	}

	

}
