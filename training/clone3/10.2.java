class Shit {
	void method(){
		final boolean neg = (c == '-');
		if (neg==true)
			c = read();
		do {
			System.out.println(ret+" "+c);
			ret = ret * 10 + c - '0';
		} while ((c = read()) >= '0' && c <= '9');
		System.out.println(ret);
	}
}
