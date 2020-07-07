using System;
public class CreateTaskEvent
{   
    public string Description { get; set; }
    public DateTime Deadline { get; set; }   
    public string Id { get; set; }
    public string Service { get; set; }
    public CreateTaskEvent(string description, DateTime deadline, string id) {
        this.Description = description;
        this.Deadline = deadline;
        this.Id = id;
        this.Service = "Meetings";
    }
}