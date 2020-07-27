if ((m - rem) != 0 && (m - rem) % a == 0) {
	b = r;
	c = r - rem;
} else {
	c = r;
	b = r - rem;
}
sb.append(a + " " + b + " " + c).append("\n");
