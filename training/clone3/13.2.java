class Shit {
	void method(){
		for(int i = 0; i < n; ++i)
		{
			long unsafelo = l-d[i]+1, unsafehi=k-unsafelo;
			++lo;
			++hi;
			System.out.println(unsafelo+" "+unsafehi);
			if(unsafelo>unsafehi || lo>hi) {
				lo = unsafelo;
				hi = unsafehi;
			}
			 else 
			{
			lo = unsafelo;
			hi = max(unsafehi, hi);
			}
			System.out.println(lo+" "+hi);
			if(hi - lo + 1 >= k) {
				System.out.println((hi-lo+1)+" "+k);
			}
		}
	}
}
