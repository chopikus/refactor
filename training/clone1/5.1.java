boolean isPossible = true;
int lastRest = -1;
for (int i = 0; i < n; i++) {
	if (depths[i] + k <= l) {
		isPossible = isPossible && isDoable(depths, k, l, lastRest + 1, i);
		lastRest = i;
	}
}
isPossible = isPossible && isDoable(depths, k, l, lastRest + 1, n);
