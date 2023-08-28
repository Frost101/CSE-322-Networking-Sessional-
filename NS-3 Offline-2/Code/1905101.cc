#include "ns3/applications-module.h"
#include "ns3/core-module.h"
#include "ns3/internet-module.h"
#include "ns3/network-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/point-to-point-layout-module.h"
#include "ns3/csma-module.h"
#include "ns3/stats-module.h"
#include "ns3/callback.h"
#include "ns3/flow-monitor-module.h"
#include <stdio.h>
#include <fstream>

#define PORT 2000


using namespace ns3;
using namespace std;

NS_LOG_COMPONENT_DEFINE("Task1");

// Here is the topology
//-------------------------------------------------------------------------------
//
//                      s0 -----    10Mbps-1Gbps   -------rcv0
// TCP SENDERS                      r0 ------ r1                     RECEIVERS
//                      s1 -----   (RTT=100msec)   -------rcv1
//
//--------------------------------------------------------------------------------

class TutorialApp : public Application
{
  public:
    TutorialApp();
    ~TutorialApp() override;

    /**
     * Register this type.
     * \return The TypeId.
     */
    static TypeId GetTypeId();

    /**
     * Setup the socket.
     * \param socket The socket.
     * \param address The destination address.
     * \param packetSize The packet size to transmit.
     * \param dataRate the data rate to use.
     * \param simulatetime
     */
    void Setup(Ptr<Socket> socket,
               Address address,
               uint32_t packetSize,
               DataRate dataRate,
               uint32_t simulatetime);

  private:
    void StartApplication() override;
    void StopApplication() override;

    /// Schedule a new transmission.
    void ScheduleTx();
    /// Send a packet.
    void SendPacket();

    Ptr<Socket> m_socket;   //!< The transmission socket.
    Address m_peer;         //!< The destination address.
    uint32_t m_packetSize;  //!< The packet size.
    DataRate m_dataRate;    //!< The data rate to use.
    EventId m_sendEvent;    //!< Send event.
    bool m_running;         //!< True if the application is running.
    uint32_t m_packetsSent; //!< The number of packets sent.
    uint32_t m_simulatetime;   //!< Simulation time
};


TutorialApp::TutorialApp()
    : m_socket(0),
      m_peer(),
      m_packetSize(0),
      m_dataRate(0),
      m_sendEvent(),
      m_running(false),
      m_packetsSent(0),
      m_simulatetime(0)
{
}

TutorialApp::~TutorialApp()
{
    m_socket = 0;
}

/* static */
TypeId
TutorialApp::GetTypeId()
{
    static TypeId tid = TypeId("TutorialApp")
                            .SetParent<Application>()
                            .SetGroupName("Tutorial")
                            .AddConstructor<TutorialApp>();
    return tid;
}

void
TutorialApp::Setup(Ptr<Socket> socket,
                   Address address,
                   uint32_t packetSize,
                   DataRate dataRate,
                   uint32_t simulatetime)
{
    m_socket = socket;
    m_peer = address;
    m_packetSize = packetSize;
    m_dataRate = dataRate;
    m_simulatetime = simulatetime;
}

void
TutorialApp::StartApplication()
{
    m_running = true;
    m_packetsSent = 0;
    m_socket->Bind();
    m_socket->Connect(m_peer);
    SendPacket();
}

void
TutorialApp::StopApplication()
{
    m_running = false;

    if (m_sendEvent.IsRunning())
    {
        Simulator::Cancel(m_sendEvent);
    }

    if (m_socket)
    {
        m_socket->Close();
    }
}

void
TutorialApp::SendPacket()
{
    Ptr<Packet> packet = Create<Packet>(m_packetSize);
    m_socket->Send(packet);

    // if (++m_packetsSent < m_nPackets)
    // {
    //     ScheduleTx();
    // }

    if(Simulator::Now().GetSeconds() < m_simulatetime){
        ScheduleTx();
    }
}

void
TutorialApp::ScheduleTx()
{
    if (m_running)
    {
        Time tNext(Seconds(m_packetSize * 8 / static_cast<double>(m_dataRate.GetBitRate())));
        m_sendEvent = Simulator::Schedule(tNext, &TutorialApp::SendPacket, this);
    }
}



// * Files
string fileName = "scratch/plotData/data.txt";
string fileName1 = "scratch/plotData/cwnd1.txt";
string fileName2 = "scratch/plotData/cwnd2.txt";
ofstream writeToFile1(fileName1, ios_base::app);
ofstream writeToFile2(fileName2, ios_base::app);

/**
 * Congestion window change callback
 *
 * \param oldCwnd Old congestion window.
 * \param newCwnd New congestion window.
 */
static void
CwndChange1(uint32_t oldCwnd, uint32_t newCwnd)
{
    // * For TCP New Reno
    //NS_LOG_UNCOND("1 " << Simulator::Now().GetSeconds() << "\t" << newCwnd);
    writeToFile1 << Simulator::Now().GetSeconds() << "\t" << newCwnd << "\n" ;
}


static void
CwndChange2(uint32_t oldCwnd, uint32_t newCwnd)
{
    // * For TCP West Wood, Adaptive Reno
    //NS_LOG_UNCOND("2 " << Simulator::Now().GetSeconds() << "\t" << newCwnd);
    writeToFile2 << Simulator::Now().GetSeconds() << "\t" << newCwnd << "\n" ;
}


// * These things can be changed(input from cmd)
int bottleNeckDRateV = 50;
double packetLossExp = 6;
int packetSize = 1024;


// * Constant
int bottleNeckDelayV = 100;
string delaySR = "1ms";
string dataRateSR = "1Gbps";
double simulationTime = 30.0;   //^ In seconds
double cleanupTime = 2.0;       //^ For flowmonitor

// * Algorithms
string algorithm1 = "ns3::TcpNewReno";
string algorithm2 = "ns3::TcpHighSpeed";
string algorithm3 = "ns3::TcpAdaptiveReno";
string algorithm4 = "ns3::TcpWestwoodPlus";
int algoType = 1;       // ^ Type 1:WestWood, Type 2: Adaptive

int main(int argc, char *argv[]){

    // * For file writing
    ofstream writeToFile1(fileName1, ios_base::app);
    ofstream writeToFile2(fileName2, ios_base::app);

    // * Input from Command Line 
    CommandLine cmd (__FILE__);
    cmd.AddValue ("algoType","Type 1 for HighSpeed, Type 2 for Adaptive Reno, Type 3 for WestwoodPlus", algoType);
    cmd.AddValue ("bottleNeckDRate","BottleNeck Data Rate", bottleNeckDRateV);
    cmd.AddValue ("packetLossExp", "Packet loss rate exponential", packetLossExp);
    cmd.AddValue ("fileName", "File for throughput,datarate,lossexp", fileName);
    cmd.AddValue ("fileName1", "File for TCP new Reno", fileName1);
    cmd.AddValue ("fileName2", "File for WestWoodPlus/HighSpeed/adaptive", fileName2);
    cmd.Parse (argc,argv);

    double packetLossRate = (1.0 / pow(10, packetLossExp));
    string bottleNeckDataRate = to_string(bottleNeckDRateV) + "Mbps";
    string bottleNeckDelay = to_string(bottleNeckDelayV) + "ms";


    // * For debugging purpose
    if(algoType == 1){
        cout << "AlgoType: TCP New Reno Vs High-Speed, bottleNeck Data Rate: " << bottleNeckDataRate << "  packetLossExp: " << packetLossExp << endl;
    }
    else if(algoType == 2){
        cout << "AlgoType: TCP New Reno Vs Adaptive Reno, bottleNeck Data Rate: " << bottleNeckDataRate << "  packetLossExp: " << packetLossExp << endl;
    }
    else if(algoType == 3){
        cout << "AlgoType: TCP New Reno Vs TCP WestwoodPlus, bottleNeck Data Rate: " << bottleNeckDataRate << "  packetLossExp: " << packetLossExp << endl;
    }

    // * Set Packet Size
    Config::SetDefault ("ns3::TcpSocket::SegmentSize", UintegerValue (packetSize));

    // * Point to point config setup

    // * Setup BottleNeck
    PointToPointHelper bottleNeckP2P;
    // cout << bottleNeckDataRate << endl;
    // cout << bottleNeckDelay << endl;
    bottleNeckP2P.SetDeviceAttribute("DataRate", StringValue(bottleNeckDataRate));
    bottleNeckP2P.SetChannelAttribute("Delay", StringValue(bottleNeckDelay));


    // * Setup p2p of sender to router and receiver to router
    PointToPointHelper senderReceiverP2P;
    // cout << dataRateSR << endl;
    // cout << delaySR << endl;
    senderReceiverP2P.SetDeviceAttribute  ("DataRate", StringValue (dataRateSR));
    senderReceiverP2P.SetChannelAttribute ("Delay", StringValue (delaySR));
    // * Setting the router buffer capacity
    // * To get the buffer employ drop-tail discarding
    senderReceiverP2P.SetQueue("ns3::DropTailQueue","MaxSize",
                                StringValue(to_string((bottleNeckDelayV * bottleNeckDRateV * 125) / packetSize) + "p"));

    // * Setup dumbell topology
    // * PointToPointDumbbellHelper to the Rescue
    // ? SenderCount, Link
    // ? ReceiverCount, Link
    // ? BottleNeckLink
    PointToPointDumbbellHelper dumbellHelper (2, senderReceiverP2P,
                                              2, senderReceiverP2P,
                                              bottleNeckP2P);

    
    // * Add the RateErrorModel to the devices of the bottleneck link
    Ptr<RateErrorModel> errmodel = CreateObject<RateErrorModel>();
    errmodel->SetAttribute("ErrorRate", DoubleValue(packetLossRate));
    dumbellHelper.m_routerDevices.Get(0)->SetAttribute("ReceiveErrorModel", PointerValue(errmodel));    //^ Left Router
    dumbellHelper.m_routerDevices.Get(1)->SetAttribute("ReceiveErrorModel", PointerValue(errmodel));    //^ Right Router


    // * Lets install stack
    Config::SetDefault ("ns3::TcpL4Protocol::SocketType", StringValue(algorithm1));

    // * Sender 0 and Receiver 0 will get TCP New Reno
    InternetStackHelper stackNewReno;
    stackNewReno.Install(dumbellHelper.GetLeft(0));
    stackNewReno.Install(dumbellHelper.GetRight(0));

    // * Install TCP New Reno to the routers too
    stackNewReno.Install(dumbellHelper.GetLeft());  //^ Left Router
    stackNewReno.Install(dumbellHelper.GetRight()); //^ Right Router

    // * Sender 1 and Receiver 1 will get TCP WestWood/Adaptive Reno
    if(algoType == 1){
        // ? High Speed
        Config::SetDefault ("ns3::TcpL4Protocol::SocketType", StringValue(algorithm2));
    }
    else if(algoType == 2){
        // ? Adaptive Reno
        Config::SetDefault ("ns3::TcpL4Protocol::SocketType", StringValue(algorithm3));
    }
    else if(algoType == 3){
        // ? WestwoodPlus
        Config::SetDefault ("ns3::TcpL4Protocol::SocketType", StringValue(algorithm4));
    }

    InternetStackHelper stack2;
    stack2.Install(dumbellHelper.GetLeft(1));
    stack2.Install(dumbellHelper.GetRight(1));



    // * Ip address
    // ? Left Nodes
    // ? Right Nodes
    // ? Router
    dumbellHelper.AssignIpv4Addresses(Ipv4AddressHelper("10.1.1.0", "255.255.255.0"),
                                    Ipv4AddressHelper("10.2.1.0", "255.255.255.0"),
                                    Ipv4AddressHelper("10.3.1.0", "255.255.255.0"));


    // * Populate Routing Table
    Ipv4GlobalRoutingHelper::PopulateRoutingTables();


    // * Now Install Flowmonitor
    FlowMonitorHelper flowMonitor;
    flowMonitor.SetMonitorAttribute("MaxPerHopDelay", TimeValue(Seconds(cleanupTime)));
    Ptr<FlowMonitor> fmonitor = flowMonitor.InstallAll();


    // * Set up flows and application
    // * Number of flows = 2

    ApplicationContainer sinkApps;
    for(int i=0; i <2; i++){
        Address sinkAddress (InetSocketAddress (dumbellHelper.GetRightIpv4Address (i), PORT));
        PacketSinkHelper packetSinkHelper ("ns3::TcpSocketFactory", InetSocketAddress (Ipv4Address::GetAny(), PORT));
        sinkApps.Add(packetSinkHelper.Install(dumbellHelper.GetRight(i)));


        Ptr<Socket> ns3TcpSocket = Socket::CreateSocket (dumbellHelper.GetLeft (i), TcpSocketFactory::GetTypeId());
        Ptr<TutorialApp> myApp = CreateObject<TutorialApp>();
        myApp->Setup (ns3TcpSocket, sinkAddress, packetSize, DataRate(dataRateSR), simulationTime);
        dumbellHelper.GetLeft(i)->AddApplication(myApp);
        myApp->SetStartTime(Seconds (1));
        myApp->SetStopTime(Seconds(simulationTime));

        if(i%2 == 0)
            ns3TcpSocket->TraceConnectWithoutContext ("CongestionWindow", MakeCallback (&CwndChange1));
        else 
            ns3TcpSocket->TraceConnectWithoutContext ("CongestionWindow", MakeCallback (&CwndChange2));
    }

    sinkApps.Start(Seconds (0));
    sinkApps.Stop(Seconds(simulationTime+cleanupTime));
    Simulator::Stop(Seconds(simulationTime+cleanupTime));
    Simulator::Run();

    fmonitor->SerializeToXmlFile("scratch/flowstat.xml",1,1);
    Ptr<Ipv4FlowClassifier> classifier = DynamicCast<Ipv4FlowClassifier> (flowMonitor.GetClassifier());
    FlowMonitor::FlowStatsContainer stats = fmonitor->GetFlowStats ();


    // * For throughtput calculation
    int receivedBytes1 = 0;
    int receivedBytes2 = 0;

    int tmp=0;
    for(auto it = stats.begin(); it!= stats.end(); it++){
        if(tmp%2==0){
            // * sender 0 to receiver 0 and ACK
            receivedBytes1 += (it->second.rxBytes);
        }
        else{
            // * sender 1 to receiver 1 and ACK
            receivedBytes2 += (it->second.rxBytes);
        }
        tmp++;
    }


    int throughput1 = (receivedBytes1 * 8.0) / simulationTime;
    int throughput2 = (receivedBytes2 * 8.0) / simulationTime;
    NS_LOG_UNCOND("Throughput1 = " << throughput1 << "  Throughput2 = " << throughput2 << "\n");


    //^ Write to file
    ofstream writeToFile(fileName, ios_base::app);
    writeToFile << throughput1 << " " << throughput2 << " " << bottleNeckDRateV << " " <<  packetLossExp  << "\n" ;
    writeToFile.close();
    writeToFile1.close();
    writeToFile2.close();
    Simulator::Destroy();
    return 0;

}



