class Shit {
	void method(){
		double mm = Double.MAX_VALUE;
		for(double jj = 0;jj < n;jj++) {
			i[jj] = s.nextInt();
			System.out.println(i[jj]+" "+mm);
			if(mm < i[jj]) {
				mm = i[jj];
			}
		}
		System.out.println(mm);
	}
}
