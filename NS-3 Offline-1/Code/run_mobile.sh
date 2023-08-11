#!/bin/bash

folder=scratch/mobile_data
fileName=$folder/plot.txt
nodeCount=20
flow=50
packetPerSec=100
coverageArea=5
nodeSpeed=5
simulationTime=10.0
sinkStart=0.0
sinkStop=10.0
senderStart=1.0
senderStop=10.0

##Create folder and text file for output
mkdir -p $folder
rm -f $fileName
touch $fileName
##./ns3 run "scratch/1905101_1.cc --nodeCount=$nodeCount --flow=$flow --packetPerSec=$packetPerSec --coverageArea=$coverageArea --nodeSpeed=$nodeSpeed --simulationTime=$simulationTime --sinkStart=$sinkStart --sinkStop=$sinkStop --senderStart=$senderStart --senderStop=$senderStop --fileName=$fileName"

##################         Nodecount VS Throughput and packet delivery ratio           ############### 
#No of Nodes: 20 40 60 80 100
for i in $(seq 20 20 100)
do
    ./ns3 run "scratch/1905101_2.cc --nodeCount=$i --flow=$flow --packetPerSec=$packetPerSec --coverageArea=$coverageArea --nodeSpeed=$nodeSpeed --simulationTime=$simulationTime --sinkStart=$sinkStart --sinkStop=$sinkStop --senderStart=$senderStart --senderStop=$senderStop --fileName=$fileName"
done

##NodeCount VS Throughput graph generation
gnuplot -c "$folder/res.gnuplot" "$folder/throughput_nodeCount.png" "Node Count VS Throughput" "Nodecount" "throughput (bps)" "$fileName" 1 6

##NodeCount VS Packet Delivery Ratio
gnuplot -c "$folder/res.gnuplot" "$folder/packetRatio_nodeCount.png" "Node Count VS Packet Delivery Ratio" "Nodecount" "packet delivery ratio" "$fileName" 1 7





##################             Flow VS throughput and Packet Ratio                  ####################
rm -f $fileName
touch $fileName

##No of flows: 10,20,30,40,50
for i in $(seq 10 10 50)
do
    ./ns3 run "scratch/1905101_2.cc --nodeCount=$nodeCount --flow=$i --packetPerSec=$packetPerSec --coverageArea=$coverageArea --nodeSpeed=$nodeSpeed --simulationTime=$simulationTime --sinkStart=$sinkStart --sinkStop=$sinkStop --senderStart=$senderStart --senderStop=$senderStop --fileName=$fileName"
done

##Fow VS Throughput graph generation
gnuplot -c "$folder/res.gnuplot" "$folder/throughput_flow.png" "Flow VS Throughput" "Flow" "throughput (bps)" "$fileName" 2 6

##Flow VS Packet Delivery Ratio
gnuplot -c "$folder/res.gnuplot" "$folder/packetRatio_flow.png" "Flow VS Packet Delivery Ratio" "Flow" "Packet Delivery Ratio" "$fileName" 2 7




##################             Packet Per Second VS throughput and Packet Ratio                  ####################
rm -f $fileName
touch $fileName

##No of packet per sec: 100,200,300,400,500
for i in $(seq 100 100 500)
do
    ./ns3 run "scratch/1905101_2.cc --nodeCount=$nodeCount --flow=$flow --packetPerSec=$i --coverageArea=$coverageArea --nodeSpeed=$nodeSpeed --simulationTime=$simulationTime --sinkStart=$sinkStart --sinkStop=$sinkStop --senderStart=$senderStart --senderStop=$senderStop --fileName=$fileName"
done

##Packet per sec VS Throughput graph generation
gnuplot -c "$folder/res.gnuplot" "$folder/throughput_packetpersec.png" "Packet Per Second VS Throughput" "Packet per second" "throughput (bps)" "$fileName" 3 6

##Packet per sec VS Packet Delivery Ratio
gnuplot -c "$folder/res.gnuplot" "$folder/packetRatio_packetpersec.png" "Packet per second VS Packet Delivery Ratio" "Packet per second" "Packet Delivery Ratio" "$fileName" 3 7





##################             Speed of nodes VS throughput and Packet Ratio                  ####################
rm -f $fileName
touch $fileName

##Value of Speed of nodes: 5,10,15,20,25
for i in $(seq 5 5 25)
do
    ./ns3 run "scratch/1905101_2.cc --nodeCount=$nodeCount --flow=$flow --packetPerSec=$packetPerSec --coverageArea=$coverageArea --nodeSpeed=$i --simulationTime=$simulationTime --sinkStart=$sinkStart --sinkStop=$sinkStop --senderStart=$senderStart --senderStop=$senderStop --fileName=$fileName"
done

##Node speed VS Throughput graph generation
gnuplot -c "$folder/res.gnuplot" "$folder/throughput_nodespeed.png" "Node Speed VS Throughput" "Node Speed" "throughput (bps)" "$fileName" 5 6

##Node speed VS Packet Delivery Ratio
gnuplot -c "$folder/res.gnuplot" "$folder/packetRatio_nodespeed.png" "Node Speed VS Packet Delivery Ratio" "Node Speed" "Packet Delivery Ratio" "$fileName" 5 7
