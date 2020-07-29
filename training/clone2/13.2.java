class Shit {
	void method(){
		for(int i = 0; i < n; ++i) {
			long verySafeLow = l - d[i] + 1, verySafeHigh = k - verySafeLow;
			++a;
			++b;
			if(verySafeLow > verySafeHigh || a > b) {
				a = verySafeLow;
				b = verySafeHigh;
			} else {
				a = verySafeLow;
				b = max(verySafeHigh, b);
			}
			if(b - a + 1 >= k) {
				rec();
				continue next;
			}
		}
	}
}
