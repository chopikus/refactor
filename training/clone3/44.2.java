class Shit {
	void method(){
		for(int ab1 = 1; ab1 <= a; ab1++) {
			int i=nextInt()-2;
			int j=nextInt()-2;
			int jj= m*2-1-j;
			if(1==i%2) 
			{
				System.out.println(i+" "+j+" "+jj);
				Tree.update(1,0,2*n-1, i,j);
				int mx = Tree.query(1, 0, 2*n-1,0, i);
				if(jj <= mx)
					notOk = true;
				
			} 
		}
	}
}
