class Shit {
 void method(){
 int flag=0;
for (int i = 0; i <n ; i++) {
	if (a[i]>b[i]){
		flag=1;
		break;
	}
	if (a[i]==b[i]){
		continue;
	}
	map[a[i]-'a'][b[i]-'a']++;

}
if (flag==1){
	System.out.println(-1);
	continue;
}
 }
 }
