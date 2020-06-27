using Microsoft.EntityFrameworkCore;

namespace MartinezRojo_Noelia_arso_laboratorio_Meetings.Models
{
    public class MeetingContext : DbContext
    {
        public MeetingContext(DbContextOptions<MeetingContext> options)
            : base(options)
        {
        }

        public DbSet<Meeting> Meetings { get; set; }
    }
}