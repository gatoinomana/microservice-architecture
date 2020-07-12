using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Options;
using MartinezRojo_Noelia_arso_laboratorio_Meetings.Models;
using MartinezRojo_Noelia_arso_laboratorio_Meetings.Services;

namespace MartinezRojo_Noelia_arso_laboratorio_Meetings
{
    public class Startup
    {
        public Startup(IConfiguration configuration)
        {
            Configuration = configuration;
        }

        public IConfiguration Configuration { get; }

        // This method gets called by the runtime. Use this method to add services to the container.
        public void ConfigureServices(IServiceCollection services)
        {
            services.AddHttpClient();

            // Database settings
            services.Configure<MeetingsDatabaseSettings>(
                Configuration.GetSection(nameof(MeetingsDatabaseSettings)));

            services.AddSingleton<IMeetingsDatabaseSettings>(sp =>
                sp.GetRequiredService<IOptions<MeetingsDatabaseSettings>>().Value);
            
            // Service to communicate with database
            services.AddSingleton<MeetingsService>();

            // Service to communicate with Users microservice
            services.AddSingleton<UsersService>();

            // Service to communique with RabbitMQ queue
            services.AddSingleton<MsgQueueService>();

            // Register the Swagger services
            services.AddSwaggerDocument();
            
            services.AddControllers();
        }

        // This method gets called by the runtime. Use this method to configure the HTTP request pipeline.
        public void Configure(IApplicationBuilder app, IWebHostEnvironment env)
        {
            if (env.IsDevelopment())
            {
                app.UseDeveloperExceptionPage();
            }

            app.UseHttpsRedirection();

            app.UseRouting();

            app.UseAuthorization();

            app.UseEndpoints(endpoints =>
            {
                endpoints.MapControllers();
            });

            // Register the Swagger generator and the Swagger UI middlewares
            app.UseOpenApi();
            app.UseSwaggerUi3();
        }
    }
}
