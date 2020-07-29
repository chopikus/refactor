class Shit {
	void method(){
		PII res=new PII(m,m);
		// pair
		for(int rr=0,j=m; j<m*2;++j){
			while(tr.get(rr).mid<=trains.get(j).mid-l) {
				++rr;
			}
			if(ans.mid>j-rr)
				ans=new PII(i-rr,tr.get(j).mid);
		}
		System.out.println(res.mid+"\n"+(res.id-k));
	}
}
