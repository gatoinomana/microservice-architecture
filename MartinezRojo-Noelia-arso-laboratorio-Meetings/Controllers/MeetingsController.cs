using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using MartinezRojo_Noelia_arso_laboratorio_Meetings.Services;
using System.Text.Json;
using Microsoft.AspNetCore.Http;

namespace MartinezRojo_Noelia_arso_laboratorio_Meetings.Controllers
{
    [Route("api/Meetings")]
    [ApiController]
    public class MeetingsController : ControllerBase
    {
        private readonly MeetingsService _meetingsService;
        private readonly UsersService _usersService;
        private readonly MsgQueueService _msgQueueService;

        public MeetingsController(MeetingsService meetingsService, 
        UsersService usersService, MsgQueueService msgQueueService)
        {
            _meetingsService = meetingsService;
            _usersService = usersService;
            _msgQueueService = msgQueueService;
        }

        // POST: api/Meetings
        // To protect from overposting attacks, enable the specific properties you want to bind to, for
        // more details, see https://go.microsoft.com/fwlink/?linkid=2123754.
        [ProducesResponseType(StatusCodes.Status201Created)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        [ProducesResponseType(StatusCodes.Status403Forbidden)]
        [ProducesResponseType(StatusCodes.Status500InternalServerError)]
        [HttpPost]
        public async Task<ActionResult<Meeting>> PostMeeting(Meeting meeting, [FromQuery] string userId)
        {
            if (userId == null)
            {
                return BadRequest("Missing 'userId' query parameter");
            }

            // Check role of user
            bool isTeacher;

            try
            {
                isTeacher = await _usersService.IsTeacher(userId);
            }
            catch
            {
                return StatusCode(500, "Couldn't check the user's role");
            }

            // If user is a teacher, check meeting is theirs
            if (!isTeacher)
            {
                return StatusCode(403, "Only teachers can create meetings");;
            }

            // If user is a teacher, check meeting is theirs
            if (isTeacher && !meeting.Organizer.Equals(userId))
            {
                return StatusCode(403, "The user making the request must match the organizer");;
            }

            // Check all fields are valid
            BadRequestObjectResult result = ValidateFields(meeting);

            if (result != null) {
                return result;
            }

            // Check organizer doesn't have any other meeting
            // overlapping in time with this one
            List<Meeting> otherMeetings = _meetingsService.GetByOrganizer(meeting.Organizer);

            foreach (Meeting other in otherMeetings)
            {
                if (TimeOverlap(meeting.StartTime, meeting.EndTime,
                other.StartTime, other.EndTime))
                {
                    return StatusCode(403, "The same teacher cannot have too meetings overlapping in time");
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

            // Publish serialized event
            CreateTaskEvent _event = new CreateTaskEvent(
                "New meeting", meeting.BookingEndTime, meeting.Id);

            var options = new JsonSerializerOptions
            {
                PropertyNamingPolicy = JsonNamingPolicy.CamelCase
            };
            string jsonString = JsonSerializer.Serialize(_event, options);
            _msgQueueService.ProduceMessage(jsonString);

            return CreatedAtAction("GetMeeting", new { id = meeting.Id.ToString() }, meeting);
        }

        [ProducesResponseType(StatusCodes.Status204NoContent)]   
        [ProducesResponseType(StatusCodes.Status404NotFound)]        
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        [ProducesResponseType(StatusCodes.Status403Forbidden)]
        // DELETE: api/Meetings/5
        [HttpDelete("{id:length(24)}")]
        public async Task<IActionResult> DeleteMeeting(string id, [FromQuery] string userId)
        {
            if (userId == null)
            {
                return BadRequest("Missing 'userId' query parameter");
            }

            var meeting = _meetingsService.Get(id);

            if (meeting == null)
            {
                return NotFound("The id does not belong to any existing meeting");
            }

            // Check user is the meeting's organizer
            if (!meeting.Organizer.Equals(userId))
            {
                return StatusCode(403, "You must be the meeting's organizer to remove it");
            }

            // Check it is a future meeting
            if (meeting.StartTime <= DateTime.Today)
            {
                return StatusCode(403, "Only future meetings can be removed");
            }

            _meetingsService.Remove(meeting.Id);

            // Publish serialized event for each attendee
            List<string> studentIds = await _usersService.GetAllStudents();

            foreach(String studentId in studentIds)
            {
                RemoveTaskEvent _event = new RemoveTaskEvent(
                    studentId, meeting.Id);

                var options = new JsonSerializerOptions
                {
                    PropertyNamingPolicy = JsonNamingPolicy.CamelCase
                };
                string jsonString = JsonSerializer.Serialize(_event, options);
                _msgQueueService.ProduceMessage(jsonString);
            }
            
            return NoContent();
        }
        
        [ProducesResponseType(StatusCodes.Status204NoContent)]   
        [ProducesResponseType(StatusCodes.Status404NotFound)]        
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        [ProducesResponseType(StatusCodes.Status403Forbidden)]
        // DELETE: api/Meetings
        [HttpDelete]
        public async Task<IActionResult> DeleteMeetings([FromQuery] string userId)
        {
            if (userId == null)
            {
                return BadRequest("Missing 'userId' query parameter");
            }

            // Check user is a teacher
            bool isTeacher;

            try
            {
                isTeacher = await _usersService.IsTeacher(userId);
            }
            catch
            {
                return StatusCode(500, "Couldn't check the user's role");
            }

            if (!isTeacher)
            {
                return StatusCode(403, "Only teachers can delete meetings");
            }

            // Check they have future meetings to delete
            List<Meeting> theirFutureMeetings = 
                _meetingsService.GetByOrganizer(userId)
                .FindAll(m => m.StartTime > DateTime.Today);
            
            if (!theirFutureMeetings.Any())
            {
                return NotFound("The user has no future meetings to delete");
            }

            foreach(Meeting m in theirFutureMeetings)
            {
                _meetingsService.Remove(m);
            }

            // Publish serialized event for each attendee of each meeting
            List<string> studentIds = await _usersService.GetAllStudents();

            foreach(String studentId in studentIds)
            {
                foreach(Meeting meeting in theirFutureMeetings)
                {
                    RemoveTaskEvent _event = new RemoveTaskEvent(
                        studentId, meeting.Id);

                    var options = new JsonSerializerOptions
                    {
                        PropertyNamingPolicy = JsonNamingPolicy.CamelCase
                    };
                    string jsonString = JsonSerializer.Serialize(_event, options);
                    _msgQueueService.ProduceMessage(jsonString);
                }
            }

            return NoContent();
        }

        [ProducesResponseType(StatusCodes.Status200OK)]   
        [ProducesResponseType(StatusCodes.Status404NotFound)]        
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        [ProducesResponseType(StatusCodes.Status403Forbidden)]
        // For use cases AddAttendee (as teacher) and JoinMeeting (as student)
        [HttpPatch("{id:length(24)}/add-attendee")]
        public async Task<ActionResult<Meeting>> AddAttendee(
            string id, [FromQuery] string userId, BookingDTO dto)
        {
            if (userId == null)
            {
                return BadRequest("Missing 'userId' query parameter");
            }

            // Check role of attendee inside DTO
            bool attendeeIsStudent;

            try
            {
                attendeeIsStudent = await _usersService.IsStudent(dto.StudentId);
            }
            catch
            {
                return StatusCode(500, "Couldn't check the user's role");
            }

            var meeting = _meetingsService.Get(id);

            // Check attendee is a student
            if (!attendeeIsStudent)
            {
                return StatusCode(403, "Only students can attend meetings");
            }

            // Check user performing request can add attendees
            if (!userId.Equals(meeting.Organizer) && !userId.Equals(dto.StudentId))
            {
                return StatusCode(403, 
                "To add an attendee, you must be the organizer or be the student being added");
            }

            // Check meeting and interval exist
            int indexOfInterval = Array.FindIndex(meeting.Intervals, i => 
                i.StartTime.Equals(dto.IntervalStartTime.ToUniversalTime()));

            if (meeting == null || indexOfInterval == -1)
            {
                return NotFound("The meeting or interval do not exist");
            }

            // Check it is a future meeting
            if (meeting.StartTime <= DateTime.Today || meeting.EndTime <= DateTime.Today)
            {
                return StatusCode(403, "You can only add attendees to future meetings");
            }
            
            // If student, check booking period is still open
            bool isStudent = userId.Equals(dto.StudentId);

            if (isStudent)
            {
                if (meeting.BookingEndTime <= DateTime.Today || 
                    meeting.BookingStartTime > DateTime.Today)
                {
                    return StatusCode(403, 
                    "Students can only book for meetings during the booking period");
                }
            }

            // Check interval has a free spot
            int indexOfSpot = Array.FindIndex(
                meeting.Intervals[indexOfInterval].Attendees, booking => booking == null);
        
            if (indexOfSpot == -1) 
            {
                return StatusCode(403, "The meeting is fully booked");
            }

            // Check student hasn't booked for this meeting yet
            bool alreadyBooked = meeting.Intervals.ToList().Any(interval => 
                interval.Attendees.ToList().Any(booking => 
                    booking != null && booking.StudentId.Equals(dto.StudentId)));

            if (alreadyBooked)
            {
                return StatusCode(403, "Student already has a booking for this meeting");
            }

            // Check student hasn't booked for another meeting
            // overlapping in time with this one
            List<Meeting> bookedMeetings = _meetingsService.Filter(
                "{ \"Intervals.Attendees.StudentId\": \"" + dto.StudentId + "\" }");

            foreach(Meeting m in bookedMeetings)
            {
                foreach(Interval i in m.Intervals)
                {
                    foreach(Booking b in i.Attendees)
                    {
                        if (TimeOverlap(i.StartTime, i.EndTime, dto.IntervalStartTime, 
                        meeting.Intervals[indexOfInterval].EndTime))
                        {
                            return StatusCode(403, 
                            "Student has a booking for another meeting that overlaps in time with this one");
                        }
                    }
                }
            }

            // Fill spot
            Booking newBooking = new Booking();
            newBooking.StudentId = dto.StudentId;

            if (isStudent)
            {
                newBooking.Comment = dto.Comment;
            }

            meeting.Intervals[indexOfInterval].Attendees[indexOfSpot] = newBooking;

            _meetingsService.Update(id, meeting);

            RemoveTaskEvent _event = new RemoveTaskEvent(
                dto.StudentId, meeting.Id);

            var options = new JsonSerializerOptions
            {
                PropertyNamingPolicy = JsonNamingPolicy.CamelCase
            };
            string jsonString = JsonSerializer.Serialize(_event, options);
            _msgQueueService.ProduceMessage(jsonString);

            return Ok(meeting);
        }

        [ProducesResponseType(StatusCodes.Status200OK)]   
        [ProducesResponseType(StatusCodes.Status404NotFound)]        
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        [ProducesResponseType(StatusCodes.Status403Forbidden)]
        [HttpPatch("{id:length(24)}/remove-attendee")]
        public ActionResult<Meeting> RemoveAttendee(
            string id, [FromQuery] string userId, BookingDTO dto)
        {
            if (userId == null)
            {
                return BadRequest("Missing 'userId' query parameter");
            }

            var meeting = _meetingsService.Get(id);

            // Check user performing request can remove attendees
            if (!userId.Equals(meeting.Organizer) && !userId.Equals(dto.StudentId))
            {
                return StatusCode(403, "To remove an attendee, you must be the organizer or the attendee");
            }

            // Check meeting and interval exist
            int indexOfInterval = Array.FindIndex(meeting.Intervals, i => 
                i.StartTime.Equals(dto.IntervalStartTime.ToUniversalTime()));

            if (meeting == null || indexOfInterval == -1)
            {
                return NotFound("The meeting or interval does not exist");
            }

            // Check it is a future meeting
            if (meeting.StartTime <= DateTime.Today || meeting.EndTime <= DateTime.Today)
            {
                return StatusCode(403, "You can only remove attendees from future meetings");
            }

            // Check student had a booking in this meeting and interval
            int indexOfBooking = Array.FindIndex(meeting.Intervals[indexOfInterval].Attendees, 
                booking => booking != null && booking.StudentId.Equals(dto.StudentId));

            if (indexOfBooking == -1)
            {
                return StatusCode(403, 
                "Student didn't have a booking for this meeting at the indicated interval");
            }

            meeting.Intervals[indexOfInterval].Attendees[indexOfBooking] = null;
            _meetingsService.Update(id, meeting);

            return Ok(meeting);
        }

        [ProducesResponseType(StatusCodes.Status200OK)]      
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        [ProducesResponseType(StatusCodes.Status403Forbidden)]
        [ProducesResponseType(StatusCodes.Status500InternalServerError)]     
        // GET: api/Meetings
        [HttpGet]
        public async Task<ActionResult<List<Meeting>>> GetMeetings([FromQuery] string userId)
        {
            if (userId == null)
            {
                return BadRequest("Missing 'userId' query parameter");
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
                return StatusCode(500, "Couldn't check the user's role");
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
                return StatusCode(403, "Only registered teachers and students can see meetings");
            }
        }
        
        [ProducesResponseType(StatusCodes.Status200OK)]      
        [ProducesResponseType(StatusCodes.Status404NotFound)]      
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        [ProducesResponseType(StatusCodes.Status403Forbidden)]
        [ProducesResponseType(StatusCodes.Status500InternalServerError)]     
        // GET: api/Meetings/5
        [HttpGet("{id:length(24)}", Name = "GetMeeting")]
        public async Task<ActionResult<Meeting>> GetMeeting(string id, [FromQuery] string userId)
        {
            if (userId == null)
            {
                return BadRequest("Missing 'userId' query parameter");
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
                return StatusCode(500, "Couldn't check the user's role");
            }

            // Check meeting exists
            var meeting = _meetingsService.Get(id);

            if (meeting == null)
            {
                return NotFound("The id does not belong to any existing meeting");
            }
            
            // Discard if past meeting
            if (meeting.StartTime < DateTime.Today)
            {
                return StatusCode(403, "Only future surveys are visible");
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
                return StatusCode(403, "Only registered teachers and students can see meetings");
            }
        }
        private BadRequestObjectResult ValidateFields(Meeting meeting) {

            if (meeting.Title == null)
            {
                return BadRequest("'Title' cannot be empty");
            }
            if (meeting.Organizer == null)
            {
                return BadRequest("'Organizer' cannot be empty");
            }
            if (meeting.Location == null)
            {
                return BadRequest("'Location' cannot be empty");
            }
            if (meeting.StartTime == null)
            {
                return BadRequest("'StartTime' cannot be empty");
            }
            if (meeting.EndTime == null)
            {
                return BadRequest("'EndTime' cannot be empty");
            }
            if (meeting.BookingStartTime == null)
            {
                return BadRequest("'BookingStartTime' cannot be empty");
            }
            if (meeting.BookingEndTime == null)
            {
                return BadRequest("'BookingEndTime' cannot be empty");
            }
            if (meeting.TotalSpots <= 0)
            {
                return BadRequest("'TotalSpots' must be a positive integer");
            }
            if (meeting.SpotsPerInterval <= 0)
            {
                return BadRequest("'SpotsPerInterval' must be a positive integer");
            }
            if (meeting.TotalSpots < meeting.SpotsPerInterval)
            {
                return BadRequest("'SpotsPerInterval' cannot be less than 'TotalSpots'");
            }
            if (meeting.TotalSpots % meeting.SpotsPerInterval != 0)
            {
                return BadRequest("'TotalSpots' must be a multiple of 'SpotsPerInterval'");
            }
            if (meeting.StartTime < DateTime.Today)
            {
                return BadRequest("Meeting cannot start in the past");
            }
            if (meeting.EndTime <= meeting.StartTime)
            {
                return BadRequest("'EndTime' must come after 'StartTime'");
            }
            if (meeting.BookingStartTime >= meeting.StartTime || 
                meeting.BookingEndTime <= meeting.BookingStartTime)
            {
                return BadRequest("Booking time must be start and end before the meeting starts");
            }

            return null;
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
