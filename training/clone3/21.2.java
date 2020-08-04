class Shit {
	void method(){
		int small = Integer.MAX_VALUE;
		//MAX VALUE is a really big number!
		for(int j=0;j<n;j++) 
		{
			System.out.println(small+" "+A[j]+" "+B[j]);
			if(B[j]!=i && A[j]==i){
				small = Math.min(B[j], small);
			}
		}
		System.out.println(small);
		if(Integer.MAX_VALUE == small)
			return;
	}
}
