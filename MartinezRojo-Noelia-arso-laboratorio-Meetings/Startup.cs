using System;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using MartinezRojo_Noelia_arso_laboratorio_Meetings.Models;
using MartinezRojo_Noelia_arso_laboratorio_Meetings.Services;
using Microsoft.Extensions.Options;

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

            services.Configure<MeetingsDatabaseSettings>(
                Configuration.GetSection(nameof(MeetingsDatabaseSettings)));

            services.AddSingleton<IMeetingsDatabaseSettings>(sp =>
                sp.GetRequiredService<IOptions<MeetingsDatabaseSettings>>().Value);
            
            services.AddSingleton<MeetingsService>();
            services.AddSingleton<UsersServiceFacade>();
            
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
        }
    }
}
