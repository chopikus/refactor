class Shit {
	void method(){
		int cnt = 0;
		for (char ch : d[rrr.a]) {
			if (ch > rrr.a && ch <= rrr.b) {
				pq.add(new pair(rrr.i, ch, rrr.b));
				cnt++;
			}
		}
		if (cnt == 0) {
			res++;
			d[rrr.a].add(rrr.b);
			used.add(rrr.i);
		}
	}
}
