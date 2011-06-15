package marubinotto.piggydb.ui.page.html;

public class FragmentNodeContent extends AbstractOneFragment {

	public Long ctxRelationId;
	
	@Override 
	public void onRender() {
		super.onRender();
		getLogger().debug("ctxRelationId: " + ctxRelationId);
	}
}
