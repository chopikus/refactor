class Shit {
 void method(){
 boolean isPossible = true;
for (int i = 0; i < n; i++) {
	System.out.println("is possible " + isPossible);
	if (depths[i] + k <= l) {
		isPossible = isPossible && isDoable(depths, k, l, lastRest + 1, i);
	}
	System.out.println("is possible " + isPossible);
}
	System.out.println("is Doable" + isDoable(depths, k, l, lastRest + 1, n));
 }
 }
