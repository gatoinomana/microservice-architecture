using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MartinezRojo_Noelia_arso_laboratorio_Meetings.Models;
using MartinezRojo_Noelia_arso_laboratorio_Meetings.Services;
using System.Net.Http;
using Microsoft.AspNetCore.JsonPatch;
using Newtonsoft.Json.Linq;

namespace MartinezRojo_Noelia_arso_laboratorio_Meetings.Controllers
{
    [Route("api/Meetings")]
    [ApiController]
    public class MeetingsController : ControllerBase
    {
        private readonly MeetingsService _meetingsService;
        private readonly UsersServiceFacade _usersService;
        private readonly MsgQueueService _msgQueueService;

        public MeetingsController(MeetingsService meetingsService, 
        UsersServiceFacade usersService, MsgQueueService msgQueueService)
        {
            _meetingsService = meetingsService;
            _usersService = usersService;
            _msgQueueService = msgQueueService;
        }

        // GET: api/Meetings
        [HttpGet]
        public ActionResult<List<Meeting>> GetMeetings()
        {
            return _meetingsService.Get();
        }
            
        // GET: api/Meetings/5
        [HttpGet("{id:length(24)}", Name = "GetMeeting")]
        public ActionResult<Meeting> GetMeeting(string id)
        {
            var meeting = _meetingsService.Get(id);

            if (meeting == null)
            {
                return NotFound();
            }

            return meeting;
        }

        // PUT: api/Meetings/5
        // To protect from overposting attacks, enable the specific properties you want to bind to, for
        // more details, see https://go.microsoft.com/fwlink/?linkid=2123754.
        [HttpPut("{id:length(24)}")]
        public IActionResult PutMeeting(string id, Meeting meetingIn)
        {
            var meeting = _meetingsService.Get(id);

            if (meeting == null)
            {
                return NotFound();
            }

            _meetingsService.Update(id, meetingIn);

            return NoContent();
        }

        // POST: api/Meetings
        // To protect from overposting attacks, enable the specific properties you want to bind to, for
        // more details, see https://go.microsoft.com/fwlink/?linkid=2123754.
        [HttpPost]
        public async Task<ActionResult<Meeting>> PostMeeting(Meeting meeting)
        {
            try {
                await _usersService.OnGet(meeting.Organizer);
            } 
            catch(HttpRequestException e) {
                Console.WriteLine("HttpRequestException: {0}", e);
                return StatusCode(500);
            }
            var organizerRole = _usersService.UserRole;

            if (organizerRole != null && organizerRole.Equals("TEACHER"))
            {
                // Add calculated intervals to meeting
                int numIntervals = meeting.TotalSpots / meeting.SpotsPerInterval;
                TimeSpan totalTime = meeting.EndTime - meeting.StartTime;
                TimeSpan timePerInterval = totalTime / numIntervals;
                DateTime intervalStartTime = meeting.StartTime;
                DateTime intervalEndTime = intervalStartTime + timePerInterval;

                for(int i = 0; i < numIntervals; i++) {
                    meeting.Intervals.Add(new Interval());
                    var num = meeting.Intervals.Count;
                    meeting.Intervals[i].Spots = meeting.SpotsPerInterval;
                    meeting.Intervals[i].Attendees = new string[meeting.Intervals[i].Spots];
                    meeting.Intervals[i].StartTime = intervalStartTime;
                    meeting.Intervals[i].EndTime = intervalEndTime;

                    intervalStartTime = intervalEndTime;
                    intervalEndTime = intervalStartTime + timePerInterval;
                }

                // Save meeting to database
                _meetingsService.Create(meeting);

                // Produce event
                _msgQueueService.ProduceMessage("creada reunion");

                return CreatedAtAction("GetMeeting", new { id = meeting.Id.ToString() }, meeting);
            }

            else return StatusCode(403);
        }

        // DELETE: api/Meetings/5
        [HttpDelete("{id:length(24)}")]
        public IActionResult DeleteMeeting(string id)
        {
            var meeting = _meetingsService.Get(id);

            if (meeting == null)
            {
                return NotFound();
            }

            _meetingsService.Remove(meeting.Id);

            return NoContent();
        }

        [HttpPatch("{id:length(24)}")]
        public async Task<ActionResult<Meeting>> BookMeeting(string id, [FromBody] JObject data)
        {
            DateTime intervalStartTime = data["intervalStartTime"].ToObject<DateTime>();
            string studentId = data["studentId"].ToString();
 
            // Check meeting exists
            var meeting = _meetingsService.Get(id);

            if (meeting == null)
            {
                return NotFound();
            }

            // Check interval exists and has a free spot
            Interval interval = meeting.Intervals.Find(i => i.StartTime.Equals(intervalStartTime));
            int filledSpots = interval.Attendees.Count(elem => elem != null);

            if (interval == null)
            {
                return NotFound();
            }
            else if (filledSpots == interval.Spots)
            {
                return StatusCode(403);
            }

            // Check user exists and is student
            try 
            {
                await _usersService.OnGet(studentId);
            } 
            catch(HttpRequestException e)
            {
                Console.WriteLine("HttpRequestException: {0}", e);
                return StatusCode(500);
            }
            
            var userRole = _usersService.UserRole;

            if (userRole == null || !userRole.Equals("STUDENT"))
            {
                return StatusCode(403);
            }

            // Fill spot
            string[] attendees = meeting.Intervals[meeting.Intervals.IndexOf(interval)].Attendees;
            int i = 0;
            while(i < attendees.Length && attendees[i] != null) {
                i++;
            }
            meeting.Intervals[meeting.Intervals.IndexOf(interval)].Attendees[i] = studentId;
            
            _meetingsService.Update(id, meeting);

            return CreatedAtAction("GetMeeting", new { id = meeting.Id.ToString() }, meeting);
        }


    }
}
