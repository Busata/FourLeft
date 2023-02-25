import { Component } from '@angular/core';
import {DiscordIntegrationApiService} from "../discord-integration-api.service";
import {DiscordIntegrationService} from "../discord-integration.service";

@Component({
  selector: 'app-discord-integration-container',
  templateUrl: './discord-integration-container.component.html',
  styleUrls: ['./discord-integration-container.component.scss'],
  providers: [DiscordIntegrationService]
})
export class DiscordIntegrationContainerComponent {

  constructor(private discordIntegrationService: DiscordIntegrationService, private discordIntegrationApiService: DiscordIntegrationApiService) {
  }

  authWithDiscord() {
    let discordAuthentication = <Window> window.open("https://discord.com/api/oauth2/authorize?client_id=1016221531396907038&redirect_uri=http%3A%2F%2Flocalhost%3A4200%2Fprivate%2Fdiscord_callback%2F&response_type=code&scope=identify%20guilds", 'popup', 'popup,width=600,height=700')
    let timer = setInterval(function() {
      if(discordAuthentication.closed) {
        clearInterval(timer);
        console.log("screen closed");
      }
    }, 1000);
  }

  getGuilds() {
    this.discordIntegrationApiService.getDiscordGuilds();
  }
}
