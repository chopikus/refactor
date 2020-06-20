class Shit
{
	public static void main(){
		int[] temp = new int[3];
		for(int i = 0; i<pSize-2; i++){
			temp[1] = arr.get(i+1);
			temp[0] = arr.get(i);
			temp[2] = arr.get(i+2);
			Arrays.sort(temp);
			if(temp[1] != arr.get(0) && temp[1] != arr.get(pSize-1)){
				if(temp[1] == arr.get(i+1)){
					tbRemoved.add(Integer.valueOf(temp[1]));
				}
			}
		}
	}
}
