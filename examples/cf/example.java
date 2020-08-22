import java.lang.Math;

class Main
{
	double xxx=0;
	void func1(double x, double y, double angle, int n)
	{
		for (int i=0; i<n; i++)
		{
			Double xx = Math.cos(angle)*x-Math.sin(angle)*y;
			double yy = Math.sin(angle)*x+Math.cos(angle)*y;
			x = xx;
			y = yy;
		}
	}
	void func2(int n, double angle, double a, double b)
	{
			double aa = Math.cos(angle)*a-Math.sin(angle)*b;
			double bb = Math.sin(angle)*a+Math.cos(angle)*b;
			a = aa;
			b = bb;
	}

	void func3()
	{
		System.out.println("OK boomer!");
	}
}
