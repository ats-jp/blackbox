package jp.ats.blackbox.test;

import java.util.Optional;

import org.blendee.util.Blendee;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.persistence.CycleGroupException;
import jp.ats.blackbox.persistence.GroupHandler;

public class RelationshipTest {

	public static void main(String[] args) {
		Common.startWithLog();

		register();

		update();

		cycleTest1();

		cycleTest2();
	}

	private static void register() {
		var group0 = GroupHandlerTest.register();
		var group1 = GroupHandlerTest.register(group0);
		var group2 = GroupHandlerTest.register(group1);
		GroupHandlerTest.register(group2);
	}

	private static void update() {
		var group0 = GroupHandlerTest.register();
		var group1 = GroupHandlerTest.register(group0);
		var group2 = GroupHandlerTest.register(group1);
		GroupHandlerTest.register(group2);

		var group4 = GroupHandlerTest.register();

		var req = new GroupHandler.UpdateRequest();

		req.parent_id = Optional.of(group4);
		req.revision = 0;
		req.id = group1;

		Blendee.execute(t -> {
			GroupHandler.update(req);
		});
	}

	private static void cycleTest1() {
		var group0 = GroupHandlerTest.register();
		var group1 = GroupHandlerTest.register(group0);
		var group2 = GroupHandlerTest.register(group1);
		var group3 = GroupHandlerTest.register(group2);

		var req = new GroupHandler.UpdateRequest();

		req.parent_id = Optional.of(group3);
		req.revision = 0;
		req.id = group1;

		Blendee.execute(t -> {
			try {
				GroupHandler.update(req);
				throw new Error();
			} catch (CycleGroupException e) {}
		});
	}

	private static void cycleTest2() {
		var group0 = GroupHandlerTest.register();
		var group1 = GroupHandlerTest.register(group0);
		var group2 = GroupHandlerTest.register(group1);
		GroupHandlerTest.register(group2);

		var req = new GroupHandler.UpdateRequest();

		req.parent_id = Optional.of(U.NULL_ID);
		req.revision = 0;
		req.id = group1;

		Blendee.execute(t -> {
			GroupHandler.update(req);
		});
	}
}
