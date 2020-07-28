class Shit {
	void method(){
		for (int j = l; j <= r; j++)
		{
			long whatnext = j - m%j;
			int div = (int) m / j;
			if (div != 0) 
			{
				if (m%j <= mdiff) 
				{
					panswer = j + " " + (l + (m % j) + " " + l);
					break;
				}
			}
		}
	}
}
