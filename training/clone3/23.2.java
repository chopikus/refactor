class Shit {
	void method()	{
		int cnt = 0;
		//wtf is happening here 
		for (char ch : dp[rp.a]) 
		{
			System.out.println(ch+" "+cnt);
			if (ch>rp.a && ch<=rp.b) {
				System.out.println(rp.i+" "+ch+" "+rp.b);
				pq.add(new pair(rp.i, ch, rp.b));
				cnt++;
			}
		}
		if (cnt==0) 
		{
			dp[rp.a].add(rp.b);
			vis.add(rp.i);
		}
	}
}
