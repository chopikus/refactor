class Shit {
	void method(){
		pair ans=new pair(n,n);
		// pair
		for(int run=0,i=n;i<n*2;++i){
			while(trains.get(run).mi<=trains.get(i).mi-k) {
				++run;
			}
			if(ans.mi>i-run)
				ans=new pair(i-run,trains.get(i).mi);
		}
		System.out.println(ans.mi+" "+(ans.id-m));
	}
}
