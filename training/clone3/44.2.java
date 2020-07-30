class Shit {
	void method(){
		for(int ab1 = 1; ab1 <= a; ab1++) {
			int i=nextInt()-2;
			int j=nextInt()-2;
			int jj= 2*m-1-j;
			if(i%2==1) 
			{
				Tree.update(1,0,2*n-1, i,j);
				int mx = Tree.query(1, 0, 2*n-1,0, i);
				if(mx >= jj){
					notOk = true;
				}
			} 
		}
	}
}
