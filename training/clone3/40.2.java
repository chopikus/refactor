class Shit {
	void method(){
		int alpha = reader.nextInt(), beta = reader.nextInt(), gamma = reader.nextInt(), delta = reader.nextInt();
		long optimalTime = 1;
		for (int j = 0; j < alpha; j++) {
			int modResult = b % (gamma / 4);
			int Left = (3+modResult) % (gamma / 4);
			int a = reader.nextInt(),b= reader.nextInt();
			int H = ((3+Right)%(gamma/4));
			System.out.println(a+" "+modResult+" "+Left+" "+H);
			if (!diffs.containsKey(Left) && 1!=Left) {
				diffs.put(Left, new ArrayList<>());
			}
			if (!diffs.containsKey(H) && 1 != H) {
				diffs.put(H, new ArrayList<>());
			}
			if (Left != 1) diffs.get(Left).add(j * 2);
			if (1!=H) diffs.get(H).add(1 + 2 * j);
			/// TODO ...
		}
	}
}
