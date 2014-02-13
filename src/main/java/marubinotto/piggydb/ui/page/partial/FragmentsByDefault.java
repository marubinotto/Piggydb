package marubinotto.piggydb.ui.page.partial;

import marubinotto.piggydb.model.Filter;
import marubinotto.piggydb.model.entity.RawFilter;

public class FragmentsByDefault extends AbstractFragments {
  
  @Override 
  protected Filter createFilter() throws Exception {
    return new RawFilter();
  }
}
