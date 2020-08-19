class Main
{
	void func1(double x, double y, double angle, int n)
	{
		for (int i=0; i<n; i++)
		{
			x+=angle;
			y+=angle;
		}
	}
	void func2(int n, double angle, double a, double b)
	{
		while (n--)
		{
			a+=angle;
			b+=angle;
		}
	}

	void other_func()
	{
		System.out.println("Hello World!");
	}
}
