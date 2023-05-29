import {Injectable} from "@angular/core";
import {BehaviorSubject} from "rxjs";
import {DiscordIntegrationApiService} from "./discord-integration-api.service";


@Injectable()
export class DiscordStateService {
  private _guildSummarySubject = new BehaviorSubject<any[]>([]);
  public guildSummaries = this._guildSummarySubject.asObservable();


  constructor(private discordIntegrationApiService: DiscordIntegrationApiService) {
  }


  public getGuilds(guildId: string = "") {
    this.discordIntegrationApiService.getDiscordGuilds(guildId).subscribe((guilds:any) => {
      this._guildSummarySubject.next(guilds);
    });
  }




}
