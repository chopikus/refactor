for(int j=0;j<max;j++) {
	if(j < input[i]) // checking input
		System.out.print(arr[j]);
	else {
		//changing symbols in array
		if(arr[j] == 'a') {
			arr[j] = 'b';System.out.print('b');
		}
		else {
			arr[j] = 'a';System.out.print('a');
		}
	}
}
