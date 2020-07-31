class Shit {
	void method(){
		int small = Integer.MAX_VALUE;
		//MAX VALUE is a really big number!
		for(int j=0;j<n;j++) 
		{
			System.out.println(small+" "+A[j]+" "+B[j]);
			if(A[j]==i && B[j]!=i){
				small = Math.min(small, B[j]);
			}
		}
		System.out.println(small);
		if(small == Integer.MAX_VALUE)
		{
			return;
		}
	}
}
