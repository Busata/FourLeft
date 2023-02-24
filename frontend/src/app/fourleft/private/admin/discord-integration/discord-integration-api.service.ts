import {Inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";


@Injectable()
export class DiscordIntegrationApiService {
  constructor(private httpClient: HttpClient) {
  }

  public postDiscordToken(token: string) {
    return this.httpClient.post("/api/discord/integration/auth", token);
  }

  public getDiscordGuilds() {
    this.httpClient.get("/api/discord/integration/guilds").subscribe(response => {
      console.log(response);
    });
  }
}
