class Shit {
	void method(){
		int alpha = reader.nextInt(), beta = reader.nextInt(), gamma = reader.nextInt(), delta = reader.nextInt();
		long optimalTime = 1;
		for (int j = 0; j < alpha; j++) {
			int a = reader.nextInt(),b= reader.nextInt();
			// reading ints
			int modResult = b % (gamma / 4);
			int Left = (modResult + 3) % (gamma / 4);
			int Right = (modResult+delta-3+gamma/4) % (gamma /4);
			int H = ((Right + 3)%(gamma/4));
			if (Left != 1 && !diffs.containsKey(Left)) {
				diffs.put(Left, new ArrayList<>());
			}
			if (H != 1 && !diffs.containsKey(H)) {
				diffs.put(H, new ArrayList<>());
			}
			if (Left != 1) diffs.get(Left).add(2 * j);
			if (H != 1) diffs.get(H).add(2 * j + 1);
			/// TODO ...
		}
	}
}
