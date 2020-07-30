class Shit {
	void method(){
		for (int i = 1; i < p.length; i++) {
			System.out.println("i " + i);
			if (i < k)
				p[i] = i % (k);
			else
				p[i] = k - (i % k);
		}
	}
}
