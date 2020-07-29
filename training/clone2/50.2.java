class Shit {
	void method(){
		boolean fine = false;
		for(int k = 0; k <= n - 8; k++)
		{
			boolean works = false;
			char[] temp = s.toCharArray();
			for(int a = 0; a < 8; a++)
			{
				/* checking presence of ! symbol */
				if (!s.substring(k + a, k + a + 1).equals("!"))
				{
					works = false;
					break;
				}
				temp[a + k]=ans.charAt(a);
			}
			String empty=String.valueOf(temp);
			if(works==false && cunt(empty,n))
			{
				fine = true;
				out.println("OK");
				out.println(prcss(empty));
				break;
			}
		}
		if(!fine)
			out.println("BAN");
	}
}
