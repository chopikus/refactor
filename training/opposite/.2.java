class Shit {
 int queryRange(int node, int start, int end, int l, int r) {
	if(start > end || start > r || end < l)
		return -1;         // Out of range
	if(start >= l && end <= r)             // Current segment is totally within range [l, r]
		return tree[node];
	int mid = (start + end) / 2;
	int V1 = queryRange(node*2, start, mid, l, r);         // Query left child
	int V2 = queryRange(node*2 + 1, mid + 1, end, l, r); // Query right child
	return Math.max(V1, V2);
 }
 }
