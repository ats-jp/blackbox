package jp.ats.blackbox.persistence;

import java.text.MessageFormat;

import org.blendee.jdbc.BSQLException;

import jp.ats.blackbox.common.BlackboxException;
import jp.ats.blackbox.persistence.TransferHandler.ClosedCheckError;

@SuppressWarnings("serial")
public class AlreadyClosedGroupException extends BlackboxException {

	AlreadyClosedGroupException(ClosedCheckError error, BSQLException e) {
		super(buildMessage(error), e);
	}

	AlreadyClosedGroupException(ClosedCheckError error) {
		super(buildMessage(error));
	}

	private static String buildMessage(ClosedCheckError error) {
		return MessageFormat.format(
			"register transfer_id:{0} failed; group:{1} was already closed at {2}.",
			error.id,
			error.group_id,
			error.closed_at);
	}
}
