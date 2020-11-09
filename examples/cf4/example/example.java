import java.lang.Math;

class Main
{
	static Thread yy = new Thread();
	static final double PI = 3.1415;
	enum Shit
	{
		ONE, TWO, THREE
	};

	static void veryImportantFunction()
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
	static void superComputing()
	{
		double aa = Math.cos(PI/2)-Math.sin(PI/2);
		double bb = Math.sin(PI/2)+Math.cos(PI/2);
		if (aa==345.0f || aa==0f){
			if (aa==345.0f)
				aa++;
			else if (aa==0.0f)
				aa++;
		}
		aa*=2;
		bb*=2;
	}
}
