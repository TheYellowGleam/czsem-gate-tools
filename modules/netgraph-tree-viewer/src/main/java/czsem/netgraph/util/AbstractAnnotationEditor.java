package czsem.netgraph.util;

import java.awt.ComponentOrientation;

import gate.Annotation;
import gate.AnnotationSet;
import gate.creole.AbstractVisualResource;
import gate.gui.annedit.AnnotationEditorOwner;
import gate.gui.annedit.OwnedAnnotationEditor;
import gate.util.GateException;

@SuppressWarnings("serial")
public abstract class AbstractAnnotationEditor extends AbstractVisualResource implements OwnedAnnotationEditor {
	
	private AnnotationEditorOwner owner;
	private Annotation annotation;
	private AnnotationSet annotationSet;
	
	public abstract String getTitle();

	@Override
	public void okAction() throws GateException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cancelAction() throws GateException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean supportsCancel() {
		return true;
	}

	@Override
	public boolean canDisplayAnnotationType(String annotationType) {
		// TODO Auto-generated method stub
		return false;
	}

	protected void setAnnotation(Annotation ann, AnnotationSet set) {
		this.annotation = ann;
		this.annotationSet = set;
	}

	
	@Override
	public void editAnnotation(Annotation ann, AnnotationSet set) {
		setAnnotation(annotation, annotationSet);
	}

	@Override
	public boolean editingFinished() {
		return true;
	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Annotation getAnnotationCurrentlyEdited() {
		return annotation;
	}

	@Override
	public AnnotationSet getAnnotationSetCurrentlyEdited() {
		return annotationSet;
	}

	@Override
	public void placeDialog(int start, int end) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOwner(AnnotationEditorOwner owner) {
		this.owner = owner;
	}

	@Override
	public AnnotationEditorOwner getOwner() {
		return owner;
	}

	@Override
	public void setPinnedMode(boolean pinned) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEditingEnabled(boolean isEditingEnabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changeOrientation(ComponentOrientation orientation) {
		// TODO Auto-generated method stub
		
	}

}
