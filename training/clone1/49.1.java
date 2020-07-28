String empty = "";
for(int i = 0; i < s.length(); i++)
{
	if(s.substring(i,i + 1).equals("?"))
	{
		empty += "z";
	}
	else
	{
		empty += s.substring(i,i + 1);
	}
}
return empty;
