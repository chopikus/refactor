class Shit {
 void updateIndex(int node, int start, int end, int index, int value) {
        if(start > end || start > index || end < index)              // Current segment is not within range [l, r]
            return;
        if(start >= index && end <= index) {            // Segment is fully within range
            tree[node] = Math.max(value, tree[node]);
            return;
        }
        int mid = (start + end) / 2;
        updateIndex(node*2, start, mid, index, value);        // Updating left child
        updateIndex(node*2 + 1, mid + 1, end, index, value);   // Updating right child
        tree[node] = Math.max(tree[node*2], tree[node*2+1]);
    }
}
