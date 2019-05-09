package edu.berkeley.cs.nlp.ocular.gsm;

import static edu.berkeley.cs.nlp.ocular.data.textreader.Charset.makeAddTildeMap;
import static edu.berkeley.cs.nlp.ocular.data.textreader.Charset.makeCanBeElidedSet;
import static edu.berkeley.cs.nlp.ocular.data.textreader.Charset.makeCanBeReplacedSet;
import static edu.berkeley.cs.nlp.ocular.data.textreader.Charset.makeDiacriticDisregardMap;
import static edu.berkeley.cs.nlp.ocular.data.textreader.Charset.makeValidDoublableSet;
import static edu.berkeley.cs.nlp.ocular.data.textreader.Charset.makeValidSubstitutionCharsSet;
import static edu.berkeley.cs.nlp.ocular.util.CollectionHelper.makeSet;
import static edu.berkeley.cs.nlp.ocular.util.CollectionHelper.setUnion;
import static edu.berkeley.cs.nlp.ocular.util.Tuple2.Tuple2;
import static edu.berkeley.cs.nlp.ocular.util.CollectionHelper.makeMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.berkeley.cs.nlp.ocular.util.Tuple2;
import edu.berkeley.cs.nlp.ocular.data.textreader.Charset;
import edu.berkeley.cs.nlp.ocular.gsm.GlyphChar.GlyphType;
import edu.berkeley.cs.nlp.ocular.model.DecodeState;
import edu.berkeley.cs.nlp.ocular.model.TransitionStateType;
import edu.berkeley.cs.nlp.ocular.model.transition.SparseTransitionModel.TransitionState;
import edu.berkeley.cs.nlp.ocular.util.ArrayHelper;
import edu.berkeley.cs.nlp.ocular.util.FileHelper;
import tberg.murphy.indexer.Indexer;

/**
 * @author Dan Garrette (dhgarrette@gmail.com)
 */
public class BasicGlyphSubstitutionModel implements GlyphSubstitutionModel {
	private static final long serialVersionUID = -8473038413268727114L;

	private Indexer<String> langIndexer;
	private Indexer<String> charIndexer;

	private int numChars;

	private double[/*language*/][/*lmChar*/][/*glyph*/] probs;
	private double gsmPower;

	public BasicGlyphSubstitutionModel(double[][][] probs,
			double gsmPower,
			Indexer<String> langIndexer,
			Indexer<String> charIndexer) {
		this.langIndexer = langIndexer;
		this.charIndexer = charIndexer;
		this.numChars = charIndexer.size();
		
		this.probs = probs;
		this.gsmPower = gsmPower;
	}

	public double glyphProb(int language, int lmChar, GlyphChar glyphChar) {
		GlyphType glyphType = glyphChar.glyphType;
		int glyph = (glyphType == GlyphType.NORMAL_CHAR) ? glyphChar.templateCharIndex : (numChars + glyphType.ordinal());
		double p = probs[language][lmChar][glyph];
        /*
        if (lmChar == 14 && p != 0) { // m
            System.out.println(charIndexer.getObject(lmChar));
            System.out.println(glyph);
            System.out.println(p);
            System.out.println("\n");
        }*/
		return Math.pow(p, gsmPower);
	}
	
	public Indexer<String> getLanguageIndexer() {
		return langIndexer;
	}

	public Indexer<String> getCharacterIndexer() {
		return charIndexer;
	}

	
	public static class BasicGlyphSubstitutionModelFactory {
		private double gsmSmoothingCount;
		private double elisionSmoothingCountMultiplier;
		private Indexer<String> langIndexer;
		private Indexer<String> charIndexer;
		private Set<Integer>[] activeCharacterSets;
		private Set<Integer> canBeReplaced;
		private Set<Integer> canBeDoubled;
		private Set<Integer> validSubstitutionChars;
		private Set<Integer> canBeElided;
        private Set<String> often_elided;
        private Map<String, String> special_glyphs;
		private Map<Integer,Integer> addTilde;
		private Map<Integer,Integer> diacriticDisregardMap;
		private int sCharIndex;
		private int longsCharIndex;
		private int fCharIndex;
		private int lCharIndex;
		private int dCharIndex;
		private int sCapCharIndex;
		private int tCharIndex;
		private int hyphenCharIndex;
		private int spaceCharIndex;
		
		private int numLanguages;
		private int numChars;
		private int numGlyphs;
		public final int GLYPH_ELISION_TILDE;
		public final int GLYPH_TILDE_ELIDED;
		public final int GLYPH_FIRST_ELIDED;
		public final int GLYPH_DOUBLED;
		public final int GLYPH_ELIDED;
		//public final int GLYPH_RMRGN_HPHN_DROP;
		
		private double gsmPower;
		private int minCountsForEvalGsm;
		
		private String outputPath;
		
		public BasicGlyphSubstitutionModelFactory(
				double gsmSmoothingCount,
				double elisionSmoothingCountMultiplier,
				Indexer<String> langIndexer,
				Indexer<String> charIndexer,
				Set<Integer>[] activeCharacterSets,
				double gsmPower, int minCountsForEvalGsm,
				String outputPath) {
			this.gsmSmoothingCount = gsmSmoothingCount;
			this.elisionSmoothingCountMultiplier = elisionSmoothingCountMultiplier;
			this.langIndexer = langIndexer;
			this.charIndexer = charIndexer;
			this.activeCharacterSets = activeCharacterSets;
			this.gsmPower = gsmPower;
			this.minCountsForEvalGsm = minCountsForEvalGsm;
			
            this.often_elided = makeSet("u", "m", "t", "o", "n", "i", "s", "e", "r");
            this.special_glyphs = makeMap(Tuple2("\uA75D", "r"), Tuple2("\u204A", "e"), Tuple2("\uA76F", "c"), Tuple2("\uA749", "l"), Tuple2("\uA753", "p"), Tuple2("\uA770", "u"), Tuple2("\uA751", "p"), Tuple2("\uA757", "q"));
			this.canBeReplaced = makeCanBeReplacedSet(charIndexer);
			this.canBeDoubled = makeValidDoublableSet(charIndexer);
			this.validSubstitutionChars = makeValidSubstitutionCharsSet(charIndexer);
			this.canBeElided = makeCanBeElidedSet(charIndexer);
			this.addTilde = makeAddTildeMap(charIndexer);
			this.diacriticDisregardMap = makeDiacriticDisregardMap(charIndexer);
			
			this.sCharIndex = charIndexer.contains("s") ? charIndexer.getIndex("s") : -1;
			this.longsCharIndex = charIndexer.getIndex(Charset.LONG_S);
			this.fCharIndex = charIndexer.contains("f") ? charIndexer.getIndex("f") : -1;
			this.lCharIndex = charIndexer.contains("l") ? charIndexer.getIndex("l") : -1;
			this.dCharIndex = charIndexer.contains("d") ? charIndexer.getIndex("d") : -1;
			this.sCapCharIndex = charIndexer.contains("S") ? charIndexer.getIndex("S") : -1;
			this.tCharIndex = charIndexer.contains("t") ? charIndexer.getIndex("t") : -1;
			this.hyphenCharIndex = charIndexer.getIndex(Charset.HYPHEN);
			this.spaceCharIndex = charIndexer.getIndex(Charset.SPACE);
			
			this.numLanguages = langIndexer.size();
			this.numChars = charIndexer.size();
			this.numGlyphs = numChars + GlyphType.values().length-1;
			this.GLYPH_ELISION_TILDE = numChars + GlyphType.ELISION_TILDE.ordinal();
			this.GLYPH_TILDE_ELIDED = numChars + GlyphType.TILDE_ELIDED.ordinal();
			this.GLYPH_FIRST_ELIDED = numChars + GlyphType.FIRST_ELIDED.ordinal();
			this.GLYPH_DOUBLED = numChars + GlyphType.DOUBLED.ordinal();
			//this.GLYPH_RMRGN_HPHN_DROP = numChars + GlyphType.RMRGN_HPHN_DROP.ordinal();
			this.GLYPH_ELIDED = numChars + GlyphType.ELIDED.ordinal();
			
			this.outputPath = outputPath;
		}
		
		public GlyphSubstitutionModel uniform() {
			return make(initializeNewCountsMatrix(), 0, 0);
		}
		
		/**
		 * Initialize the counts matrix. Add smoothing counts (and no counts for invalid options).
		 */
		public double[][][] initializeNewCountsMatrix() {
			double[/*language*/][/*lmChar*/][/*glyph*/] counts = new double[numLanguages][numChars][numGlyphs];
			for (int language = 0; language < numLanguages; ++language) {
				for (int lmChar = 0; lmChar < numChars; ++lmChar) {
					for (int glyph = 0; glyph < numGlyphs; ++glyph) {
						counts[language][lmChar][glyph] = getSmoothingValue(language, lmChar, glyph);
					}
				}
			}
			return counts;
		}
		
		
		public double getSmoothingValue(int language, int lmChar, int glyph) {
			
			
			if (!(activeCharacterSets[language].contains(lmChar) || lmChar == hyphenCharIndex)) return 0.0; // lm char must be valid for the language
            if (glyph == GLYPH_ELISION_TILDE) {
				if (addTilde.get(lmChar) == null) return 0.0; // an elision-tilde-decorated char must be elision-tilde-decoratable
				return gsmSmoothingCount * elisionSmoothingCountMultiplier;
			}
			else if (glyph == GLYPH_TILDE_ELIDED) {
				if (!canBeElided.contains(lmChar)) return 0.0; // an elided char must be elidable
				return gsmSmoothingCount * elisionSmoothingCountMultiplier;
			}
			else if (glyph == GLYPH_FIRST_ELIDED) {
				if (!canBeElided.contains(lmChar)) return 0.0; // an elided char must be elidable
				return gsmSmoothingCount * elisionSmoothingCountMultiplier;
			}
			else if (glyph == GLYPH_DOUBLED) {
				if (!canBeDoubled.contains(lmChar)) return 0.0; // a doubled character has to be doubleable
				return gsmSmoothingCount;// * elisionSmoothingCountMultiplier;
			}
			else if (glyph == GLYPH_ELIDED) {
				if (!canBeElided.contains(lmChar)) return 0.0; // an elided char must be elidable
    			return gsmSmoothingCount;
                /*
                if (often_elided.contains(charIndexer.getObject(lmChar))) {
                    return gsmSmoothingCount * 10000;
                } else {
    				return gsmSmoothingCount;
                }*/
			} 
            /*else if (charIndexer.getObject(lmChar).equals("m") || charIndexer.getObject(glyph).equals("m")) {
                return 0;
            }*/
			else { // glyph is a normal character
				Integer baseChar = diacriticDisregardMap.get(lmChar);
				if (baseChar != null && baseChar.equals(glyph))
					return gsmSmoothingCount * elisionSmoothingCountMultiplier;
				else if (lmChar == sCharIndex && glyph == longsCharIndex)
					return gsmSmoothingCount;
				else if (lmChar == sCharIndex && (glyph == fCharIndex || glyph == lCharIndex))
					return 0.0;
				else if (lmChar == dCharIndex && (glyph == sCapCharIndex || glyph == tCharIndex))
					return 0.0;
				else if (lmChar == hyphenCharIndex && glyph == spaceCharIndex) // so that line-break hyphens can be elided
					return gsmSmoothingCount;
				else if (canBeReplaced.contains(lmChar) && validSubstitutionChars.contains(glyph)) {
                    String g = charIndexer.getObject(glyph);
                    String c = charIndexer.getObject(lmChar);
                    /*
                    if (special_glyphs.containsKey(g)) {
                        System.out.println(">>" + special_glyphs.get(g) + "<< >>" + c + "<<\n");
                        System.out.println(special_glyphs.get(g).equals(c) + "\n");
                    }*/
                        
                    if (special_glyphs.containsKey(g) && special_glyphs.get(g).equals(c)){  
                        return gsmSmoothingCount * 10000;
                    }
                    else
    					return gsmSmoothingCount;
                }
				else if (lmChar == glyph)
					return gsmSmoothingCount;
				else
					return 0.0;
			}
			
		}
		
		/**
		 * Traverse the sequence of viterbi states, adding counts
		 */
		public void incrementCounts(double[/*language*/][/*lmChar*/][/*glyph*/] counts, List<DecodeState> fullViterbiStateSeq) {
			for (int i = 0; i < fullViterbiStateSeq.size(); ++i) {
				TransitionState currTs = fullViterbiStateSeq.get(i).ts;
				TransitionStateType currType = currTs.getType();
				if (currType == TransitionStateType.TMPL) {
					int language = currTs.getLanguageIndex();
					if (language >= 0) {
						int lmChar = currTs.getLmCharIndex();
						int glyph = glyphIndex(currTs.getGlyphChar());
						counts[language][lmChar][glyph] += 1;
					}
				}
				else if (currType == TransitionStateType.RMRGN_HPHN_INIT) {
					int language = currTs.getLanguageIndex();
					if (language >= 0) {
						GlyphChar currGlyphChar = currTs.getGlyphChar();
						if (currGlyphChar.templateCharIndex == spaceCharIndex) { // line-break hyphen was elided
							int glyph = glyphIndex(currGlyphChar);
							counts[language][hyphenCharIndex][glyph] += 1;
						}
					}
				}
			}
		}
		
		private int glyphIndex(GlyphChar glyphChar) {
			return glyphChar.glyphType == GlyphType.NORMAL_CHAR ? glyphChar.templateCharIndex : (numChars + glyphChar.glyphType.ordinal());
		}

		public BasicGlyphSubstitutionModel make(double[/*language*/][/*lmChar*/][/*glyph*/] counts, int iter, int batchId) {
			// Normalize counts to get probabilities
			double[/*language*/][/*lmChar*/][/*glyph*/] probs = new double[numLanguages][numChars][numGlyphs];
			for (int language = 0; language < numLanguages; ++language) {
				for (int prevLmChar = 0; prevLmChar < numChars; ++prevLmChar) {
					for (int lmChar = 0; lmChar < numChars; ++lmChar) {
						double sum = ArrayHelper.sum(counts[language][lmChar]);
						for (int glyph = 0; glyph < numGlyphs; ++glyph) {
							double c = counts[language][lmChar][glyph];
							double p = (c > 1e-9 ? (c / sum) : 0.0);
                            /*
                            if (glyph == 14 && lmChar == 14) { // m
                                System.out.println(charIndexer.getObject(lmChar));
                                System.out.println(charIndexer.getObject(glyph));
                                System.out.println(counts[language][lmChar][glyph]);
                                System.out.println(p);
                                System.out.println("\n");
                            }*/
							probs[language][lmChar][glyph] = p;
						}
					}
				}
			}
			
			System.out.println("Writing out GSM information.");
			synchronized (this) { printGsmProbs3(numLanguages, numChars, numGlyphs, counts, probs, iter, batchId, gsmPrintoutFilepath(iter, batchId)); }
			
			return new BasicGlyphSubstitutionModel(probs, gsmPower, langIndexer, charIndexer);
		}

		public BasicGlyphSubstitutionModel makeForEval(double[/*language*/][/*lmChar*/][/*glyph*/] counts, int iter, int batchId) {
			return makeForEval(counts, iter, batchId, minCountsForEvalGsm);
		}

		public BasicGlyphSubstitutionModel makeForEval(double[/*language*/][/*lmChar*/][/*glyph*/] counts, int iter, int batchId, double minCountsForEvalGsm) {
			if (minCountsForEvalGsm < 1) {
				System.out.println("Estimating parameters of a new Glyph Substitution Model.  Iter: "+iter+", batch: "+batchId);
				return make(counts, iter, batchId);
			}
			else {
				// Normalize counts to get probabilities
				double[/*language*/][/*lmChar*/][/*glyph*/] evalCounts = new double[numLanguages][numChars][numGlyphs];
				double[/*language*/][/*lmChar*/][/*glyph*/] probs = new double[numLanguages][numChars][numGlyphs];
				for (int language = 0; language < numLanguages; ++language) {
					for (int lmChar = 0; lmChar < numChars; ++lmChar) {
						
						for (int glyph = 0; glyph < numGlyphs; ++glyph) {
							double trueCount = counts[language][lmChar][glyph] - gsmSmoothingCount;
							if (trueCount < 1e-9)
								evalCounts[language][lmChar][glyph] = 0;
							else if (trueCount < minCountsForEvalGsm-1e-9)
								evalCounts[language][lmChar][glyph] = 0;
							else
								evalCounts[language][lmChar][glyph] = trueCount;
						}
						
						double sum = ArrayHelper.sum(evalCounts[language][lmChar]);
						for (int glyph = 0; glyph < numGlyphs; ++glyph) {
							double c = evalCounts[language][lmChar][glyph];
							double p = (c > 1e-9 ? (c / sum) : 0.0);
							probs[language][lmChar][glyph] = p;
						}
					}
				}
				
				System.out.println("Writing out GSM information.");
				synchronized (this) { printGsmProbs3(numLanguages, numChars, numGlyphs, counts, probs, iter, batchId, gsmPrintoutFilepath(iter, batchId)+"_eval"); }
	
				return new BasicGlyphSubstitutionModel(probs, gsmPower, langIndexer, charIndexer);
			}
		}

		private void printGsmProbs3(int numLanguages, int numChars, int numGlyphs, double[][][] counts, double[][][] probs, int iter, int batchId, String outputFilenameBase) {
			Set<String> CHARS_TO_PRINT = setUnion(makeSet(" ","-","a","b","c","d",Charset.LONG_S));
			StringBuffer sb = new StringBuffer();
			sb.append("language\tlmChar\tglyph\tcount\tminProb\tprob\n"); 
			for (int language = 0; language < numLanguages; ++language) {
				String slanguage = langIndexer.getObject(language);
				for (int lmChar = 0; lmChar < numChars; ++lmChar) {
					String slmChar = charIndexer.getObject(lmChar);
					
					// figure out what the lowest count is, and then exclude things with that count
					double lowProb = ArrayHelper.min(probs[language][lmChar]);
					for (int glyph = 0; glyph < numGlyphs; ++glyph) {
						String sglyph = glyph < numChars ? charIndexer.getObject(glyph) : GlyphType.values()[glyph-numChars].toString();
						
						double p = probs[language][lmChar][glyph];
						double c = counts[language][lmChar][glyph];
						if (c > gsmSmoothingCount || (CHARS_TO_PRINT.contains(slmChar) && (CHARS_TO_PRINT.contains(sglyph) || glyph >= numChars))) {
							//System.out.println("c="+c+", lang="+langIndexer.getObject(language)+"("+language+"), prevGlyphType="+prevGlyph+ ", prevLmChar="+charIndexer.getObject(prevLmChar)+"("+prevLmChar+"), lmChar="+charIndexer.getObject(lmChar)+"("+lmChar+"), glyphChar="+(glyph < numChars ? charIndexer.getObject(glyph) : (glyph == numGlyphs ? "EpsilonTilde": "Elided"))+"("+glyph+"), p="+p+", logp="+Math.log(p));
							sb.append(slanguage).append("\t");
							sb.append(slmChar).append("\t");
							sb.append(sglyph).append("\t");
							sb.append(c).append("\t");
							sb.append(lowProb).append("\t");
							sb.append(p).append("\t");
							sb.append("\n");
						}
					}
				}
			}
		
			String outputFilename = outputFilenameBase + ".tsv";
			System.out.println("Writing info about newly-trained GSM on iteration "+iter+", batch "+batchId+" out to ["+outputFilename+"]");
			FileHelper.writeString(outputFilename, sb.toString());
		}

		private String gsmPrintoutFilepath(int iter, int batchId) {
			String preext = "newGSM";
			String outputFilenameBase = outputPath + "/gsm/" + preext;
			if (iter > 0) outputFilenameBase += "_iter-" + iter;
			if (batchId > 0) outputFilenameBase += "_batch-" + batchId;
			return outputFilenameBase;
		}
	}

	public Indexer<String> getLangIndexer() { return langIndexer; }
	public Indexer<String> getCharIndexer() { return charIndexer; }
}
