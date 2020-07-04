using System;
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System.Collections.Generic;
public class Meeting
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string Id { get; set; }
    public string Title { get; set; }
    public string Organizer { get; set; }
    public string Location { get; set; }
    public string Description { get; set; }
    public DateTime StartTime { get; set; }
    public DateTime EndTime { get; set; }
    public DateTime BookingStartTime { get; set; }
    public DateTime BookingEndTime { get; set; }
    public int TotalSpots { get; set; }
    public int SpotsPerInterval { get; set; }
    public Boolean PublicAttendeeList { get; set; }
    public Interval[] Intervals { get; set; }
    
}