import {Inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {DiscordChannelSummaryTo, DiscordMemberTo} from '@server-models';


@Injectable()
export class DiscordIntegrationApiService {

  public isAuthenticated$ = this.httpClient.get('/api/internal/discord/integration/authentication_status')
  constructor(private httpClient: HttpClient) {
  }

  public isAuthenticated() {
    return this.httpClient.get('/api/internal/discord/integration/authentication_status')
  }

  public getDiscordGuild(guildId: string) {
    return this.httpClient.get(`/api/internal/discord/integration/guilds/${guildId}`);
  }

  public getDiscordChannels(guildId: string) {
    return this.httpClient.get<DiscordChannelSummaryTo>(`/api/internal/discord/integration/guilds/${guildId}/channels`);
  }
  public getDiscordMembers(guildId: string) {
    return this.httpClient.get<DiscordMemberTo>(`/api/internal/discord/integration/guilds/${guildId}/members`);
  }

  public canManageServer(guildId: string) {
    return this.httpClient.get(`/api/internal/discord/integration/guilds/${guildId}/can_manage`);
  }

  public postDiscordToken(token: string) {
    return this.httpClient.post("/api/internal/discord/integration/auth", token);
  }

  public getDiscordGuilds() {
    return this.httpClient.get("/api/internal/discord/integration/guilds");
  }
}
