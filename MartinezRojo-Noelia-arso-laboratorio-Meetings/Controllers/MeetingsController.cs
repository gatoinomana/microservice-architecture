using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MartinezRojo_Noelia_arso_laboratorio_Meetings.Models;
using MartinezRojo_Noelia_arso_laboratorio_Meetings.Services;

namespace MartinezRojo_Noelia_arso_laboratorio_Meetings.Controllers
{
    [Route("api/Meetings")]
    [ApiController]
    public class MeetingsController : ControllerBase
    {
        private readonly MeetingService _meetingService;

        public MeetingsController(MeetingService meetingService)
        {
            _meetingService = meetingService;
        }

        // GET: api/Meetings
        [HttpGet]
        public ActionResult<List<Meeting>> GetMeetings()
        {
            return _meetingService.Get();
        }
            
        // GET: api/Meetings/5
        [HttpGet("{id:length(24)}", Name = "GetMeeting")]
        public ActionResult<Meeting> GetMeeting(string id)
        {
            var meeting = _meetingService.Get(id);

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
            var meeting = _meetingService.Get(id);

            if (meeting == null)
            {
                return NotFound();
            }

            _meetingService.Update(id, meetingIn);

            return NoContent();
        }

        // POST: api/Meetings
        // To protect from overposting attacks, enable the specific properties you want to bind to, for
        // more details, see https://go.microsoft.com/fwlink/?linkid=2123754.
        [HttpPost]
        public ActionResult<Meeting> PostMeeting(Meeting meeting)
        {
            _meetingService.Create(meeting);

            return CreatedAtAction("GetMeeting", new { id = meeting.Id.ToString() }, meeting);
        }

        // DELETE: api/Meetings/5
        [HttpDelete("{id:length(24)}")]
        public IActionResult DeleteMeeting(string id)
        {
            var meeting = _meetingService.Get(id);

            if (meeting == null)
            {
                return NotFound();
            }

            _meetingService.Remove(meeting.Id);

            return NoContent();
        }
    }
}
