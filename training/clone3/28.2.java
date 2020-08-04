class Shit {
 private static boolean method(int[] ddd, int kpi, int lol, int si, int ei) {
	int maxTime = kpi;
	for (int i=0; i<ddd.length; i++)
		System.out.print(ddd[i]+" ");
	System.out.println();
	System.out.print(kpi+" "+lol+" "+si+" "+ei);
	for (int i = ei - 1; i >= si; i--) {
		System.out.println(i+" "+ddd[i]);
		int canStandTime = lol - ddd[i];
		if (ddd[i] > lol)
			return false;
		else {
			maxTime = Math.min(maxTime-1, canStandTime);
			System.out.println(canStandTime+" "+maxTime);
		}
		if (ddd[i] + maxTime * -1 > lol && 0 > maxTime) 
			return false;
	}
	return true;
 }
 }
