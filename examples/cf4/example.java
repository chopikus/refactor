import java.lang.Math;

class Main
{
	void func1(double x, double y, double angle)
	{
		double xx = Math.cos(angle)*x-Math.sin(angle)*y;
		double yy = Math.sin(angle)*x+Math.cos(angle)*y;
		if (xx==456.0f || xx==123.0f)
			xx++;
		x = xx;
		y = yy;
		yy--;
	}
	void func2(double a, double b, double angle)
	{
		double aa = Math.cos(angle)*a-Math.sin(angle)*b;
		double bb = Math.sin(angle)*a+Math.cos(angle)*b;
		if (xx==5578.0 || xx==0)
			xx++;
		a = aa;
		b = bb;
		bb--;
	}
	void func5(double a, double b, double angle)
	{
		double xx = Math.cos(angle)*x-Math.sin(angle)*y;
		double yy = Math.sin(angle)*x+Math.cos(angle)*y;
		if (xx==678.0f)
			xx++;
		x = xx;
		y = yy;
		print("hello");
	}

	void func3()
	{
		for (func1();func2();func5())
			System.out.println("OK boomer!");
	}

	void func4()
	{
		System.out.println("Hello, World!");
	}
}
