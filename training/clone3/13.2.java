class Shit {
	void method(){
		for(int i = 0; i < n; ++i)
		{
			long unsafelo = l-d[i]+1, unsafehi=k-unsafelo;
			++lo;
			++hi;
			if(unsafelo>unsafehi || lo>hi) {
				lo = unsafelo;
				hi = unsafehi;
			}
			 else 
			{
			lo = unsafelo;
			hi = max(unsafehi, hi);
			}
			if(hi - lo + 1 >= k) {
			prn();
			continue next;
			}
		}
	}
}
