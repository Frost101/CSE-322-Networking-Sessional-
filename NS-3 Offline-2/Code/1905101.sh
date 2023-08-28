#!/bin/bash

algoType=1
bottleNeckDRateV=100
packetLossExp=6
folder=scratch/plotData
fileName="$folder/data.txt"
fileName1="$folder/cwnd1.txt"
fileName2="$folder/cwnd2.txt"





# parameters - title, xlabel, ylabel, input file1, output file, xcolumn, y column1, y tick1, input file2, y column2, y tick2
graph() {
    gnuCommand="set terminal pngcairo size 1024,768 enhanced font 'Verdana,12';
    set output '$5.png';
    set title '$1' font 'Verdana,16';
    set xlabel '$2' font 'Verdana,14';
    set ylabel '$3' font 'Verdana,14';
    plot '$4' using $6:$7 title '$8' with linespoints linestyle 1"


    if [[ $# -gt 8 ]]
    then
    gnuCommand=$gnuCommand", '$9' using $6:${10} title '${11}' with
    linespoints linestyle 2;"
    else
    gnuCommand=$gnuCommand";"
    fi

    if [[ $# -gt 11 ]]
    then

    # replace linespoints with lines for the legend
    gnuCommand=${gnuCommand//linespoints/lines}

    fi

    gnuCommand=$gnuCommand" exit;"


    echo $gnuCommand | gnuplot
}



############################       Generate Throughput VS Packet Loss Rate Graph     #####################################

mkdir -p $folder
rm -f $fileName
rm -f $fileName1
rm -f $fileName2

touch $fileName
touch $fileName1
touch $fileName2

for i in $(seq 10 10 300)
do 
    ./ns3 run "scratch/1905101.cc --algoType=$algoType --bottleNeckDRate=$i --packetLossExp=$packetLossExp --fileName=$fileName --fileName1=$fileName1 --fileName2=$fileName2"

done

# parameters - title, xlabel, ylabel, input file1, output file, xcolumn, y column1, y tick1, input file2, y column2, y tick2
graph "Throughput VS BottleNeck Data Rate" "Bottleneck Data Rate" "Throughput" "$fileName" "$folder/throughput_bottleneck.png" 3 1 "TCPNewReno" "$fileName" 2 "TCPWestWoodPlus"
#gnuplot -c "$folder/res.gnuplot" "$folder/TCPNewRenothroughput_bottleneck.png" "Throughput(TCPNewReno) VS Bottleneck Data rate" "Bottleneck Data Rate" "Throughput (bps)" "$fileName" 3 1



############################       Generate Throughput VS Packet Loss Rate Graph     #####################################

rm -f $fileName
rm -f $fileName1
rm -f $fileName2

touch $fileName
touch $fileName1
touch $fileName2


for i in $(seq 2 0.2 6)
do
    ./ns3 run "scratch/1905101.cc --algoType=$algoType --bottleNeckDRate=$bottleNeckDRateV --packetLossExp=$i --fileName=$fileName --fileName1=$fileName1 --fileName2=$fileName2"
done

# parameters - title, xlabel, ylabel, input file1, output file, xcolumn, y column1, y tick1, input file2, y column2, y tick2
graph "Throughput VS Packet Loss Rate" "Packet Loss Rate/Exp" "Throughput" "$fileName" "$folder/throughput_packetloss.png" 4 1 "TCPNewReno" "$fileName" 2 "TCPWestWoodPlus"



############################       Generate Congestion Window VS Time Graph     #####################################

rm -f $fileName
rm -f $fileName1
rm -f $fileName2

touch $fileName
touch $fileName1
touch $fileName2

./ns3 run "scratch/1905101.cc --algoType=$algoType --bottleNeckDRate=$bottleNeckDRateV --packetLossExp=$packetLossExp --fileName=$fileName --fileName1=$fileName1 --fileName2=$fileName2"
# parameters - title, xlabel, ylabel, input file1, output file, xcolumn, y column1, y tick1, input file2, y column2, y tick2
graph "Congestion Window VS Time" "Time(seconds)" "Congestion Window" "$fileName1" "$folder/congestion_time.png" 1 2 "TCPNewReno" "$fileName2" 2 "TCPWestWoodPlus"

