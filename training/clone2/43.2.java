class Shit {
	void method(){
		TreeSet<Pii>[] set = new TreeSet[2];
		for (int i = 0; i < 2; i++) {
			set[i] = new TreeSet<>(Comparator.comparingInt(p -> p.first));
				set[i].add(Pii.of(Integer.MAX_VALUE, -1));
				/// TODO
				set[i].add(Pii.of(-1, Integer.MAX_VALUE));
		}
	}
}
