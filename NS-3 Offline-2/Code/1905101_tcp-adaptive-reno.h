#ifndef TCP_ADAPTIVERENO_H
#define TCP_ADAPTIVERENO_H

#include "tcp-congestion-ops.h"
#include "tcp-westwood-plus.h"
#include "ns3/tcp-recovery-ops.h"
#include "ns3/sequence-number.h"
#include "ns3/traced-value.h"
#include "ns3/event-id.h"

namespace ns3 {
class Time;

/**
 * \ingroup congestionOps
 *
 * \brief An implementation of TCP ADAPTIVE RENO.
 *
 * 
 * 
 * 
 * 
 */
class TcpAdaptiveReno : public TcpWestwoodPlus
{
public:
    /**
    * \brief Get the type ID.
    * \return the object TypeId
    */
    static TypeId GetTypeId (void);

    TcpAdaptiveReno (void);
    /**
    * \brief Copy constructor
    * \param sock the object to copy
    */
    TcpAdaptiveReno (const TcpAdaptiveReno& sock);
    virtual ~TcpAdaptiveReno (void);

    /**
    * \brief Filter type (None or Tustin)
    */
    enum FilterType 
    {
        NONE,
        TUSTIN
    };

    virtual void PktsAcked (Ptr<TcpSocketState> tcb, uint32_t packetsAcked, const Time& rtt);


    virtual uint32_t GetSsThresh (Ptr<const TcpSocketState> tcb,uint32_t bytesInFlight);

    virtual Ptr<TcpCongestionOps> Fork ();

private:
    double EstimateCongestionLevel();
    void EstimateIncWnd(Ptr<TcpSocketState> tcb);
    void EstimateBW (const Time& rtt, Ptr<TcpSocketState> tcb);

protected:
    virtual void CongestionAvoidance (Ptr<TcpSocketState> tcb, uint32_t segmentsAcked);

    Time  m_RttCurr;   // ^ Current RTT
    Time  m_RttMin;    // ^ Minimum RTT
    Time  m_RttConJth;  // ^ Congestion RTT (j th event)
    Time  m_RttJthPktL;  // ^ RTT of j packet loss
    Time  m_RttPrevConJ; // ^ Previous Congestion RTT (j-1 th event)

    // Window calculations
    int32_t m_WBase;   // ^ Base Window
    int32_t m_WInc;    // ^ Increment Window
    int32_t m_WProbe;  // ^ Probe Window 
};

} // namespace ns3

#endif /* TCP_ADAPTIVE_RENO_H */
