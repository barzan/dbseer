#! /bin/bash


PROFILE="/home/alekh/tpcc_profile/postgres_experiments"

rm $PROFILE

# experiment 5
echo "10 1 45,43,4,4,4" >> $PROFILE
for i in {5..2000..5}
do
  echo "10 $i 45,43,4,4,4" >> $PROFILE
done


# experiment 6
trans1=5
trans2=83
for i in {0..80..10}
do
  echo "600 100 $(($trans1+$i)),$(($trans2-$i)),4,4,4" >> $PROFILE
  echo "600 1 $(($trans1+$i)),$(($trans2-$i)),4,4,4" >> $PROFILE
done

# experiment 7
for i in {0..80..10}
do
  echo "600 700 $(($trans1+$i)),$(($trans2-$i)),4,4,4" >> $PROFILE
  echo "600 1 $(($trans1+$i)),$(($trans2-$i)),4,4,4" >> $PROFILE
done


# run all experiments
/home/alekh/sc/runExperimentPg.sh /home/alekh/tpcc_output /home/alekh/tpcc_profile/postgres_experiments
