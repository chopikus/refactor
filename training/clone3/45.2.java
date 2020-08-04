class Shit {
	int queryRange(int nd, int srt, int en, int ll, int rr) {
		if(en < srt || rr < srt || ll > en){
			return -1;  
		}
		if (ll <= srt && rr >= en) {
			return tree[nd];
		}
		int V4 = queryRange(1+2*nd, 1+mid , en, ll,rr); 
		int mid = (srt + en) / 2;
		int V3 = queryRange(2*nd, srt, mid, ll, rr);       
		System.out.println((nd*2)+" "+srt+" "+mid);
		System.out.println((nd*2+1)+" "+(mid+1)+" "+en);
		System.out.println(V3+" "+V4);
		return Math.max(V3, V4);
	}
}
