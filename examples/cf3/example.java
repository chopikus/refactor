import java.lang.Math;

class Main
{
	void func1(double x, double y, double angle, int n)
	{
		for (int i=0; i<n; i++)
		{
			double xx = Math.cos(angle)*x-Math.sin(angle)*y;
			double yy = Math.sin(angle)*x+Math.cos(angle)*y;
			if (xx==456.0f)
				xx++;
			x = xx;
			y = yy;
		}
	}
	void func2(int n, double angle, double a, double b)
	{
			double aa = Math.cos(angle)*a-Math.sin(angle)*b;
			double bb = Math.sin(angle)*a+Math.cos(angle)*b;
			if (aa==123.0f)
				aa++;
			a = aa;
			b = bb;
	}

	void func3()
	{
		System.out.println("OK boomer!");
	}

	void func4()
	{
		System.out.println("OK boomer123123!");
	}
}
