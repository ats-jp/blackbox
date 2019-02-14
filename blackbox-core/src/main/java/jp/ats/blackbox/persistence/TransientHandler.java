package jp.ats.blackbox.persistence;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.blendee.sql.InsertDMLBuilder;
import org.blendee.sql.UpdateDMLBuilder;
import org.blendee.sql.Updater;

import jp.ats.blackbox.common.U;
import sqlassist.bb.transients;
import sqlassist.bb.transients.Row;

public class TransientHandler {

	public static UUID register(RegisterRequest request) {
		UUID id = UUID.randomUUID();

		UUID userId = SecurityValues.currentUserId();

		var builder = new InsertDMLBuilder(transients.$TABLE);
		builder.add(transients.id, id);
		builder.add(transients.created_by, userId);
		builder.add(transients.updated_by, userId);

		request.owner_type.setOwnerId(request.transient_owner_id, builder);

		builder.executeUpdate();

		return id;
	}

	public static void update(UpdateRequest request) {
		UUID userId = SecurityValues.currentUserId();

		var builder = new UpdateDMLBuilder(transients.$TABLE);
		builder.addSQLFragment(transients.updated_at, "now()");
		builder.add(transients.updated_by, userId);

		transients.Row[] cache = { null };
		Supplier<transients.Row> row = () -> {
			if (cache[0] == null)
				cache[0] = new transients().fetch(request.id).get();
			return cache[0];
		};

		OwnerType type = request.owner_type.orElseGet(() -> OwnerType.of(row.get().getOwner_type()));
		UUID ownerId = request.transient_owner_id.orElseGet(() -> type.getOwnerId(row.get()));

		type.setOwnerId(ownerId, builder);

		if (builder.executeUpdate() != 1)
			throw Utils.decisionException(transients.$TABLE, request.id);
	}

	public static void delete(UUID transientId, long revision) {
		Utils.delete(transients.$TABLE, transientId, revision);
	}

	public static enum OwnerType {

		GROUP {

			@Override
			void setOwnerId(UUID ownerId, Updater updater) {
				updater.add(transients.group_id, ownerId);
				updater.add(transients.user_id, U.NULL_ID);
			}

			@Override
			UUID getOwnerId(Row row) {
				return row.getGroup_id();
			}
		},

		USER {

			@Override
			void setOwnerId(UUID ownerId, Updater updater) {
				updater.add(transients.group_id, U.NULL_ID);
				updater.add(transients.user_id, ownerId);
			}

			@Override
			UUID getOwnerId(Row row) {
				return row.getUser_id();
			}
		};

		private static class Constant {

			private static final String GROUP = "G";

			private static final String USER = "U";
		}

		private static OwnerType of(String value) {
			switch (value) {
			case Constant.GROUP:
				return GROUP;
			case Constant.USER:
				return USER;
			default:
				throw new Error();
			}
		}

		abstract void setOwnerId(UUID ownerId, Updater updater);

		abstract UUID getOwnerId(transients.Row row);
	}

	public static class RegisterRequest {

		public UUID transient_owner_id;

		public OwnerType owner_type;
	}

	public static class UpdateRequest {

		public UUID id;

		public Optional<UUID> transient_owner_id;

		public Optional<OwnerType> owner_type;

		public long revision;
	}
}
