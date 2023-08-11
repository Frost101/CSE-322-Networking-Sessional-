#include "ns3/applications-module.h"
#include "ns3/core-module.h"
#include "ns3/internet-module.h"
#include "ns3/network-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/network-module.h"
#include "ns3/stats-module.h"
#include "ns3/ssid.h"
#include "ns3/yans-wifi-helper.h"
#include "ns3/mobility-module.h"
#include <fstream>


#define PORT 9
#define PACKET_SIZE 1024
#define TX_RANGE 5

// Network Topology

//               AP                  AP
//  *    *   *   *                   *    *    *    *
//  |    |   |   |     10.1.2.0      |    |    |    |
// n4   n3  n2  n0 ---------------- n1   n5   n6   n7
//   10.1.1.0        point-to-point     10.1.3.0

using namespace ns3;
using namespace std;

//^ Global Var
int packetSentCount = 0;
int packetReceivedCount = 0;
int bytesReceived = 0;

double simulationTime = 10.0;
double sinkStart = 0.0;
double sinkStop = 9.0;
double senderStart = 1.0;
double senderStop = 8.0;
string fileName = "scratch/static_data/plot.txt";



//^ Network Throughput Calculation
double calculateThroughput() {
    return (double)((bytesReceived * 8.0) / simulationTime); 
}

//^ Packet Delivery Ratio
double calculateRatio() { 
    return (double)(packetReceivedCount / (packetSentCount * 1.0)); 
}

//^ Trace Sink
void packetReceived(Ptr< const Packet > packet, const Address &address){
    bytesReceived += packet->GetSize();
    packetReceivedCount++;
    //cout << "Packets received: " << packetReceivedCount << "   Bytes Received: " << bytesReceived << endl;
}

void packetSent(Ptr< const Packet > packet){
    packetSentCount++;
    //cout << "Packet Sent: " << packetSentCount << endl;
}



NS_LOG_COMPONENT_DEFINE("ns3-offline-1");



int 
main(int argc, char* argv[])
{
    int nodeCount = 20;
    int nodeSpeed = 5;              //* 5 m/s, 10 m/s etc
    int flow = 10;
    int packetsPerSec = 100;
    int coverageArea = 1;


    /*
double simulationTime;
double sinkStart;
double sinkStop;
double senderStart;
double senderStop;
    */

    //^ Command line argument parser setup. 
    CommandLine cmd(__FILE__);
    cmd.AddValue("nodeCount", "Number of nodes", nodeCount);
    cmd.AddValue("flow", "Number of flows", flow);
    cmd.AddValue("packetPerSec", "Number of packets per second", packetsPerSec);
    cmd.AddValue("nodeSpeed", "Speed of nodes", nodeSpeed);
    cmd.AddValue("coverageArea", "Coverage area", coverageArea);
    cmd.AddValue("simulationTime","Total simulation time",simulationTime);
    cmd.AddValue("sinkStart","Sink start time",sinkStart);
    cmd.AddValue("sinkStop", "Sink Stop Time",sinkStop);
    cmd.AddValue("senderStart","Sender Start Time",senderStart);
    cmd.AddValue("senderStop","Sender Stop Time",senderStop);
    cmd.AddValue("fileName","Output File Name",fileName);
    cmd.Parse(argc, argv);

    cout << "nodeCount: " << nodeCount << endl;
    cout << "flow: " << flow << endl;
    cout << "packetpersec: " << packetsPerSec << endl;
    cout << "nodeSpeed: " << nodeSpeed << endl;
    cout << "coverage area: " << coverageArea << endl;
    cout << "simulation time: " << simulationTime << endl;
    cout << "sink start: " << sinkStart << endl;
    cout << "sink stop: " << sinkStop << endl;
    cout << "sender start: " << senderStart << endl;
    cout << "sender stop: " << senderStop << endl;
    cout << "Output file name: " << fileName << endl;

    int senderCount = nodeCount/2;
    int receiverCount = senderCount;


    //LogComponentEnable("PacketSink", LOG_LEVEL_INFO); // Replace LOG_LEVEL_INFO with the desired logging level
    //LogComponentEnable("OnOffApplication", LOG_LEVEL_INFO); // Replace LOG_LEVEL_INFO with the desired logging level

    Config::SetDefault("ns3::TcpSocket::SegmentSize", UintegerValue(PACKET_SIZE));


    //^ Node creation starts
    NodeContainer bottleneckNodes;
    bottleneckNodes.Create(2);

    NodeContainer senderNodes;
    senderNodes.Create(senderCount);

    NodeContainer receiverNodes;
    receiverNodes.Create(receiverCount);


    NodeContainer wifiApNodeSender = bottleneckNodes.Get(0);
    NodeContainer wifiApNodeReceiver = bottleneckNodes.Get(1);
    //^ Node creation done


    //^ Point to point link installation 
    PointToPointHelper bottleneckP2P;
    bottleneckP2P.SetDeviceAttribute("DataRate", StringValue("20Mbps"));
    bottleneckP2P.SetChannelAttribute("Delay", StringValue("2ms"));


    //^ Install p2p net devices
    NetDeviceContainer bottleneckDevices;
    bottleneckDevices = bottleneckP2P.Install(bottleneckNodes);

    //^ Physical Layer
    //^ YANS model - Yet Another Network Simulator
    //^ For sender side
    YansWifiChannelHelper channelSender = YansWifiChannelHelper::Default();
    channelSender.AddPropagationLoss("ns3::RangePropagationLossModel",
                                    "MaxRange",
                                    DoubleValue(coverageArea * TX_RANGE));
    YansWifiPhyHelper phySender;
    phySender.SetChannel(channelSender.Create()); //* Share the same wireless medium


    //^ Physical Layer
    //^ YANS model - Yet Another Network Simulator
    //^ For receiver's side
    YansWifiChannelHelper channelReceiver = YansWifiChannelHelper::Default();
    channelReceiver.AddPropagationLoss("ns3::RangePropagationLossModel",
                                    "MaxRange",
                                    DoubleValue(coverageArea * TX_RANGE));
    YansWifiPhyHelper phyReceiver;
    phyReceiver.SetChannel(channelReceiver.Create()); //* Share the same wireless medium


    //^ For Data Link Layer, Set up ssid
    WifiMacHelper macSender;
    Ssid ssidSender = Ssid("ns-3-ssid-sender");
    WifiMacHelper macReceiver;
    Ssid ssidReceiver = Ssid("ns-3-ssid-receiver");

    WifiHelper wifi;

    //^ ActiveProbing false -  probe requests will not be sent by MACs created by this
    //^ helper, and stations will listen for AP beacons.
    //^ Setup Sender net devices
    NetDeviceContainer senderDevices;
    macSender.SetType("ns3::StaWifiMac", "Ssid", SsidValue(ssidSender), "ActiveProbing", BooleanValue(false));
    senderDevices = wifi.Install(phySender, macSender, senderNodes);


    //^ Setup Receiver net devices
    NetDeviceContainer receiverDevices;
    macReceiver.SetType("ns3::StaWifiMac", "Ssid", SsidValue(ssidReceiver), "ActiveProbing", BooleanValue(false));
    receiverDevices = wifi.Install(phyReceiver, macReceiver, receiverNodes);


    //^ Setup AP Devices
    macSender.SetType("ns3::ApWifiMac", "Ssid", SsidValue(ssidSender));
    macReceiver.SetType("ns3::ApWifiMac", "Ssid", SsidValue(ssidReceiver));

    //^ Setup AP Sender Device 
    NetDeviceContainer apDeviceSender;
    apDeviceSender = wifi.Install(phySender, macSender, wifiApNodeSender);

    //^ Setup AP Receiver Device 
    NetDeviceContainer apDeviceReceiver;
    apDeviceReceiver = wifi.Install(phyReceiver, macReceiver, wifiApNodeReceiver);

    //^ Setup Mobility
    // MobilityHelper mobility;
    // mobility.SetPositionAllocator(
    //     "ns3::GridPositionAllocator",
    //     "MinX",
    //     DoubleValue(0.0),
    //     "MinY",
    //     DoubleValue(0.0),
    //     "DeltaX",
    //     DoubleValue(5.0),
    //     "DeltaY",
    //     DoubleValue(10.0),
    //     "GridWidth",
    //     UintegerValue(3),
    //     "LayoutType",
    //     StringValue("RowFirst")
    // );

    // mobility.SetMobilityModel(
    //     "ns3::RandomWalk2dMobilityModel",
    //     "Bounds",
    //     RectangleValue(Rectangle(-50,50,-50,50)),
    //     "Speed",
    //     StringValue("ns3::ConstantRandomVariable[Constant="+std::to_string(nodeSpeed)+"]")
    // );

    // mobility.Install(senderNodes);
    // mobility.Install(receiverNodes);

    //^ Setup mobility. All nodes should be stationary
    MobilityHelper mobility;
    mobility.SetMobilityModel("ns3::ConstantPositionMobilityModel");
    mobility.Install(wifiApNodeSender);
    mobility.Install(wifiApNodeReceiver);
    mobility.Install(senderNodes);
    mobility.Install(receiverNodes);


    //^ Install an Internet Stack (TCP, UDP, IP, etc.) in each nodes once
    InternetStackHelper stack;
    stack.Install(senderNodes);
    stack.Install(receiverNodes);
    stack.Install(bottleneckNodes);

    //^ Assign IP Addresses
    Ipv4AddressHelper address;

    address.SetBase("10.1.1.0","255.255.255.0");
    Ipv4InterfaceContainer senderNodeInterface;
    senderNodeInterface = address.Assign(senderDevices);
    Ipv4InterfaceContainer senderAPNodeInterface;
    senderAPNodeInterface = address.Assign(apDeviceSender);


    address.SetBase("10.1.2.0","255.255.255.0");
    Ipv4InterfaceContainer p2pInterfaces;
    p2pInterfaces = address.Assign(bottleneckDevices);


    address.SetBase("10.1.3.0","255.255.255.0");
    Ipv4InterfaceContainer receiverNodeInterface;
    receiverNodeInterface = address.Assign(receiverDevices);
    Ipv4InterfaceContainer receiverAPNodeInterface;
    receiverAPNodeInterface = address.Assign(apDeviceReceiver);


    //^ Create applications according to the flow count
    //^ Create sinkApps first
    ApplicationContainer sinkApps;
    PacketSinkHelper sinkHelper("ns3::TcpSocketFactory", InetSocketAddress(Ipv4Address::GetAny(), PORT));
    for(int i=0; i<receiverCount; i++){
       sinkApps.Add(sinkHelper.Install(receiverNodes.Get(i)));
    }



    //^ Create On Off server Apps
    ApplicationContainer senderApps;
    for(int i=0; i<flow; i++){
        OnOffHelper senderHelper("ns3::TcpSocketFactory", InetSocketAddress(receiverNodeInterface.GetAddress(i%receiverCount),PORT));
        senderHelper.SetAttribute("OnTime", StringValue("ns3::ConstantRandomVariable[Constant=1]"));
        senderHelper.SetAttribute("OffTime",StringValue("ns3::ConstantRandomVariable[Constant=0]"));
        senderHelper.SetAttribute("PacketSize", UintegerValue(PACKET_SIZE));
        senderHelper.SetAttribute("DataRate", DataRateValue(DataRate(packetsPerSec * PACKET_SIZE * 8)));

        senderApps.Add(senderHelper.Install(senderNodes.Get(i%senderCount)));
    }


    //^ For Tracing 
    for(uint32_t i=0; i<sinkApps.GetN(); i++){
       sinkApps.Get(i)->TraceConnectWithoutContext("Rx", MakeCallback(&packetReceived));
    }

    for(uint32_t i=0; i<senderApps.GetN(); i++){
        senderApps.Get(i)->TraceConnectWithoutContext("Tx", MakeCallback(&packetSent));
    }



    //^ Routing
    Ipv4GlobalRoutingHelper::PopulateRoutingTables();


    //^ Schedule time
    sinkApps.Start(Seconds(sinkStart));
    sinkApps.Stop(Seconds(sinkStop));
    senderApps.Start(Seconds(senderStart));
    senderApps.Stop(Seconds(senderStop));


    //^ Force stop the simulator
    Simulator::Stop(Seconds(simulationTime));

    Simulator::Run();
    Simulator::Destroy();

    cout << "Throughput: " << calculateThroughput() << "   Packet Delivery Ratio: " << calculateRatio() << endl;
    cout << "--------------------------------------------------------" << endl;

    //^ Write to file
    ofstream writeToFile(fileName, ios_base::app);
    writeToFile << nodeCount << " " << flow << " " << packetsPerSec << " " << coverageArea  << " " << nodeSpeed << " " << calculateThroughput() << " " << calculateRatio() << "\n" ;
    writeToFile.close();
    return 0;
}