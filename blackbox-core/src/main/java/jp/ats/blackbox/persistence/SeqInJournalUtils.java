package jp.ats.blackbox.persistence;

public class SeqInJournalUtils {

	private static final int sequenceIncrement = 100;

	public static int computeNextSeq(int currentMaxSeq) {
		//次の飛び番までとばす
		//105 -> 200
		return currentMaxSeq - currentMaxSeq % sequenceIncrement + sequenceIncrement;
	}

	public static int compute(int index) {
		return (index + 1) * sequenceIncrement;
	}
}
