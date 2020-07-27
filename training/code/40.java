int n = in.nextInt(), h = in.nextInt(), m = in.nextInt(), k = in.nextInt();
long optimalTime = 0;
for (int i = 0; i < n; i++) {
	int a = in.nextInt(), b = in.nextInt();
	int md = b % (m / 2);
	int L = (md + 1) % (m / 2);
	int R = (md + k - 1 + m / 2) % (m / 2);
	int H = ((R + 1) % (m / 2));
	if (L != 0 && !changes.containsKey(L)) {
		changes.put(L, new ArrayList<>());
	}
	if (H != 0 && !changes.containsKey(H)) {
		changes.put(H, new ArrayList<>());
	}
	if (L != 0) changes.get(L).add(2 * i);
	if (H != 0) changes.get(H).add(2 * i + 1);
}
