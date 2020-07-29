class Shit {
	void method(){
		for(int qq = 1; qq <= q; qq++) {
			int i=nextInt()-1;
			int j=nextInt()-1;
			int jj=2*m-1 - j;
			if(i%2==1) 
			{
				TreeOdd.updateIndex(1,0,2*n-1, i,j);
				int max = TreeEven.queryRange(1, 0, 2*n-1,0, i);
				if(max >= jj){
					ok=false;
				}
			} 
		}
	}
}
