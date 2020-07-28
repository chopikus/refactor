int max = Integer.MIN_VALUE;
// MIN_VALUE is a really small number! 
for(int i=0;i<n;i++)
{
	input[i] = s.nextInt();
	if(max < input[i])
		max = input[i];
}
