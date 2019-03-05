package jp.ats.blackbox.test;

import com.google.gson.Gson;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.JournalHandler.JournalRegisterRequest;

public class JsonTest {

	public static void main(String[] args) {
		var gson = new Gson();
		String json = gson.toJson(JournalHandlerTest.createRequest(U.NULL_ID, U.NULL_ID));
		System.out.println(json);
		JournalRegisterRequest restored = gson.fromJson(json, JournalRegisterRequest.class);
		System.out.println(gson.toJson(restored));
	}
}
