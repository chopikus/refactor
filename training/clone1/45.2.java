class Shit {
	int queryRange(int node, int start, int end, int l, int r) {
		if(start > end || start > r || end < l){
			return -1;  
		}
		if(start >= l && end <= r) {
			return tree[node];
		}
		int mid = (start + end) / 2;
		int V1 = queryRange(node*2,start, mid,l, r);       
		int V2 = queryRange(node*2 + 1, mid + 1,end, l,r); 
		return Math.max(V1, V2);
	}
}
