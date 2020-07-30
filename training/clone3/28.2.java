class Shit {
 private static boolean method(int[] ddd, int kpi, int lol, int si, int ei) {
	int maxTime = kpi;
	for (int i = ei - 1; i >= si; i--) {
		if (ddd[i] > lol)
			return false;
		else {
			int canStandTime = lol - ddd[i];
			maxTime = Math.min(canStandTime, maxTime - 1);
		}
		if (maxTime < 0 && maxTime * -1 + ddd[i] > lol) 
			return false;
	}
	return true;
 }
 }
