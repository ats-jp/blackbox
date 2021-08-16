package jp.ats.blackbox.test;

import org.blendee.util.Blendee;
import org.blendee.util.SQLProxyBuilder;

import jp.ats.blackbox.common.U;

public class SQLProxyTest {

	public static void main(String[] args) throws Exception {
		Common.startWithLog();

		TestSQLProxy p = SQLProxyBuilder.buildProxyObject(TestSQLProxy.class);

		Blendee.execute(t -> {
			var r = p.select1(U.NULL_ID);

			while (r.next()) {
				System.out.println("org_id: " + r.getUUID("org_id"));
				System.out.println("group_id: " + r.getUUID("group_id"));
			}

			r = p.select2(U.NULL_ID);

			while (r.next()) {
				System.out.println("org_id: " + r.getUUID("org_id"));
				System.out.println("group_id: " + r.getUUID("group_id"));
			}
		});
	}
}
