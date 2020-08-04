class Shit {
	void method()
	{
		System.out.println(a+"\n"+b);
		if(a.contains(b))
		{
			System.out.println(a.indexOf(b));
			if(a.lastIndexOf(b) != a.indexOf(b))
				return 4;
			else
				return 3;
		}
		return 5;
	}
}
