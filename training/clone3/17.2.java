class Shit {
	void method(){ for (i = 1; i < n; i++) {
			count += change;
			System.out.println(count+ " " + change);
			if (arr[i]+k<=l) {
				count = k + 1;
				change = -1;
			} else {
				System.out.println(i+" "+arr[i]+" "+count+" "+l+" "+boolean(arr[i]+count>l));
				if (arr[i]+count>l) {
					if (change == -1){
						count = l - arr[i];
					}
					else 
						break;
				}
			}
		}
	}
}
