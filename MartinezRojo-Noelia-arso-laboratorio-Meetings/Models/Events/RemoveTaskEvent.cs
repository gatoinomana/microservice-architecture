public class RemoveTaskEvent
{   
    public string StudentId { get; set; }
    public string Id { get; set; }
    public string Service { get; set; }
    public RemoveTaskEvent(string studentId, string id) {
        this.StudentId = studentId;
        this.Id = id;
        this.Service = "Meetings";
    }
}