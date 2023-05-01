import {Component, OnInit} from '@angular/core';
import {DiscordIntegrationApiService} from "../discord-integration-api.service";
import {DiscordStateService} from "../discord-state.service";

@Component({
  selector: 'app-discord-home',
  templateUrl: './discord-home.component.html',
  styleUrls: ['./discord-home.component.scss']
})
export class DiscordHomeComponent implements OnInit{


  constructor(public discordStateService: DiscordStateService) {
  }

  ngOnInit(): void {
    this.discordStateService.getGuilds();
  }

  getGuildIcon(guild: any) {
    return `https://cdn.discordapp.com/icons/${guild.id}/${guild.icon}.png`
  }

  inviteBot(guild: any) {
    let botInvite = <Window> window.open(`/api/external/discord/invite_bot?guild_id=${guild.id}`, 'popup', 'popup,width=600,height=700');
    let timer = setInterval(() => {
      if(botInvite.closed) {
        clearInterval(timer);
        this.discordStateService.getGuilds();
      }
    }, 1000);
  }
}
