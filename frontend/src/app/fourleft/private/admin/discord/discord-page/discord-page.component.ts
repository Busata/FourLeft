import { Component } from '@angular/core';
import {DiscordIntegrationApiService} from "../discord-integration-api.service";

@Component({
  selector: 'app-discord-integration-container',
  templateUrl: './discord-page.component.html',
  styleUrls: ['./discord-page.component.scss'],
  providers: []
})
export class DiscordPageComponent {

  constructor(private discordIntegrationApiService: DiscordIntegrationApiService) {
  }

  getGuilds() {
    this.discordIntegrationApiService.getDiscordGuilds();
  }
}
