import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class ConfigureChannelService {

  constructor(private httpClient: HttpClient) { }


  saveConfiguration(guildId: number, channelId: number, config: any) {
    return this.httpClient.post(`/api/internal/discord/channels/${channelId}/configuration`, config);
  }

  getConfiguration(guildId: number, channelId: number) {
    return this.httpClient.get(`/api/internal/discord/channels/${channelId}/configuration`);
  }
}
