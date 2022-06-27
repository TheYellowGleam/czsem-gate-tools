package czsem.gate.externalannotator;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Utils;
import gate.creole.ANNIEConstants;
import gate.util.InvalidOffsetException;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import czsem.gate.externalannotator.RecursiveEntityAnnotator.SecondaryEntity;
import czsem.gate.externalannotator.SequenceAnnotator.CannotAnnotateCharacterSequence;
import czsem.gate.utils.GateUtils;

public class Annotator implements AnnotatorInterface {
	private static Logger logger = LoggerFactory.getLogger(Annotator.class);

	public static interface Annotable {
		public String getAnnotationType();
		public FeatureMap getFeatures();
		public void setGateAnnId(Integer gate_annotation_id);
	}
	
	public static abstract class AnnotableDependency implements SecondaryEntity {

		public abstract Integer getParentGateId();
		public abstract Integer getChildGateId();

		@Override
		public FeatureMap getFeatures() {
			return GateUtils.createDependencyArgsFeatureMap(
					getParentGateId(), 
					getChildGateId());
		}

		@Override
		public boolean annotate(AnnotatorInterface annotator) throws InvalidOffsetException {
			if (getParentGateId() == null) return false;
			if (getChildGateId() == null) return false;
			annotator.annotateDependecy(this);
			return true;
		}
		
		@Override
		public void setGateAnnId(Integer gate_annotation_id) {}
	}
	
	public static class DependencyImpl extends AnnotableDependency {
		protected final Integer parentGateAnnId;
		protected final Integer childGateAnnId;

		public DependencyImpl(Integer parentGateAnnId, Integer childGateAnnId) {
			this.parentGateAnnId = parentGateAnnId;
			this.childGateAnnId = childGateAnnId;
		}

		@Override
		public String getAnnotationType() {
			return "Dependency";
		}

		@Override
		public Integer getParentGateId() {
			return parentGateAnnId;
		}

		@Override
		public Integer getChildGateId() {
			return childGateAnnId;
		}
	}

	public static interface SeqAnnotable extends Annotable {
		public String getString();
	}

	public static interface Sentence extends SeqAnnotable {
		List<? extends SeqAnnotable> getOrderedTokens();
		void annotateSecondaryEntities(AnnotatorInterface annotator) throws InvalidOffsetException;		
	}

	
	public static interface AnnotationSource {
		Iterable<Sentence> getOrderedSentences();		
	}

	private SequenceAnnotator seq_anot;
	private AnnotationSet as;
	private long nextSpaceTokenStart = 0;

	
	public void setSeqAnot(SequenceAnnotator seq_anot) {
		this.seq_anot = seq_anot;
	}

	public void initBefore(Document doc, String outputASName) {
		nextSpaceTokenStart = 0;
		
		seq_anot = new SequenceAnnotator(doc);
		as = doc.getAnnotations(outputASName);
		
		seq_anot.backup();
	}
	
	public void annotate(
			AnnotationSource annotationSource,
			Document doc,
			String outputASName) throws InvalidOffsetException
	{
		initBefore(doc, outputASName);

		for (Sentence s : annotationSource.getOrderedSentences())
		{
			annotateSentence(s);
		}
	}

	public void annotateSentence(Sentence s) throws InvalidOffsetException {
    	//seq_anot.backup(); 
    	//commented out (moved to annotate(...)) because of cases when sentence annotation fails but tokens are ok

    	if (safeAnnotateSeq(s)) {
    		//search inside the last sentence only
    		addSplitAnnotation();
    		seq_anot.restoreToLastStartAndBackupCurrent();
    	} else {
    		seq_anot.restorePreviousAndBackupCurrent();
    	}
    	
		safeAnnotateIterableSeq(s.getOrderedTokens());
		
		s.annotateSecondaryEntities(this);

		//important in cases, when there are no tokens 
		seq_anot.restorePreviousAndBackupCurrent();

	}

	public static boolean isEndPunctuationChar(char ch) {
		return Character.OTHER_PUNCTUATION == Character.getType(ch);
	}
	
	public void addSplitAnnotation() throws InvalidOffsetException {
		int end = seq_anot.lastEndInt();
		
		/*
		System.err.print(">");
		System.err.print(seq_anot.substring(0, end));
		System.err.println("<");
		/**/
		
		//if (end > 0) end--;
		//this was wrong
		int start = end;

		if (start > 0 && isEndPunctuationChar(seq_anot.charAt(start-1)))
			start--;
		
		as.add((long) start, (long) end, "Split", Factory.newFeatureMap());
	}
	
	protected void createSpaceTokens(long start, long end) throws InvalidOffsetException {
		createSpaceTokens(as, seq_anot.substring((int)start, (int)end), start, end);
	}

	public static void createSpaceTokens(AnnotationSet as, Document doc, long start, long end) throws InvalidOffsetException {
		createSpaceTokens(as, Utils.stringFor(doc, start, end), start, end);
	}

	public static void createSpaceTokens(AnnotationSet as, String spanContent, long start, long end) throws InvalidOffsetException {
		for (int i = 0; i < spanContent.length(); i++) {
			int spaceLength;
			//CRLF detection
			if (i+1 < spanContent.length() &&
					spanContent.codePointAt(i) == '\r' &&
					spanContent.codePointAt(i+1) == '\n')
			{
				spaceLength = 2; //is CRLF
			} else {
				spaceLength = 1; //not CRLF
			}

			String curSubStr = spanContent.substring(i, i+spaceLength);
			as.add(start+i, start+i+spaceLength, ANNIEConstants.SPACE_TOKEN_ANNOTATION_TYPE,
				Utils.featureMap(
					ANNIEConstants.TOKEN_STRING_FEATURE_NAME, curSubStr,
					ANNIEConstants.TOKEN_KIND_FEATURE_NAME, getSpaceTokenKind(spanContent, i)
					//ANNIEConstants.TOKEN_LENGTH_FEATURE_NAME, 1
			));
			i += spaceLength-1;
		}
	}
	
	public static String getSpaceTokenKind(String str, int offset) throws InvalidOffsetException {
		int code = str.codePointAt(offset);
		
		if (Character.isISOControl(code)) return "control"; 
		if (Character.isSpaceChar(code)) return "space"; 
		
		return "other";
	}
	

	protected int annotateIterableSeqStep(List<? extends SeqAnnotable> sa, int i) throws InvalidOffsetException {
		SeqAnnotable next = sa.get(i);
		try {
			annotateSeq(next);
			return i;
		} catch (CannotAnnotateCharacterSequence e) {
			int j = 0;
			if (e.annotator_content.charAt(e.last_start_index) == '.' && e.token.equals("<") && sa.size() > i+5 &&
					sa.get(i+ ++j).getString().equals("<") &&
					sa.get(i+ ++j).getString().equals("<") &&
					sa.get(i+ ++j).getString().equals("DOT") &&
					sa.get(i+ ++j).getString().equals(">") &&
					sa.get(i+ ++j).getString().equals(">"))
			{
				//DOT
				annotateSeq(sa.get(i+ 3));
				i+= 5;
				return i;
			}
			throw e;
		}
	}

	protected void safeAnnotateIterableSeq(List<? extends SeqAnnotable> sa) throws InvalidOffsetException {
		
		for (int i=0; i<sa.size(); i++)
		{
			boolean isOK = false;
			try {
				i = annotateIterableSeqStep(sa, i);
				isOK = true;
			} catch (CannotAnnotateCharacterSequence e) {
				isOK = safeAnnotateSeq(sa.get(i));
			}
			
			if (isOK) {
				if (nextSpaceTokenStart < seq_anot.lastStart())
					createSpaceTokens(nextSpaceTokenStart, seq_anot.lastStart());
				
				nextSpaceTokenStart = seq_anot.lastEnd();
			}
		}
	}

	protected void annotateIterableSeq(List<SeqAnnotable> sa) throws InvalidOffsetException {
		for (int i=0; i<sa.size(); i++)
		{
			i = annotateIterableSeqStep(sa, i);
		}
	}

	protected boolean safeAnnotateSeq(SeqAnnotable seqAnn) throws InvalidOffsetException {
		try {
			annotateSeq(seqAnn);
			return true;
		} catch (CannotAnnotateCharacterSequence e) {
			logger.error("SeqAnnotation error in document: {}\n{}", as.getDocument().getName(), this, e);
			return false;
		}
	}

	protected void annotateSeq(SeqAnnotable seqAnn) throws InvalidOffsetException {
    	seq_anot.nextToken(seqAnn.getString());
    	annotate(seqAnn, seq_anot.lastStart(), seq_anot.lastEnd());
	}

	@Override
	public void annotate(Annotable ann, Long startOffset, Long endOffset) throws InvalidOffsetException {
    	Integer gate_annotation_id = as.add(
    			startOffset,
    			endOffset,
    			ann.getAnnotationType(),
    			ann.getFeatures());
    	
    	ann.setGateAnnId(gate_annotation_id);    	
	}

	@Override
	public Annotation getAnnotation(Integer id) {
		return as.get(id);
	}

	@Override
	public void annotateDependecy(AnnotableDependency dAnn) throws InvalidOffsetException {
		Integer gate_parent_id = dAnn.getParentGateId();
		Integer gate_child_id = dAnn.getChildGateId();

		
		Annotation a1 = as.get(gate_parent_id);
		Annotation a2 = as.get(gate_child_id);
		
		if (a1 == null || a2 == null) return;
		
		Long ix1 = Math.min(a1.getStartNode().getOffset(), a2.getStartNode().getOffset());
		Long ix2 = Math.max(a1.getEndNode().getOffset(), a2.getEndNode().getOffset());
		
		annotate(dAnn, ix1, ix2);
		
	}

	public AnnotationSet getAS() {
		return as;
	}

	public void setAS(AnnotationSet as) {
		this.as = as; 		
	}

	public void forceStartOffset(long offset) {
		if (offset - nextSpaceTokenStart > SequenceAnnotator.LARGE_SPACE)
			nextSpaceTokenStart = offset;
		
		seq_anot.forceStartOffset(offset);
	}

	public void annotateTokens(List<? extends SeqAnnotable> tokens) throws InvalidOffsetException {
		safeAnnotateIterableSeq(tokens);
	}
}
