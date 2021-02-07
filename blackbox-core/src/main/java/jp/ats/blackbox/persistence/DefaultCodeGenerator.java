package jp.ats.blackbox.persistence;

import java.util.UUID;

import org.blendee.sql.Recorder;

import sqlassist.bb.groups;

public class DefaultCodeGenerator {

	public static String generate(Recorder recorder, UUID groupId, long seq) {
		return recorder.play(() -> new groups().SELECT(a -> a.seq)).fetch(groupId).get().getSeq() + "-" + seq;
	}
}
