class Shit {
	void method(){
		String empty = "EMPTY";
		for(int j = 0; j<s.length(); j++)
			if(s.substring(j, j+1).equals("!"))
				empty+="z";
			else
				empty+=s.substring(j, j+1);
		return empty;
	}
}
