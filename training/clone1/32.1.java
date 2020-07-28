long rest = m - n * a;
long b;
long c;
if (rest < 0) {
	b = l;
	c = l - rest;
} else {
	c = l;
	b = l + rest;
}

if (l <= b && b <= r && l <= c && c <= r && n > 0 && n * a + b - c == m) {
	System.out.println(a + " " + b + " " + c);
	return;
}
