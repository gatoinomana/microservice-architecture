using System.Net.Http;
using System.Text.Json;
using System.Threading.Tasks;
using System;
using System.Collections.Generic;
using System.IO;
public class UsersService
{
    private readonly IHttpClientFactory _clientFactory;
    protected class RoleResponse {
        public string role { get; set; }
    }
    public UsersService(IHttpClientFactory clientFactory)
    {
        _clientFactory = clientFactory;
    }

    public async Task<bool> IsStudent(string userId) {
        RoleResponse response = await GetRole(userId);
        return response.role.Equals("STUDENT");
    }

    public async Task<bool> IsTeacher(string userId) {
        RoleResponse role = await GetRole(userId);
        return role.role.Equals("TEACHER");
    }
    
    private async Task<RoleResponse> GetRole(string userId)
    {
        var request = new HttpRequestMessage(HttpMethod.Get,
            "http://localhost:8080/api/users/" + userId + "/role");

        var client = _clientFactory.CreateClient();

        var response = await client.SendAsync(request);

        if (response.IsSuccessStatusCode)
        {
            string jsonString = await response.Content.ReadAsStringAsync();
            return JsonSerializer.Deserialize<RoleResponse>(jsonString);
        }
        else
        {
            return null;
        } 
    }

    public async Task<List<String>> GetAllStudents()
    {
        List<string> studentIds = new List<string>();

        var request = new HttpRequestMessage(HttpMethod.Get,
            "http://localhost:8080/api/users/students");

        var client = _clientFactory.CreateClient();

        var response = await client.SendAsync(request);

        if (response.IsSuccessStatusCode)
        {
            string jsonString = await response.Content.ReadAsStringAsync();

            using (JsonDocument document = JsonDocument.Parse(jsonString))
            {
                JsonElement root = document.RootElement;
                JsonElement studentsElement = root.GetProperty("students");
                foreach (JsonElement student in studentsElement.EnumerateArray())
                {
                    studentIds.Add(student.GetProperty("email").GetString());
                }
            }
        }

        return studentIds;
    }
}