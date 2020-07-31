class Shit {
 private static boolean method(int[] ddd, int kpi, int lol, int si, int ei) {
	int maxTime = kpi;
	for (int i=0; i<ddd.length; i++)
		System.out.print(ddd[i]+" ");
	System.out.println();
	System.out.print(kpi+" "+lol+" "+si+" "+ei);
	for (int i = ei - 1; i >= si; i--) {
		System.out.println(i+" "+ddd[i]);
		if (ddd[i] > lol)
			return false;
		else {
			int canStandTime = lol - ddd[i];
			maxTime = Math.min(canStandTime, maxTime - 1);
			System.out.println(canStandTime+" "+maxTime);
		}
		if (maxTime < 0 && maxTime * -1 + ddd[i] > lol) 
			return false;
	}
	return true;
 }
 }
