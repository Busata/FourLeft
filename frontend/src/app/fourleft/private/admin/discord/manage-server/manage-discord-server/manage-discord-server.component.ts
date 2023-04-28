import {Component, Input, OnInit} from '@angular/core';
import {DiscordIntegrationApiService} from "../../discord-integration-api.service";
import {DiscordChannelSummaryTo} from '@server-models';

@Component({
  selector: 'app-manage-discord-server',
  templateUrl: './manage-discord-server.component.html',
  styleUrls: ['./manage-discord-server.component.scss']
})
export class ManageDiscordServerComponent implements OnInit {

  @Input() guild: any;

  channels: DiscordChannelSummaryTo[] = [];
  public filter: any = '';

  constructor(private discordIntegrationApiService: DiscordIntegrationApiService) {
  }

  ngOnInit(): void {
    this.discordIntegrationApiService.getDiscordChannels(this.guild.id).subscribe((channels: any) => {
      this.channels = channels;
    });
  }


  get configuredChannels() {
    return this.channels.filter(channel => channel.hasConfiguration);
  }

  get unconfiguredChannels() {
    return this.channels.filter(channel => !channel.hasConfiguration).filter(
      channel => this.filter == '' || channel.name.indexOf(this.filter) !== -1
    );
  }


  setChannelFilter(value: any) {
    this.filter = value;
  }
}
