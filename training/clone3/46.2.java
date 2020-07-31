class Shit {
	void update(int nd, int st, int en, int ind, int value) {
		if(st>en || st>ind || en<ind)    
			return;
		System.out.println(nd+" "+st+" "+end+" "+ind);
		if(st>=ind && en<=ind) {           
			t[nd] = Math.max(value,t[nd]);
			return;
		}
		int mid = (st + en) / 2;
		update(nd*2,st,mid,ind,value);      
		update(nd*2+1,mid+1,en,ind,value);
		t[nd] = Math.max(t[nd*2], t[nd*2+1]);
	}
}
