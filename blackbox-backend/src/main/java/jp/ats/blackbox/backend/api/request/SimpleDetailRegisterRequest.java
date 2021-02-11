package jp.ats.blackbox.backend.api.request;

import java.util.Optional;

public class SimpleDetailRegisterRequest {

	/**
	 * 追加情報JSON
	 */
	public Optional<Object> props = Optional.empty();

	/**
	 * 配下のnode
	 * 必須
	 */
	public SimpleNodeRegisterRequest[] nodes;
}
