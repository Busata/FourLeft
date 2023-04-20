import { Component } from '@angular/core';
import {DiscordIntegrationApiService} from "../../discord-integration-api.service";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'app-manage-discord-server-container',
  templateUrl: './manage-discord-server-container.component.html',
  styleUrls: ['./manage-discord-server-container.component.scss']
})
export class ManageDiscordServerContainerComponent {

  public guild: any = null;

  constructor(private discordIntegrationApiService: DiscordIntegrationApiService, private activeRoute: ActivatedRoute) {

  }

  ngOnInit(): void {
    let guildId = <string> this.activeRoute.snapshot.paramMap.get('guildId');
    this.discordIntegrationApiService.getDiscordGuild(guildId).subscribe(guild => {
      this.guild = guild;
    })
  }
}
