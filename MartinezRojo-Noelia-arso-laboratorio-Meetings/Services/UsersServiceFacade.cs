using System.Net.Http;
using System.Text.Json;
using System.IO;
using System.Threading.Tasks;
public class UsersServiceFacade
{
    private readonly IHttpClientFactory _clientFactory;
    public string UserRole { get; private set; }
    public bool GetUserRoleError { get; private set; }

    public UsersServiceFacade(IHttpClientFactory clientFactory)
    {
        _clientFactory = clientFactory;
    }

    public async Task OnGet(string userId)
    {
        var request = new HttpRequestMessage(HttpMethod.Get,
            "http://localhost:8080/api/users/" + userId + "/role");

        var client = _clientFactory.CreateClient();

        var response = await client.SendAsync(request);

        if (response.IsSuccessStatusCode)
        {
            using var responseStream = await response.Content.ReadAsStreamAsync();
            StreamReader reader =  new StreamReader( responseStream );
            string role = reader.ReadToEnd();

            //JsonSerializer.DeserializeAsync<string>
            UserRole = role;
        }
        else
        {
            UserRole = null;
            GetUserRoleError = true;
        }
    }
}