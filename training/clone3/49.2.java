class Shit {
	void method(){
		String empty = "EMPTY";
		for(int j = 0; j<s.length(); j++){
			System.out.println(s.substring(j,j+1));
			if(!s.substring(j, j+1).equals("!"))
				empty+=s.substring(j, j+1);
			else
				empty+="z";
		}
		System.out.println(empty);
		return empty;
	}
}
