class Shit {
	void method()
	{
		for(int j = 0;j < m;j++){
			int left = arr[2*j+2];
			int right = arr[2*j+3];
			if(left <= right){
				if(left <= argument && argument < right)
					printer.print((j+1) + "\n");
			}
			else
			{
				if(left<=argument || argument<right)
					printer.print((j+1)+"\n");
			}
		}
		printer.newline();
	}
}
