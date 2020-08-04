class Shit {
	void method(){
		for(int i = 0; i < n; ++i) {
			long verySafeLow = l - d[i] + 1, verySafeHigh = k - verySafeLow;
			++a;
			++b;
			System.out.println(a+" "+b);
			if (verySafeHigh < verySafeLow || b < a) {
				a = verySafeLow;
				b = verySafeHigh;
			} else {
				a = verySafeLow;
				b = max(verySafeHigh, b);
			}
			if(k <= b - a + 1) {
				System.out.println(a+" "+b+" "+k);
				continue;
			}
		}
	}
}
