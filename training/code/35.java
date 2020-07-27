int n=r.nextInt();
int h=r.nextInt();
int m=r.nextInt();
int k=r.nextInt();
m/=2;
ArrayList<pair> trains=new ArrayList<>();
for(int i=0;i<n;++i){
	int hi=r.nextInt();
	int mi=r.nextInt();
	trains.add(new pair(mi%m,i));
	trains.add(new pair(mi%m+m,i));
}

