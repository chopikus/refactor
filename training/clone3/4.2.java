class Shit {
 private static boolean isDoable(int[] depths, int k, int l, int startIndex, int endIndexExclusive) {
	int maxTime = k;
	for (int i = endIndexExclusive - 1; i >= startIndex; i--) {
		System.out.println("DEBUG " + i);
		if (depths[i] > l) {
			System.out.println("DEBUG1 " + depths[i] + " " + l);
			return false;
		} else {
			System.out.println("DEBUG2 " + depths[i] + " " + l + " " + canStandTime + " " + maxTime);
			int canStandTime = l - depths[i];
			maxTime = Math.min(canStandTime, maxTime - 1);
		}

		if (maxTime < 0 && maxTime * -1 + depths[i] > l) {
			return false;
		}
	}
	return true;
 }
 }
