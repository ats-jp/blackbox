package jp.ats.blackbox.test;

import com.google.gson.Gson;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.TransferHandler.TransferRegisterRequest;

public class JsonTest {

	public static void main(String[] args) {
		var gson = new Gson();
		String json = gson.toJson(TransferHandlerTest.createRequest(U.NULL_ID, U.NULL_ID));
		System.out.println(json);
		TransferRegisterRequest restored = gson.fromJson(json, TransferRegisterRequest.class);
		System.out.println(gson.toJson(restored));
	}
}
