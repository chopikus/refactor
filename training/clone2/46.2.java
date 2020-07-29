class Shit {
	void updateIndex(int node, int start, int end, int index, int value) {
		if(start>end || start>index || end<index)    
			return;
		if(start>=index && end<=index) {           
			tree[node] = Math.max(value,tree[node]);
			return;
		}
		int mid = (start + end) / 2;
		updateIndex(node*2,start,mid,index,value);      
		updateIndex(node*2+1,mid+1,end,index,value);
		tree[node] = Math.max(tree[node*2], tree[node*2+1]);
	}
}
