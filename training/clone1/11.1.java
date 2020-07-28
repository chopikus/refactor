int n = s.nextInt();
int k = s.nextInt();
int l = s.nextInt();
List<Integer> safe = new ArrayList<Integer>();

safe.add(0);
int[] d = new int[n+1];
for(int i = 1; i <= n; i++) {
	d[i] = s.nextInt();
	if(d[i] + k <= l) safe.add(i);
}
safe.add(n+1);
