package marubinotto.piggydb.model.repository;

import marubinotto.piggydb.model.FileRepository;
import marubinotto.piggydb.model.Fragment;

public abstract class AbstractFileRepository implements FileRepository {

	protected static String getFragmentFileKey(Fragment fragment) { 
		String key = fragment.getId().toString();
		if (fragment.getFileType() != null) {
			key = key + "." + fragment.getFileType();
		}
		return key;
	}
}
