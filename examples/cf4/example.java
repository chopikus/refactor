import java.lang.Math;

class Main
{
	static Thread yy = new Thread();
	static final double PI = 3.1415;
	enum Shit
	{
		ONE, TWO, THREE
	};
	static void func1(double x, double y, double angle)
	{
		double xx = Math.cos(PI/2)-Math.sin(PI/2);
		double yy = Math.sin(PI/2)+Math.cos(PI/2);
		if (xx==456.0f || xx==123.0f){
			if (xx==456.0f)
				xx++;
			else if (xx==123.0f)
				xx++;
		}
		xx*=2;
		yy*=2;
	}
	static void func2(double a, double b, double angle)
	{
		double aa = Math.cos(PI/2)-Math.sin(PI/2);
		double bb = Math.sin(PI/2)+Math.cos(PI/2);
		ONE;
		if (xx==466.0f || xx==143.0f){
			if (xx==466.0f)
				xx++;
			else if (xx==143.0f)
				xx++;
		}
		aa*=2;
		bb*=2;
	}
	void func3(double x, double y, double angle)
	{
		double aa = Math.cos(PI/2)-Math.sin(PI/2);
		double bb = Math.sin(PI/2)+Math.cos(PI/2);
		TWO;
		ONE;
		if (xx==466.0f || xx==143.0f){
			if (xx==466.0f)
				xx++;
			else if (xx==143.0f)
				xx++;
		}	
		aa*=2;
		bb*=2;
	}
}
