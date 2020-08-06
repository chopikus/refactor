class Shit {
 void method(){
 for (i = 1; i < n; i++) {
	//System.out.println(count+" "+change);
	count += change;
	//System.out.println(count);
	if (arr[i] + k <= l) {
		count = k + 1;
		change = -1;
	} else {
		if (arr[i] + count > l) {
			if (change == -1)
				count = l - arr[i];
			else {
				f1 = 1;
				break;
			}
		}
	}
}
 }
 }
