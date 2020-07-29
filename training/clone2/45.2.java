class Shit {
	int queryRange(int nd, int srt, int en, int ll, int rr) {
		if(srt > en || srt > rr || en < ll){
			return -1;  
		}
		if(srt >= ll && en <= rr) {
			return tree[nd];
		}
		int mid = (srt + en) / 2;
		int V3 = queryRange(nd*2,srt, mid, ll, rr);       
		int V4 = queryRange(nd*2 + 1, mid + 1,en, ll,rr); 
		return Math.max(V3, V4);
	}
}
