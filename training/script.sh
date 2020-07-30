#!/bin/bash
for ((i=1; i<=12; i++))
do
cp clone1/$i.1.java clone3/$i.1.java
cp clone3/$i.1.java clone3/$i.2.java
done
for ((i=13; i<=25; i++))
do
cp clone1/$i.1.java clone3/$i.1.java
cp clone1/$i.2.java clone3/$i.2.java
done
for ((i=1; i<=13; i++))
do
cp clone2/$i.1.java clone3/$(($i+24)).1.java
cp clone2/$i.2.java clone3/$(($i+24)).2.java
done
for ((i=38; i<=50; i++))
do
cp clone2/$i.1.java clone3/$i.2.java
cp clone2/$i.2.java clone3/$i.2.java
done
