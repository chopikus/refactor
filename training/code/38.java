class Shit {
 void method(){
 for(int i = 0;i < n;i++){
	int L = xs[2*i+2];
	int R = xs[2*i+3];
	if(L <= R){
		if(L <= arg && arg < R){
			out.print((i+1) + " ");
		}
	}else{
		if(L <= arg || arg < R){
			out.print((i+1) + " ");
		}
	}
}
out.println();
 }
 }
