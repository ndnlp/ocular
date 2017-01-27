package edu.berkeley.cs.nlp.ocular.eval;

import static edu.berkeley.cs.nlp.ocular.data.textreader.Charset.HYPHEN;
import static edu.berkeley.cs.nlp.ocular.data.textreader.Charset.LONG_S;
import static edu.berkeley.cs.nlp.ocular.data.textreader.Charset.SPACE;
import static edu.berkeley.cs.nlp.ocular.util.CollectionHelper.last;
import static edu.berkeley.cs.nlp.ocular.util.Tuple2.Tuple2;

import java.util.ArrayList;
import java.util.List;

import edu.berkeley.cs.nlp.ocular.data.textreader.Charset;
import edu.berkeley.cs.nlp.ocular.gsm.GlyphChar.GlyphType;
import edu.berkeley.cs.nlp.ocular.model.DecodeState;
import edu.berkeley.cs.nlp.ocular.model.transition.SparseTransitionModel.TransitionState;
import edu.berkeley.cs.nlp.ocular.util.CollectionHelper;
import edu.berkeley.cs.nlp.ocular.util.Tuple2;
import tberg.murphy.indexer.Indexer;

/**
 * @author Dan Garrette (dhgarrette@gmail.com)
 */
public class ModelTranscriptions {
	private List<Tuple2<String,String>>[] viterbiDiplomaticCharLangLines;
	private List<Tuple2<String,String>>[] viterbiNormalizedCharLangLines;
	private List<Tuple2<String,String>> viterbiNormalizedCharLangTranscription; // A continuous string, re-assembling words hyphenated over a line.
	private List<DecodeState>[] viterbiDecodeStates;
	
	private Indexer<String> charIndexer;
	private Indexer<String> langIndexer;

	@SuppressWarnings("unchecked")
	public ModelTranscriptions(DecodeState[][] decodeStates, Indexer<String> charIndexer, Indexer<String> langIndexer) {
		this.charIndexer = charIndexer;
		this.langIndexer = langIndexer;
		int numLines = decodeStates.length;
		
		this.viterbiDiplomaticCharLangLines = new List[numLines];
		this.viterbiNormalizedCharLangLines = new List[numLines];
		this.viterbiNormalizedCharLangTranscription = new ArrayList<Tuple2<String,String>>();
		this.viterbiDecodeStates = new List[numLines];

		for (int line = 0; line < numLines; ++line) {
			viterbiDiplomaticCharLangLines[line] = new ArrayList<Tuple2<String,String>>();
			viterbiNormalizedCharLangLines[line] = new ArrayList<Tuple2<String,String>>();
			viterbiDecodeStates[line] = new ArrayList<DecodeState>();
			
			for (int i = 0; i < decodeStates[line].length; ++i) {
				DecodeState ds = decodeStates[line][i];
				TransitionState ts = ds.ts;
				String currDiplomaticChar = charIndexer.getObject(ts.getGlyphChar().templateCharIndex);
				String prevDiplomaticChar = (!viterbiDiplomaticCharLangLines[line].isEmpty() ? CollectionHelper.last(viterbiDiplomaticCharLangLines[line])._1 : null); // null if start of line, but that's ok
				if (HYPHEN.equals(prevDiplomaticChar) && HYPHEN.equals(currDiplomaticChar)) {
					// collapse multi-hyphens
				}
				else {
					viterbiDecodeStates[line].add(ds);

					//
					// Add diplomatic characters to diplomatic transcription
					//
					if (!ts.getGlyphChar().isElided()) {
						viterbiDiplomaticCharLangLines[line].add(makeCharLangTuple(Charset.unescapeChar(currDiplomaticChar), ts.getLanguageIndex()));
					}
					
					//
					// Add normalized characters to normalized transcriptions
					//
					if (ts.getGlyphChar().glyphType != GlyphType.DOUBLED) { // the first in a pair of doubled characters isn't part of the normalized transcription
						String currNormalizedChar = charIndexer.getObject(ts.getLmCharIndex());
						//if (LONG_S.equals(currNormalizedChar)) currNormalizedChar = "s"; // don't use long-s in normalized transcriptions
						
						//
						// Add to normalized line transcription
						viterbiNormalizedCharLangLines[line].add(makeCharLangTuple(Charset.unescapeChar(currNormalizedChar), ts.getLanguageIndex()));
						
						//
						// Add to normalized running transcription
						switch(ts.getType()) {
							case RMRGN_HPHN_INIT:
							case RMRGN_HPHN:
							case LMRGN_HPHN:
								break;
								
							case LMRGN:
							case RMRGN:
								if (!viterbiNormalizedCharLangTranscription.isEmpty() && !SPACE.equals(last(viterbiNormalizedCharLangTranscription))) {
									viterbiNormalizedCharLangTranscription.add(makeCharLangTuple(SPACE, ts.getLanguageIndex()));
								}
								break;
							
							case TMPL:
								if (SPACE.equals(currNormalizedChar) && (viterbiNormalizedCharLangTranscription.isEmpty() || SPACE.equals(last(viterbiNormalizedCharLangTranscription)))) {
									// do nothing -- collapse spaces
								}
								else {
									viterbiNormalizedCharLangTranscription.add(makeCharLangTuple(Charset.unescapeChar(currNormalizedChar), ts.getLanguageIndex()));
								}
						}
					}
				}
			}
		}

		if (SPACE.equals(last(viterbiNormalizedCharLangTranscription))) {
			viterbiNormalizedCharLangTranscription.remove(viterbiNormalizedCharLangTranscription.size() - 1);
		}
	}
	
	private Tuple2<String, String> makeCharLangTuple(String c, int langIndex) {
		String lang = (langIndex >= 0 ? langIndexer.getObject(langIndex) : null);
		return Tuple2(c, lang);
	}

	public List<Tuple2<String, String>>[] getViterbiDiplomaticCharLangLines() {
		return viterbiDiplomaticCharLangLines;
	}

	public List<String>[] getViterbiDiplomaticCharLines() {
		@SuppressWarnings("unchecked")
		List<String>[] output = new List[viterbiDiplomaticCharLangLines.length];
		for (int i = 0; i < viterbiDiplomaticCharLangLines.length; ++i) 
			output[i] = mapToElement1(viterbiDiplomaticCharLangLines[i]);
		return output;
	}

	public List<String>[] getViterbiDiplomaticLangLines() {
		@SuppressWarnings("unchecked")
		List<String>[] output = new List[viterbiDiplomaticCharLangLines.length];
		for (int i = 0; i < viterbiDiplomaticCharLangLines.length; ++i) 
			output[i] = mapToElement2(viterbiDiplomaticCharLangLines[i]);
		return output;
	}

	public List<Tuple2<String, String>>[] getViterbiNormalizedCharLangLines() {
		return viterbiNormalizedCharLangLines;
	}

	public List<String>[] getViterbiNormalizedCharLines() {
		@SuppressWarnings("unchecked")
		List<String>[] output = new List[viterbiNormalizedCharLangLines.length];
		for (int i = 0; i < viterbiNormalizedCharLangLines.length; ++i) 
			output[i] = mapToElement1(viterbiNormalizedCharLangLines[i]);
		return output;
	}

	public List<String>[] getViterbiNormalizedLangLines() {
		@SuppressWarnings("unchecked")
		List<String>[] output = new List[viterbiNormalizedCharLangLines.length];
		for (int i = 0; i < viterbiNormalizedCharLangLines.length; ++i) 
			output[i] = mapToElement2(viterbiNormalizedCharLangLines[i]);
		return output;
	}

	public List<Tuple2<String, String>> getViterbiNormalizedCharLangTranscription() {
		return viterbiNormalizedCharLangTranscription;
	}

	public List<String> getViterbiNormalizedCharTranscription() {
		return mapToElement1(viterbiNormalizedCharLangTranscription);
	}

	public List<String> getViterbiNormalizedLangTranscription() {
		return mapToElement2(viterbiNormalizedCharLangTranscription);
	}

	public List<DecodeState>[] getViterbiDecodeStates() {
		return viterbiDecodeStates;
	}
	
	private <A,B> List<A> mapToElement1(List<Tuple2<A,B>> input) {
		List<A> output = new ArrayList<A>();
		for (Tuple2<A,B> t : input) output.add(t._1);
		return output;
	}
	
	private <A,B> List<B> mapToElement2(List<Tuple2<A,B>> input) {
		List<B> output = new ArrayList<B>();
		for (Tuple2<A,B> t : input) output.add(t._2);
		return output;
	}
}
