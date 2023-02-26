import {Inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";


@Injectable()
export class DiscordIntegrationApiService {
  constructor(private httpClient: HttpClient) {
  }

  public isAuthenticated() {
    return this.httpClient.get('/api/discord/integration/authentication_status')
  }

  public postDiscordToken(token: string) {
    return this.httpClient.post("/api/discord/integration/auth", token);
  }

  public getDiscordGuilds() {
    return this.httpClient.get("/api/discord/integration/guilds");
  }
}
