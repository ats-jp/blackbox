package jp.ats.blackbox.test;

import com.google.gson.Gson;

import jp.ats.blackbox.persistence.TransferComponent.TransferRegisterRequest;

public class JsonTest {

	public static void main(String[] args) {
		var gson = new Gson();
		String json = gson.toJson(TransferHandlerTest.createRequest());
		System.out.println(json);
		TransferRegisterRequest restored = gson.fromJson(json, TransferRegisterRequest.class);
		System.out.println(gson.toJson(restored));
	}
}
