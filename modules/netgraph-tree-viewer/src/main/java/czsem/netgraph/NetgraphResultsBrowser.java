package czsem.netgraph;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import javax.swing.JButton;
import javax.swing.JPanel;

import czsem.fs.query.FSQuery;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.netgraph.treesource.TreeSourceWithQueryMatch;

public class NetgraphResultsBrowser extends Container {
	private static final long serialVersionUID = 4067711902912032236L;
	
	private final NetgraphTreeVisualize treeVisualize;

	protected ResultsWalker resultsWalker;
	protected final TreeSourceWithQueryMatch treeSource;

	//public AsIndexHelper asIndexHelper = new AsIndexHelper(); 

	private final JButton buttonPrevious = new JButton("< Previous");
	private final JButton buttonNext = new JButton("Next >");
	
	public NetgraphResultsBrowser(TreeSourceWithQueryMatch treeSource) {
		this.treeSource = treeSource;
		treeVisualize = new NetgraphTreeVisualize(treeSource);
	}
	
	protected static class ResultsWalker {
		
		private Iterator<QueryMatch> results;
		private ListIterator<QueryMatch> cachePos;
		private int currentIndex = -1;

		public ResultsWalker(Iterator<QueryMatch> results) {
			this.results = results;
			LinkedList<QueryMatch> cache = new LinkedList<FSQuery.QueryMatch>();
			cachePos = cache.listIterator();
		}

		public boolean hasPrevious() {
			return cachePos.hasPrevious();
		}

		public QueryMatch previous() {
			if (cachePos.previousIndex() == currentIndex) cachePos.previous();
			currentIndex = cachePos.previousIndex();			
			return cachePos.previous();
		}

		public boolean hasNext() {
			if (cachePos.hasNext()) return true;
			if (results.hasNext()) return true;
			return false;
		}

		public QueryMatch next() {
			if (cachePos.nextIndex() == currentIndex) cachePos.next();

			if (cachePos.hasNext())
			{
				currentIndex = cachePos.nextIndex();			
				return cachePos.next();
			}
			if (results.hasNext()) {
				QueryMatch r = results.next();
				currentIndex = cachePos.nextIndex();			
				cachePos.add(r);
				return r;
			}
			throw new NoSuchElementException();
		}
		
	}
	
	/*
	public static class AsIndexHelper {
		private AnnotationSet as;
		private GateAwareTreeIndex index;
		
		public void setSourceAS(AnnotationSet as) {
			this.as = as;
		}

		public void initIndex() {
			index = new GateAwareTreeIndex();
			index.addDependecies(as, DependencyConfiguration.getSelectedConfigurationFromConfigOrDefault());;
		}

		public QueryData createQueryData() {
			return new FSQuery.QueryData(index, new GateAnnotationsNodeAttributes(as));
		}
	}
	*/



	public void setResults(Iterable<QueryMatch> results) {
		Iterator<QueryMatch> i = results.iterator();
		if (! i.hasNext()) {
			//empty results
			showMatch(null);
			treeSource.selectNode(-1);
			treeSource.fireViewChanged();
		}
		
		resultsWalker = new ResultsWalker(i);
		showNext();
		buttonPrevious.setEnabled(false);
	}
	
	
	public void initComponents() {
		setLayout(new BorderLayout());
		
		treeVisualize.initComponents();		
		add(treeVisualize, BorderLayout.CENTER);
		
		
		JPanel southPanel = new JPanel(new FlowLayout());
		add(southPanel, BorderLayout.SOUTH);
				
		buttonPrevious.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { showPrevious(); }});
		southPanel.add(buttonPrevious);
		
		buttonNext.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { showNext(); }});
		//same size as "Previous"
		buttonNext.setPreferredSize(buttonPrevious.getPreferredSize());

		southPanel.add(buttonNext);
		
		setResults(Collections.emptyList());
	}

	protected void showMatch(QueryMatch match) {
		treeSource.setQueryMatch(match);
	}

	protected void showNext() {
		if (resultsWalker.hasNext())
		{
			showMatch(resultsWalker.next());
			buttonPrevious.setEnabled(true);
		}
		
		buttonNext.setEnabled(resultsWalker.hasNext());
	}

	
	protected void showPrevious() {
		if (resultsWalker.hasPrevious())
		{
			showMatch(resultsWalker.previous());		
			buttonNext.setEnabled(true);
		}

		buttonPrevious.setEnabled(resultsWalker.hasPrevious());		
	}

}
