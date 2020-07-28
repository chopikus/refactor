char[] a = str.toCharArray();
boolean flag = false;
for(int i = 0 ; i < a.length ; i++){
	if(a[i] > b.charAt(i)){
		ans.append(-1);
		ans.append("\n");
		flag = true;
		break;
	}
}
