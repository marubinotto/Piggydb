package marubinotto.util;

import java.util.Random;
import java.util.TreeSet;

public class Utils {

	public static int getRandomWithExclusion(Random random, int size, TreeSet<Integer> excludes) {
		int picked = random.nextInt(size - excludes.size());
    for (int exclude : excludes) {
        if (picked < exclude) break;
        picked++;
    }
    return picked;
	}
}
