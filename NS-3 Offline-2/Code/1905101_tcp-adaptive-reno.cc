#include "1905101_tcp-adaptive-reno.h"
#include "ns3/log.h"
#include "ns3/simulator.h"
#include "rtt-estimator.h"
#include "tcp-socket-base.h"

NS_LOG_COMPONENT_DEFINE ("TcpAdaptiveReno");

namespace ns3 {

NS_OBJECT_ENSURE_REGISTERED (TcpAdaptiveReno);

TypeId
TcpAdaptiveReno::GetTypeId (void)
{
    static TypeId tid = TypeId("ns3::TcpAdaptiveReno")
        .SetParent<TcpNewReno>()
        .SetGroupName ("Internet")
        .AddConstructor<TcpAdaptiveReno>()
        .AddAttribute("FilterType", "Use this to choose no filter or Tustin's approximation filter",
                  EnumValue(TcpAdaptiveReno::TUSTIN), MakeEnumAccessor(&TcpAdaptiveReno::m_fType),
                  MakeEnumChecker(TcpAdaptiveReno::NONE, "None", TcpAdaptiveReno::TUSTIN, "Tustin"))
        .AddTraceSource("EstimatedBW", "The estimated bandwidth",
                    MakeTraceSourceAccessor(&TcpAdaptiveReno::m_currentBW),
                    "ns3::TracedValueCallback::Double")
  ;
    return tid;
}

TcpAdaptiveReno::TcpAdaptiveReno (void) :
    TcpWestwoodPlus(),
    m_RttCurr(Time (0)),
    m_RttMin(Time (0)),
    m_RttConJth(Time (0)),
    m_RttJthPktL(Time (0)),
    m_RttPrevConJ(Time(0)),
    m_WBase(0),
    m_WInc(0),
    m_WProbe(0)
{
    NS_LOG_FUNCTION (this);
}

TcpAdaptiveReno::TcpAdaptiveReno (const TcpAdaptiveReno& sock) :
    TcpWestwoodPlus (sock),
    m_RttCurr (Time (0)),
    m_RttMin (Time (0)),
    m_RttConJth (Time (0)),
    m_RttJthPktL (Time (0)),
    m_RttPrevConJ (Time(0)),
    m_WBase(0),
    m_WInc (0),
    m_WProbe (0)
{
  NS_LOG_FUNCTION (this);
}

TcpAdaptiveReno::~TcpAdaptiveReno (void)
{

}

// * This function is called each time a packet is Acked.
// * It will increase the count of acked segment and update the current estimated bandwidth
void
TcpAdaptiveReno::PktsAcked (Ptr<TcpSocketState> tcb, uint32_t packetsAcked, const Time& rtt)
{
    NS_LOG_FUNCTION (this << tcb << packetsAcked << rtt);

    if(rtt.IsZero()){
        NS_LOG_WARN ("RTT is measured ZERO!");
        return;
    }
    m_ackedSegments += packetsAcked;

    // * calculate min rtt here
    if(m_RttMin.IsZero()){
         m_RttMin = rtt;
    }
    else if(rtt <= m_RttMin){
         m_RttMin = rtt;
    }

    m_RttCurr = rtt;


    TcpWestwoodPlus::EstimateBW (rtt, tcb);
}


// * Estimates the current congestion level using Round Trip Time(RTT)
double
TcpAdaptiveReno::EstimateCongestionLevel()
{
    // * exponential smoothing factor
    float a = 0.85; 

    // * the initial value should take the full current Jth loss Rtt
    if(m_RttPrevConJ < m_RttMin) a = 0;
  
    double rttCongestion = a*m_RttPrevConJ.GetSeconds() + (1-a)*m_RttJthPktL.GetSeconds();

    // * Calculated for the next calculation
    m_RttConJth = Seconds(rttCongestion);
  
    double tmp = (m_RttCurr.GetSeconds() - m_RttMin.GetSeconds()) / (rttCongestion - m_RttMin.GetSeconds());

    double c =  std::min(tmp, 1.0);

    return c;
}


// * Calculate WMaxInc and update the value Winc
void 
TcpAdaptiveReno::EstimateIncWnd(Ptr<TcpSocketState> tcb)
{
    double c = EstimateCongestionLevel();
    uint64_t B = m_currentBW.Get().GetBitRate();
    uint64_t M = 1000; 
    uint64_t MSS = tcb->m_segmentSize * tcb->m_segmentSize;


    double WMaxInc = static_cast<double>(B / M * static_cast<double>(MSS)); 

    double alpha = 10.0; 
    double beta = (2 * WMaxInc * ((1 / alpha) - ((1 / alpha + 1) / (std::exp(alpha)))));
    double gamma = (1 - (2 * WMaxInc * ((1 / alpha) - ((1 / alpha + 0.5) / (std::exp(alpha))))));

    m_WInc = (int)((WMaxInc / std::exp(c * alpha)) + (c * beta) + gamma);
}


// * Increases congestion window in congestion avoidence phase
void
TcpAdaptiveReno::CongestionAvoidance (Ptr<TcpSocketState> tcb, uint32_t segmentsAcked)
{
    if (segmentsAcked > 0){

        EstimateIncWnd(tcb);
        
        double tmp = static_cast<double> (tcb->m_segmentSize * tcb->m_segmentSize) / tcb->m_cWnd.Get ();
        tmp = std::max (1.0, tmp);
        m_WBase+= static_cast<uint32_t> (tmp);

        tmp = (double) (m_WProbe + m_WInc / (int)tcb->m_cWnd.Get());
        
        m_WProbe = std::max(tmp, 0.0);
      
        tcb->m_cWnd = m_WBase+ m_WProbe;
        
    }

}


// * This function is called when a loss event has occured
uint32_t
TcpAdaptiveReno::GetSsThresh (Ptr<const TcpSocketState> tcb,uint32_t bytesInFlight){

    // * Firstly, here we need to update the m_RttPrevConJ and m_RttJthPktL
    m_RttPrevConJ = m_RttConJth; 
    m_RttJthPktL = m_RttCurr; 
  
    double c = EstimateCongestionLevel();

    uint32_t ssThresh = std::max (2*tcb->m_segmentSize, (uint32_t) (tcb->m_cWnd / (1.0 + c)));

    m_WBase= ssThresh;
    m_WProbe = 0;
  
    return ssThresh;

}

Ptr<TcpCongestionOps>
TcpAdaptiveReno::Fork ()
{
  return CreateObject<TcpAdaptiveReno> (*this);
}

} // namespace ns3
