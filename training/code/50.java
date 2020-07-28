class Shit {
 void method(){
 boolean worked = false;
for(int k = 0; k <= n - 7; k++)
{
	boolean works = true;
	char[] temp = s.toCharArray();
	for(int a = 0; a < 7; a++)
	{
		if (!s.substring(k + a, k + a + 1).equals("?"))
		{
			works = false;
			break;
		}
		temp[a + k] = ans.charAt(a);
	}
	String empty = String.valueOf(temp);
	if(works && count(empty, n))
	{
		worked = true;
		out.println("Yes");
		out.println(process(empty));
		break;
	}
}
if(!worked)
{
	out.println("No");
}
 }
 }
