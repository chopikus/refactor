int cnt = 0;

for (char ch : dp[rp.a]) {

	if (ch > rp.a && ch <= rp.b) {
		pq.add(new pair(rp.i, ch, rp.b));
		cnt++;
	}
}

if (cnt == 0) {
	ans++;
	dp[rp.a].add(rp.b);
	vis.add(rp.i);
}
