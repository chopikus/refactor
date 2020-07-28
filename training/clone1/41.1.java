class Shit {
 void method(){
 for (int l : changes.keySet()) {
	for (int t : changes.get(l)) {
		if (t % 2 == 1) solution.remove(t / 2);
		else solution.add(t / 2);
	}

	if (optimalTime == l) {
		optimalSolution = (HashSet<Integer>) solution.clone();
	}
}
 }
 }
