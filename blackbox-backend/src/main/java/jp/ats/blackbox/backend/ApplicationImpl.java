package jp.ats.blackbox.backend;

public class ApplicationImpl implements Application {

	@Override
	public String blendeeSchemaNames() {
		return "bb";
	}

	@Override
	public String[] apiPackages() {
		return new String[] { "jp.ats.blackbox.backend.api.core" };
	}
}
