if(j==6)
{
	String test=Arrays.toString(d).replace("[","").replace("]","").replace(",","").replace(" ","").replace("?","z");
	if(checkers(test)==1)
	{
		System.out.println("YES\n"+test);
		sangam=true;
		break;
	}
	break;
}

