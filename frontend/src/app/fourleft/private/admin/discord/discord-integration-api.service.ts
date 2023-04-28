import {Inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {DiscordChannelSummaryTo} from '@server-models';


@Injectable()
export class DiscordIntegrationApiService {
  constructor(private httpClient: HttpClient) {
  }

  public isAuthenticated() {
    return this.httpClient.get('/api/discord/integration/authentication_status')
  }

  public getDiscordGuild(guildId: string) {
    return this.httpClient.get(`/api/discord/integration/guilds/${guildId}`);
  }

  public getDiscordChannels(guildId: string) {
    return this.httpClient.get<DiscordChannelSummaryTo>(`/api/discord/integration/guilds/${guildId}/channels`);
  }

  public canManageServer(guildId: string) {
    return this.httpClient.get(`/api/discord/integration/guilds/${guildId}/can_manage`);
  }

  public postDiscordToken(token: string) {
    return this.httpClient.post("/api/discord/integration/auth", token);
  }

  public getDiscordGuilds() {
    return this.httpClient.get("/api/discord/integration/guilds");
  }
}
