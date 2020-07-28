class Shit {
	void method(){
		for(int j = 0;j < max;j++) {
			//checking for input
			if(j < input[i])
				System.out.print(arr[j]);
			else {
				//changing letter in array
				if(arr[j] == 'a') {
					//to b
					arr[j] = 'b';
					System.out.print('b');
				}
				else {
					//to a
					arr[j] = 'a';
					System.out.print('a');
				}
			}
		}
	}
}
