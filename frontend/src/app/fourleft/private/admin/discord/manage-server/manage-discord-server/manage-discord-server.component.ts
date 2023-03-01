import {Component, Input, OnInit} from '@angular/core';
import {DiscordIntegrationApiService} from "../../discord-integration-api.service";

@Component({
  selector: 'app-manage-discord-server',
  templateUrl: './manage-discord-server.component.html',
  styleUrls: ['./manage-discord-server.component.scss']
})
export class ManageDiscordServerComponent implements OnInit {

  @Input() guild: any;

  channels: any[] = [];

  constructor(private discordIntegrationApiService: DiscordIntegrationApiService) {
  }

  ngOnInit(): void {
    this.discordIntegrationApiService.getDiscordChannels(this.guild.id).subscribe((channels: any) => {
      this.channels = channels;
    });
  }



}
