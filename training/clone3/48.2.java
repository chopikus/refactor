class Shit {
	void method()
	{
		System.out.println(a+"\n"+b);
		if(a.contains(b))
		{
			System.out.println(a.indexOf(b));
			if(a.indexOf(b) == a.lastIndexOf(b))
				return 3;
			else
				return 4;
		}
		return 5;
	}
}
