using System;
public class MeetingToBookEvent
{   
    public string Description { get; set; }
    public DateTime Deadline { get; set; }   
    public string Id { get; set; }
    public string Service { get; set; }
    public MeetingToBookEvent(string description, DateTime deadline, string id) {
        this.Description = description;
        this.Deadline = deadline;
        this.Id = id;
        this.Service = "Meetings";
    }
}