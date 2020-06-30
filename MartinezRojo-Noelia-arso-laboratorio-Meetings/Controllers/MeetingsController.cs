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
            await _usersService.OnGet(meeting.Organizer);
            var organizerRole = _usersService.UserRole;

            if (organizerRole != null && organizerRole.Equals("TEACHER"))
            {
                _meetingsService.Create(meeting);

                // Produce RabbitMQ message
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
    }
}
