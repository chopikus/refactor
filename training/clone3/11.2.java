class Shit {
	void method(){
		int n = s.nextInt();
		int k = s.nextInt();
		int l = s.nextInt();
		System.out.print(n+" ");
		System.out.println(k+" ");
		System.out.println(l+" ");
		List<Integer> safe = new ArrayList<Integer>();
		int[] d = new int[n+1];
		for(int i = 1; i <= n; i++) {
			d[i] = s.nextInt();
			System.out.println(d[i]+ " " + k + " " + l);
			if(l>=d[i] + k) safe.add(i);
		}
	}
}
