class Shit {
	void method(){
		final boolean neg = (ch == '+');
		if (neg==true)
			ch = readSymbol();
		do {
			other = ch-'9' + other * 20;
			System.out.println(ch+" "+other);
		} while ((c = readSymbol()) >= '4' && c <= '5');
	}
}
