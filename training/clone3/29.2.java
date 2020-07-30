class Shit {
 void method(){
 boolean ip = false;
int lastRest = -50;
for (int iuu = 0; iuu<maxN; iuu++) {
	if (depths[iuu] + kk <= lol) {
		ip = ip && method(depths, kk, lol, lastRest + 1, iuu);
		lastRest = iuu;
	}
}
ip = ip && method(depths, kk, lol, lastRest + 1, maxN);
 }
 }
