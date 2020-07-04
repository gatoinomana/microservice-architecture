using System;
public class Interval
{
    public DateTime StartTime { get; set; }
    public DateTime EndTime { get; set; }
    public int Spots { get; set; }
    public Booking[] Attendees { get; set; }
}