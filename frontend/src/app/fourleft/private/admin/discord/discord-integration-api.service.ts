import {Inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {DiscordChannelSummaryTo, DiscordGuildMemberTo, DiscordMemberTo} from '@server-models';


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
    return this.httpClient.get<DiscordGuildMemberTo>(`/api/internal/discord/integration/guilds/${guildId}/members`);
  }
  public getDiscordAdministrators(guildId: string) {
    return this.httpClient.get<DiscordGuildMemberTo>(`/api/internal/discord/integration/guilds/${guildId}/administrators`);
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

  grantAccess(guildId: string, memberId: string) {
    return this.httpClient.post<DiscordGuildMemberTo>(`/api/internal/discord/integration/guilds/${guildId}/administrators/${memberId}`, {});
  }

  removeAccess(guildId: string, memberId: string) {
    return this.httpClient.delete<DiscordGuildMemberTo>(`/api/internal/discord/integration/guilds/${guildId}/administrators/${memberId}`);

  }
}
