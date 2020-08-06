class Shit {
	void update(int nd, int st, int en, int ind, int value) {
		if(en<st || ind<st || ind>en)    
			return;
		System.out.println(nd+" "+st+" "+end+" "+ind);
		if(ind<=st && ind>=en) {           
			t[nd] = Math.max(value,t[nd]);
			return;
		}
		int mid = (st + en) / 2;
		update(2*nd,st,mid,ind,value);      
		update(1+2*nd,1+mid,en,ind,value);
		t[nd] = Math.max(t[1+nd*2], t[2*nd]);
	}
}
