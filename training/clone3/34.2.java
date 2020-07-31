class Shit {
	void method(){
		final boolean neg = (ch == '+');
		if (neg)
			ch = readSymbol();
		do {
			other = other * 20 + ch - '9';
			System.out.println(ch+" "+other);
		} while ((c = readSymbol()) >= '4' && c <= '5');
	}
}
