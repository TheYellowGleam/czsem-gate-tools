package czsem.netgraph;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import czsem.netgraph.batik.BatikView;
import czsem.netgraph.treesource.TreeIndexTreeSource;

public class NetgraphTreeVisualize extends Container {
	private static final long serialVersionUID = -8809341412684396883L;

	protected final TreeIndexTreeSource treeSource;
	
	//protected final NetgraphView<Integer> forestDisplay = new NetgraphView<>(treeSource);
	protected final BatikView forestDisplay;

	private final GateAnnotTableModel dataModel;

	public NetgraphTreeVisualize(TreeIndexTreeSource treeSource) {
		this.treeSource = treeSource;
		forestDisplay = new BatikView(treeSource);
		dataModel = new GateAnnotTableModel(treeSource);
	}
	
	public void initComponents() { // make the dialog
		setLayout(new BorderLayout());

		/*
		final JPopupMenu pm = new JPopupMenu();
		final JMenuItem mi_show_hiddden = new JCheckBoxMenuItem("Show hidden nodes", true);
		mi_show_hiddden.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				//forestDisplay.setShowHiddenNodes(mi_show_hiddden.isSelected());
				//forestDisplay.repaint();
			}
		});
		pm.add(mi_show_hiddden);
		pm.add(new JSeparator());
		pm.add(new JMenuItem("123456789"));
		
		setDefaultLook();
		
		JScrollPane forestScrollpane = new JScrollPane(forestDisplay);
		forestDisplay.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger())
					pm.show(e.getComponent(), e.getX(), e.getY());
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger())
					pm.show(e.getComponent(), e.getX(), e.getY());
				else
				{
					int node = forestDisplay.selectNode(e);
					if (node != -1)
					{
						fireTreeNodeSelected(node);
					}
					repaint();
				}
			}
		});
		*/

		JTable table = new JTable(dataModel);
		TableColumn column = table.getColumnModel().getColumn(0);
		column.setMinWidth(21);
		column.setMaxWidth(21);
		column.setPreferredWidth(21);
		column.setResizable(true);

		JScrollPane tableScrollpane = new JScrollPane(table);

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				tableScrollpane, forestDisplay.initComponent());
		split.setDividerLocation(200);
		add(split);
	}
}
