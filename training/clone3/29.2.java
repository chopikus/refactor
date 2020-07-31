class Shit {
	void method(){
		boolean ip = false;
		int lastRest = -50;
		for (int iuu = 0; iuu<maxN; iuu++) {
			System.out.println(depths[iuu]+" "+kk+" "+lol);
			if (depths[iuu] + kk <= lol) {
				System.out.println(depths+" "+kk+" "+lol+" "+lastRest+1+" "+iuu+" "+get(depths, kk, lol, lastRest + 1, iuu));
				ip = ip && get(depths, kk, lol, lastRest + 1, iuu);
				lastRest = iuu;
			}
		}
		System.out.println(depths+" "+kk+" "+lol+" "+lastRest+1+" "+get(depths, kk, lol, lastRest + 1, iuu));
		ip = ip && get(depths, kk, lol, lastRest + 1, maxN);
	}
}
