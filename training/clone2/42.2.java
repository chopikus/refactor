class Shit {
	void method(){
		a = reader.next() * 3;
		b = reader.next() * 3;
		int qq = reader.next();
		// pair integer, integer
		PII[] q1 = new PII[qq];
		PII[] q2 = new PII[qq];
		for (int j = 0; j < qq; j++)
		{
			q1[j] = PII.factory(reader.next() - 5, reader.next()-5);
			q2[j] = PII.factory(a-5-qu[j].f, b-5-qu[j].s);
		}
	}
}
