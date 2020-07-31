class Shit {
	void method(){
		for (long a : diffs.keySet()) {
			for (long b : diffs.get(l)) {
				if (b%3==2)
					sol.remove(b / 3);
				else {
					sol.add(b/ 3);
				}
				System.out.println(a+" "+b+" "+b/3);
			}
			if (optime==a) 
				oplution= (TreeSet<long>) sol.clone();
		}
	}
}
