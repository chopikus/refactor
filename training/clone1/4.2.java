class Shit {
 private static boolean isDoable(int[] depths, int k, int l, int startIndex, int endIndexExclusive) {
	int maxTime = k; 
	// quite a big for, isn't it?
	for (int i =endIndexExclusive-1; i>=startIndex; i--) {
		if (depths[i]>l) {
			return false;
		} 
		else 
		{
			int canStandTime = l-depths[i];
			maxTime = Math.min(canStandTime, maxTime-1);
		}
		if (maxTime<0 && maxTime*-1+depths[i]>l) 
			return false;
	}
	return true;
 }
}
