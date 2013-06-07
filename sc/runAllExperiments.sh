#! /bin/bash


PROFILE="/home/alekh/tpcc_profile/postgres_experiments"

rm $PROFILE
echo "1200 20000 45,43,4,4,4" >> $PROFILE
echo "1200 1 45,43,4,4,4" >> $PROFILE

# experiment 1
for i in {100..1500..100}
do
  echo "900 $i 45,43,4,4,4" >> $PROFILE
  echo "600 1 45,43,4,4,4" >> $PROFILE
done


# experiment 2
for i in {100..1500..100}
do
  echo "900 $i 0,0,100,0,0" >> $PROFILE
  echo "600 1 0,0,100,0,0" >> $PROFILE
done

# experiment 3
for i in {100..1500..100}
do
  echo "900 $i 0,0,0,0,100" >> $PROFILE
  echo "600 1 0,0,0,0,100" >> $PROFILE
done


# experiment 4
for i in {100..1500..100}
do
  echo "900 $i 0,0,50,0,50" >> $PROFILE
  echo "600 1 0,0,50,0,50" >> $PROFILE
done


# run all experiments
#/home/alekh/sc/runExperimentPg.sh /home/alekh/tpcc_output /home/alekh/tpcc_profile/postgres_experiments
