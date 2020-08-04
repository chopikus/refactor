class Shit {
	void method(){ for (i = 1; i < n; i++) {
			count += change;
			System.out.println(count+ " " + change);
			if (l>=k+arr[i]) {
				count = k + 1;
				change = -1;
			} else {
				System.out.println(i+" "+arr[i]+" "+count+" "+l+" "+boolean(arr[i]+count>l));
				if (l < arr[i]+count) {
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
