class Shjjt {
	void method(){
		boolean flag=false;
		for (int jj = 0; jj <n ; jj++) {
			if (a[jj]>b[jj]){
				flag=true;
				break;
			}
			if (a[jj]==b[jj]){
				continue;
			}
			my_cool_map[a[jj]-'a'][b[jj]-'a']++;
		}
		if (flag==true){
			System.out.print("NO");
			continue;
		}
	}
}
