using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using MartinezRojo_Noelia_arso_laboratorio_Meetings.Services;
using MongoDB.Bson;

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
        public async Task<ActionResult<List<Meeting>>> GetMeetings([FromQuery] string userId)
        {
            if (userId == null)
            {
                return BadRequest();
            }

            // Check role of user
            bool isTeacher, isStudent;

            try
            {
                isTeacher = await _usersService.IsTeacher(userId);
                isStudent = await _usersService.IsStudent(userId);
            }
            catch
            {
                return StatusCode(500);
            }

            // Get all future meetings
            List<Meeting> futureMeetings = _meetingsService.Get()
                .FindAll(m => m.StartTime > DateTime.Today);
        
            // If user is a teacher, discard other teachers' meetings
            if (isTeacher)
            {   
                return futureMeetings.FindAll(m => m.Organizer.Equals(userId));
            }
            // If user is a student, hide attendees 
            // from meetings with private attendee list
            else if(isStudent)
            {
                futureMeetings.ForEach(m => 
                {
                    if (! m.PublicAttendeeList)
                    {
                        m = HideAttendeeList(m, userId);
                    }
                });

                return futureMeetings;
            }
            else
            {
                return StatusCode(403);
            }
        }
            
        // GET: api/Meetings/5
        [HttpGet("{id:length(24)}", Name = "GetMeeting")]
        public async Task<ActionResult<Meeting>> GetMeeting(string id, [FromQuery] string userId)
        {
            if (userId == null)
            {
                return BadRequest();
            }

            // Check role of user
            bool isTeacher, isStudent;

            try
            {
                isTeacher = await _usersService.IsTeacher(userId);
                isStudent = await _usersService.IsStudent(userId);
            }
            catch
            {
                return StatusCode(500);
            }

            // Check meeting exists
            var meeting = _meetingsService.Get(id);

            if (meeting == null)
            {
                return NotFound();
            }
            
            // Discard if past meeting
            if (meeting.StartTime < DateTime.Today)
            {
                return BadRequest();
            }
        
            // If user is a teacher, check meeting is theirs
            if (isTeacher && meeting.Organizer.Equals(userId))
            {
                return meeting;
            }
            // If user is a student, hide attendees 
            // if meeting has a private attendee list
            else if(isStudent)
            {
                return meeting.PublicAttendeeList ? 
                    meeting : HideAttendeeList(meeting, userId); 
            }
            else
            {
                return StatusCode(403);
            }
        }

        // PUT: api/Meetings/5
        // To protect from overposting attacks, enable the specific properties you want to bind to, for
        // more details, see https://go.microsoft.com/fwlink/?linkid=2123754.
        // [HttpPut("{id:length(24)}")]
        // public IActionResult PutMeeting(string id, Meeting meetingIn)
        // {
        //     var meeting = _meetingsService.Get(id);

        //     if (meeting == null)
        //     {
        //         return NotFound();
        //     }

        //     _meetingsService.Update(id, meetingIn);

        //     return NoContent();
        // }

        // POST: api/Meetings
        // To protect from overposting attacks, enable the specific properties you want to bind to, for
        // more details, see https://go.microsoft.com/fwlink/?linkid=2123754.
        [HttpPost]
        public async Task<ActionResult<Meeting>> PostMeeting(Meeting meeting)
        {
            // Check organizer is a registered teacher
            try
            {
                if (! await _usersService.IsTeacher(meeting.Organizer))
                {
                    return StatusCode(403);
                }
            }
            catch
            {
                return StatusCode(500);
            }

            // Check for null values in non optional fields
            // Check for invalid values in dates and numbers
            if (meeting.Title == null || 
                meeting.Organizer == null || 
                meeting.Location == null || 
                meeting.StartTime == null ||
                meeting.EndTime == null || 
                meeting.BookingEndTime == null || 
                meeting.BookingEndTime == null || 
                meeting.TotalSpots <= 0 ||
                meeting.SpotsPerInterval <= 0 ||
                meeting.TotalSpots < meeting.SpotsPerInterval ||
                meeting.TotalSpots % meeting.SpotsPerInterval != 0 ||
                meeting.StartTime < DateTime.Today ||
                meeting.EndTime <= meeting.StartTime ||
                meeting.BookingEndTime <= meeting.BookingStartTime ||
                meeting.BookingStartTime >= meeting.StartTime)
            {
                return BadRequest();
            }

            // Check organizer doesn't have any other meeting
            // overlapping in time with this one
            List<Meeting> otherMeetings = _meetingsService.GetByOrganizer(meeting.Organizer);

            foreach (Meeting other in otherMeetings)
            {
                if (TimeOverlap(meeting.StartTime, meeting.EndTime,
                other.StartTime, other.EndTime))
                {
                    return StatusCode(403);
                }
            }

            // Calculate and add intervals to meeting
            int numIntervals = meeting.TotalSpots / meeting.SpotsPerInterval;
            meeting.Intervals = new Interval[numIntervals];
            TimeSpan totalTime = meeting.EndTime - meeting.StartTime;
            TimeSpan timePerInterval = totalTime / numIntervals;
            DateTime intervalStartTime = meeting.StartTime;
            DateTime intervalEndTime = intervalStartTime + timePerInterval;

            for(int i = 0; i < numIntervals; i++) {
                meeting.Intervals[i] = new Interval();
                meeting.Intervals[i].StartTime = intervalStartTime;
                meeting.Intervals[i].EndTime = intervalEndTime;
                meeting.Intervals[i].Spots = meeting.SpotsPerInterval;
                meeting.Intervals[i].Attendees = new Booking[meeting.Intervals[i].Spots];

                intervalStartTime = intervalEndTime;
                intervalEndTime = intervalStartTime + timePerInterval;
            }

            // Save meeting to database
            _meetingsService.Create(meeting);

            // Produce event
            _msgQueueService.ProduceMessage("creada reunion");

            return CreatedAtAction("GetMeeting", new { id = meeting.Id.ToString() }, meeting);
        }

        // DELETE: api/Meetings/5
        [HttpDelete("{id:length(24)}")]
        public async Task<IActionResult> DeleteMeeting(string id, [FromQuery] string userId)
        {
            if (userId == null)
            {
                return BadRequest();
            }

            // Check user is a teacher
            bool isTeacher;

            try
            {
                isTeacher = await _usersService.IsTeacher(userId);
            }
            catch
            {
                return StatusCode(500);
            }

            var meeting = _meetingsService.Get(id);

            if (meeting == null)
            {
                return NotFound();
            }

            // Check meeting is theirs and hasn't expired 
            if (isTeacher && 
                meeting.Organizer.Equals(userId) &&
                meeting.StartTime > DateTime.Today)
            {
                
                _meetingsService.Remove(meeting.Id);

                return NoContent();
            }
            else 
            {
                return StatusCode(403);
            }
        }

        // DELETE: api/Meetings
        [HttpDelete]
        public async Task<IActionResult> DeleteMeetings([FromQuery] string userId)
        {
            if (userId == null)
            {
                return BadRequest();
            }

            // Check user is a teacher
            bool isTeacher;

            try
            {
                isTeacher = await _usersService.IsTeacher(userId);
            }
            catch
            {
                return StatusCode(500);
            }

            if (isTeacher)
            {
                // Check they have future meetings to delete
                List<Meeting> theirFutureMeetings = 
                    _meetingsService.GetByOrganizer(userId)
                    .FindAll(m => m.StartTime > DateTime.Today);
                
                if (theirFutureMeetings.Any())
                {
                    foreach(Meeting m in theirFutureMeetings)
                    {
                        _meetingsService.Remove(m);
                    }

                    return NoContent();
                }
                else
                {
                    return NotFound();
                }
            }
            else
            {
                return StatusCode(403);
            }
        }

        [HttpPatch("Book/{id:length(24)}")]
        public async Task<ActionResult<Meeting>> BookMeeting(string id, 
            [FromQuery] DateTime intervalStartTime, Booking booking)
        {
            if (intervalStartTime == null)
            {
                return BadRequest();
            }

            // Check attendee is a registered student
            try
            {
                if (! await _usersService.IsStudent(booking.StudentId))
                {
                    return StatusCode(403);
                }
            }
            catch
            {
                return StatusCode(500);
            }

            // Check meeting and interval exist
            var meeting = _meetingsService.Get(id);

            int indexOfInterval = Array.FindIndex(meeting.Intervals, i => 
                i.StartTime.Equals(intervalStartTime.ToUniversalTime()));

            if (meeting == null || indexOfInterval == -1)
            {
                return NotFound();
            }

            // Check booking period is still open
            if (meeting.BookingEndTime <= DateTime.Today)
            {
                return StatusCode(403);
            }

            // Check interval has a free spot
            int indexOfSpot = Array.FindIndex(
                meeting.Intervals[indexOfInterval].Attendees, booking => booking == null);
        
            if (indexOfSpot == -1) 
            {
                return StatusCode(403);
            }

            // Check student hasn't booked for this meeting yet
            int indexOfBooking = Array.FindIndex(meeting.Intervals[indexOfInterval].Attendees, 
            _booking => _booking != null && _booking.StudentId.Equals(booking.StudentId));

            if (indexOfBooking != -1)
            {
                return StatusCode(403);
            }

            // Check student hasn't booked for another meeting
            // overlapping in time with this one
            List<Meeting> bookedMeetings = _meetingsService.Filter(
                "{ \"Intervals.Attendees.StudentId\": \"" + booking.StudentId + "\" }");

            foreach(Meeting m in bookedMeetings)
            {
                foreach(Interval i in m.Intervals)
                {
                    foreach(Booking b in i.Attendees)
                    {
                        if (TimeOverlap(i.StartTime, i.EndTime, intervalStartTime, 
                        meeting.Intervals[indexOfInterval].EndTime))
                        {
                            return StatusCode(403);
                        }
                    }
                }
            }

            // Fill spot
            meeting.Intervals[indexOfInterval].Attendees[indexOfSpot] = booking;
            _meetingsService.Update(id, meeting);

            return CreatedAtAction("GetMeeting", new { id = meeting.Id.ToString() }, meeting);
        }

        [HttpPatch("Unbook/{id:length(24)}")]
        public ActionResult<Meeting> UnbookMeeting(string id, 
        [FromQuery] DateTime intervalStartTime, [FromQuery] string studentId)
        {
            if (intervalStartTime == null || studentId == null)
            {
                return BadRequest();
            }

            // Check meeting and interval exist
            var meeting = _meetingsService.Get(id);

            int indexOfInterval = Array.FindIndex(meeting.Intervals, i => 
                i.StartTime.Equals(intervalStartTime.ToUniversalTime()));

            if (meeting == null || indexOfInterval == -1)
            {
                return NotFound();
            }

            // Check meeting hasn't started nor finished yet
            if (meeting.StartTime <= DateTime.Today)
            {
                return StatusCode(403);
            }

            // Check user had a booking
            int indexOfBooking = Array.FindIndex(meeting.Intervals[indexOfInterval].Attendees, 
            booking => booking != null && booking.StudentId.Equals(studentId));

            if (indexOfBooking == -1)
            {
                return StatusCode(403);
            }

            meeting.Intervals[indexOfInterval].Attendees[indexOfBooking] = null;
            _meetingsService.Update(id, meeting);

            return CreatedAtAction("GetMeeting", new { id = meeting.Id.ToString() }, meeting);
        }

        // For teachers to add students to a meeting
        [HttpPatch("AddStudent/{id:length(24)}")]
        public async Task<ActionResult<Meeting>> AddStudent(string id, 
            [FromQuery] DateTime intervalStartTime, 
            [FromQuery] string studentId, 
            [FromQuery] string teacherId)
        {
            if (intervalStartTime == null || 
                studentId == null || 
                teacherId == null)
            {
                return BadRequest();
            }

            // Check user is a teacher
            try
            {
                if (! await _usersService.IsTeacher(teacherId))
                {
                    return StatusCode(403);
                }
            }
            catch
            {
                return StatusCode(500);
            }

            // Check meeting and interval exist
            var meeting = _meetingsService.Get(id);

            int indexOfInterval = Array.FindIndex(meeting.Intervals, i => 
                i.StartTime.Equals(intervalStartTime.ToUniversalTime()));

            if (meeting == null || indexOfInterval == -1)
            {
                return NotFound();
            }

            // Check teacher is the meeting's organizer
            if (! teacherId.Equals(meeting.Organizer))
            {
                return StatusCode(403);
            }

            // Check meeting hasn't started nor finished yet
            if (meeting.StartTime <= DateTime.Today)
            {
                return StatusCode(403);
            }

            // Check interval has a free spot
            int indexOfSpot = Array.FindIndex(
                meeting.Intervals[indexOfInterval].Attendees, booking => booking == null);
        
            if (indexOfSpot == -1) 
            {
                return StatusCode(403);
            }

            // Check student hasn't booked for this meeting yet
            int indexOfBooking = Array.FindIndex(meeting.Intervals[indexOfInterval].Attendees, 
            _booking => _booking != null && _booking.StudentId.Equals(studentId));

            if (indexOfBooking != -1)
            {
                return StatusCode(403);
            }

            // Check student hasn't booked for another meeting
            // overlapping in time with this one
            List<Meeting> bookedMeetings = _meetingsService.Filter(
                "{ \"Intervals.Attendees.StudentId\": \"" + studentId + "\" }");

            foreach(Meeting m in bookedMeetings)
            {
                foreach(Interval i in m.Intervals)
                {
                    foreach(Booking b in i.Attendees)
                    {
                        if (TimeOverlap(i.StartTime, i.EndTime, intervalStartTime, 
                        meeting.Intervals[indexOfInterval].EndTime))
                        {
                            return StatusCode(403);
                        }
                    }
                }
            }

            // Fill spot
            Booking newBooking = new Booking();
            newBooking.StudentId = studentId;
            meeting.Intervals[indexOfInterval].Attendees[indexOfSpot] = newBooking;

            _meetingsService.Update(id, meeting);

            return CreatedAtAction("GetMeeting", new { id = meeting.Id.ToString() }, meeting);
        }

        [HttpPatch("RemoveStudent/{id:length(24)}")]
        public async Task<ActionResult<Meeting>> RemoveStudent(string id, 
            [FromQuery] DateTime intervalStartTime, 
            [FromQuery] string studentId, 
            [FromQuery] string teacherId)
        {
            if (intervalStartTime == null || 
                studentId == null || 
                teacherId == null)
            {
                return BadRequest();
            }

            // Check user is a teacher
            try
            {
                if (! await _usersService.IsTeacher(teacherId))
                {
                    return StatusCode(403);
                }
            }
            catch
            {
                return StatusCode(500);
            }

            // Check meeting and interval exist
            var meeting = _meetingsService.Get(id);

            int indexOfInterval = Array.FindIndex(meeting.Intervals, i => 
                i.StartTime.Equals(intervalStartTime.ToUniversalTime()));

            if (meeting == null || indexOfInterval == -1)
            {
                return NotFound();
            }

            // Check teacher is the meeting's organizer
            if (! teacherId.Equals(meeting.Organizer))
            {
                return StatusCode(403);
            }

            // Check meeting hasn't started nor finished yet
            if (meeting.StartTime <= DateTime.Today)
            {
                return StatusCode(403);
            }

            // Check student had a booking
            int indexOfBooking = Array.FindIndex(meeting.Intervals[indexOfInterval].Attendees, 
            booking => booking != null && booking.StudentId.Equals(studentId));

            if (indexOfBooking == -1)
            {
                return StatusCode(403);
            }

            meeting.Intervals[indexOfInterval].Attendees[indexOfBooking] = null;
            _meetingsService.Update(id, meeting);

            return CreatedAtAction("GetMeeting", new { id = meeting.Id.ToString() }, meeting);
        }

        // Hides all attendees except userId
        private Meeting HideAttendeeList(Meeting meeting, string userId)
        {
            foreach(Interval interval in meeting.Intervals)
            {
                for(int i = 0; i < interval.Attendees.Length; i++)
                {
                    if (interval.Attendees[i] != null && 
                    !interval.Attendees[i].StudentId.Equals(userId))
                    {
                        interval.Attendees[i].StudentId = "Hidden attendee";
                        interval.Attendees[i].Comment = "Hidden comment";
                    }
                }
            }
            return meeting;
        }

        private bool TimeOverlap(DateTime start1, DateTime end1, DateTime start2, DateTime end2)
        {
            start1 = start1.ToUniversalTime();
            end1 = end1.ToUniversalTime();
            start2 = start2.ToUniversalTime();
            end2 = end2.ToUniversalTime();

            // Event 1 hasn't ended when event 2 starts
            if (start1 < start2 && end1 > start2)
            {
                return true;
            }
            // Event 2 hasn't ended when event 1 starts
            else if(start2 < start1 && end2 > start1)
            {
                return true;
            }

            return false;
        }
    }
}
