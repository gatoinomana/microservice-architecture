namespace MartinezRojo_Noelia_arso_laboratorio_Meetings.Models
{
    public class MeetingsDatabaseSettings : IMeetingsDatabaseSettings
    {
        public string MeetingsCollectionName { get; set; }
        public string ConnectionString { get; set; }
        public string DatabaseName { get; set; }
    }

    public interface IMeetingsDatabaseSettings
    {
        string MeetingsCollectionName { get; set; }
        string ConnectionString { get; set; }
        string DatabaseName { get; set; }
    }
}