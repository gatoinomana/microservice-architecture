using System;
public class Meeting
{
    public long Id { get; set; }
    public string Title { get; set; }
    public string Organizer { get; set; }
    public string Location { get; set; }
    public string Description { get; set; }
    public DateTime StartingTime { get; set; }
    public DateTime EndingTime { get; set; }
    public DateTime BookingStartingTime { get; set; }
    public DateTime BookingEndingTime { get; set; }
    public int TotalSpots { get; set; }
    public int SpotsPerInterval { get; set; }
    public Boolean PublicAttendeeList { get; set; }
}