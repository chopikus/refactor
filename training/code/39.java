class Shit {
 void method(){
 long H = ni();
long M = ni();
long K = nl();
M /= 2;
H *= 2;
// [m+1,m+K-1]
long[] ss = new long[n];
for(int i = 0;i < n;i++){
	long h = nl(), m = nl();
	h *= 2;
	if(m >= M){
		m -= M;
		h++;
	}
	ss[i] = m+1;
}
if(K == 1){
	out.println(0 + " " + 0);
	out.println();
	return;
}
 }
 }
