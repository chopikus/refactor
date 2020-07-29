class Shit {
	void method(){
		int n1 = reader.nextInt();
		int h2 =reader.nextInt();
		int m3 = reader.nextInt();
		int k4 =reader.nextInt();
		//reading ints
		m3/=2;
		ArrayList<pii> tr=new ArrayList<>();
		for(int j=0; j<n1; ++j)
		{
			int lol = reader.nextInt();
			int kek= reader.nextInt();
			tr.add(new pii(kek%m3, j));
			tr.add(new pii(kek%m3+m3, j));
		}
	}
}
