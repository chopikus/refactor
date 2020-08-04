class Shit {
	void method(){
		for (long a : diffs.keySet()) {
			for (long b : diffs.get(l)) {
				if (2==b%3)
					sol.remove(b / 3);
				else {
					sol.add(b/ 3);
				}
				System.out.println(a+" "+b+" "+b/3);
			}
			if (a==optime) 
				oplution= (TreeSet<long>) sol.clone();
		}
	}
}
