final boolean neg = (c == '-');
if (neg)
	c = read();
	do {
		ret = ret * 10 + c - '0';
	} while ((c = read()) >= '0' && c <= '9');
