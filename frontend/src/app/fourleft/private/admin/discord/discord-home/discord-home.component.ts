import {Component, OnInit} from '@angular/core';
import {DiscordStateService} from "../discord-state.service";
import {map} from 'rxjs';
import {DiscordIntegrationApiService} from '../discord-integration-api.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-discord-home',
  templateUrl: './discord-home.component.html',
  styleUrls: ['./discord-home.component.scss']
})
export class DiscordHomeComponent implements OnInit{

  public authenticated = false;

  constructor(public discordStateService: DiscordStateService,
              private router: Router,
              private discordIntegrationService: DiscordIntegrationApiService) {}

  ngOnInit(): void {
    this.discordStateService.getGuilds();
    this.discordIntegrationService.isAuthenticated$.subscribe((status: any) => {
      this.authenticated = status.authenticated;
    });
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

  authenticateDiscord() {
    return this.discordIntegrationService.isAuthenticated$.pipe(map((status: any) => {
      if(!status.authenticated) {
        this.router.navigate(["/private/discord/authenticate"]);
        return true;
      }
      return status.authenticated;
    })).subscribe(authenticated => {
    this.authenticated = authenticated;
    });
  }
}
