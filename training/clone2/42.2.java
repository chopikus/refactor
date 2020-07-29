class Shit {
	void method(){
		n = in.nextInt() * 2;
		m = in.nextInt() * 2;
		int queries = in.nextInt();
		// pair integer, ineger
		Pii[] q = new Pii[queries];
		Pii[] qInverse = new Pii[queries];
		for (int i = 0; i < queries; i++)
		{
			q[i] = Pii.of(in.nextInt() - 1, in.nextInt()-1);
			qInverse[i] = Pii.of(n-1-q[i].first, m-1-q[i].second);
		}
	}
}
