using MartinezRojo_Noelia_arso_laboratorio_Meetings.Models;
using MongoDB.Driver;
using System.Collections.Generic;
using System.Linq;

namespace MartinezRojo_Noelia_arso_laboratorio_Meetings.Services
{
    public class MeetingsService
    {
        private readonly IMongoCollection<Meeting> _meetings;

        public MeetingsService(IMeetingsDatabaseSettings settings)
        {
            var client = new MongoClient(settings.ConnectionString);
            var database = client.GetDatabase(settings.DatabaseName);

            _meetings = database.GetCollection<Meeting>(settings.MeetingsCollectionName);
        }

        public List<Meeting> Get() =>
            _meetings.Find(meeting => true).ToList();

        public Meeting Get(string id) =>
            _meetings.Find<Meeting>(meeting => meeting.Id == id).FirstOrDefault();

        public Meeting Create(Meeting meeting)
        {
            _meetings.InsertOne(meeting);
            return meeting;
        }

        public void Update(string id, Meeting meetingIn) =>
            _meetings.ReplaceOne(meeting => meeting.Id == id, meetingIn);

        public void Remove(Meeting meetingIn) =>
            _meetings.DeleteOne(meeting => meeting.Id == meetingIn.Id);

        public void Remove(string id) => 
            _meetings.DeleteOne(meeting => meeting.Id == id);
    }
}