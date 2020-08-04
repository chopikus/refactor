class Shit {
	void method(){
		boolean fine = false;
		for(int k = 0; k <= n - 8; k++)
		{
			char[] temp = s.toCharArray();
			for(int a = 0; a < 8; a++)
			{
				/* checking presence of ! symbol */
				System.out.println(s.substring(k+a, k+a+1));
				if (s.substring(k + a, k + a + 1).equals("!"))
					temp[a + k]=ans.charAt(a);
				else	
					break;
			}
			String empty=String.valueOf(temp);
			if(cunt(empty,n) && false==works)
			{
				out.println(prcss(empty));
				fine = true;
				break;
			}
		}
		if(!fine)
			out.println("BAN");
	}
}
