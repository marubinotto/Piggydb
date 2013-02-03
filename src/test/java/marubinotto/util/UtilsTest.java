package marubinotto.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import org.junit.Test;

public class UtilsTest {

  @Test
  public void getRandomWithExclusion() throws Exception {
    List<Integer> results = new ArrayList<Integer>();
    TreeSet<Integer> excludes = new TreeSet<Integer>();
    int size = 10;
    Random random = new Random();
    for (int i = 0; i < size; i++) {
      int picked = Utils.getRandomWithExclusion(random, size, excludes);
      excludes.add(picked);
      results.add(picked);
    }
    System.out.println(results);
  }
}
