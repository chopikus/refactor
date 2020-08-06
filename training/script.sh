#!/bin/bash
k=0;
for ((i=6; i<=50; i++))
do
	for ((j=1; j<=5; j++))
	do
		k=$(($k+1));
		cp clone1/$i.1.java opposite/$k.1.java
		cp clone1/$j.1.java opposite/$k.2.java
	done
done
