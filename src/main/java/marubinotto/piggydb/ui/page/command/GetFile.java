package marubinotto.piggydb.ui.page.command;

import javax.servlet.http.HttpServletResponse;

import marubinotto.piggydb.model.Fragment;
import marubinotto.util.web.WebUtils;

public class GetFile extends AbstractCommand {

	public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

	public Long id;

	private Fragment fileFragment;

	@Override
	protected boolean needsAuthentication() {
		return false;
	}

	@Override
	protected boolean onPreInit() throws Exception {
		if (this.id == null) {
			getLogger().info("Missing parameter: id");
			return false;
		}

		this.fileFragment = getTargetFileFragment();
		if (this.fileFragment == null) return false;

		return true;
	}

	@Override
	protected void execute() throws Exception {
		HttpServletResponse response = getContext().getResponse();

		// Content-Type
		String mimeType = this.fileFragment.getMimeType();
		if (mimeType == null) {
			mimeType = DEFAULT_CONTENT_TYPE;
		}
		response.setContentType(mimeType);
		getLogger().info("ContentType: " + mimeType);

		// Content-Disposition (file name)
		WebUtils.setFileName(response, this.fileFragment.getFileName());

		// Content
		getDomain().getFileRepository().getFile(response.getOutputStream(), this.fileFragment);
		response.flushBuffer();
	}

	private Fragment getTargetFileFragment() throws Exception {
		Fragment fragment = getDomain().getFragmentRepository().get(this.id);
		if (fragment == null) {
			getLogger().info("Missing fragment: #" + this.id);
			return null;
		}
		if (!fragment.isFile()) {
			getLogger().info("Not file fragment: #" + this.id);
			return null;
		}
		if (!isAuthenticated() && !fragment.isPublic()) {
			getLogger().info("Forbidden: #" + this.id);
			setRedirectToLogin();
			return null;
		}
		return fragment;
	}
}
