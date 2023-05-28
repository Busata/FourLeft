import {Component, Input, OnInit} from '@angular/core';
import {DiscordIntegrationApiService} from "../../discord-integration-api.service";
import {DiscordChannelSummaryTo, DiscordGuildMemberTo, DiscordMemberTo} from '@server-models';

@Component({
  selector: 'app-manage-discord-server',
  templateUrl: './manage-discord-server.component.html',
  styleUrls: ['./manage-discord-server.component.scss']
})
export class ManageDiscordServerComponent implements OnInit {

  @Input() guild: any;

  channels: DiscordChannelSummaryTo[] = [];
  members: DiscordGuildMemberTo[] = [];
  administrators: DiscordGuildMemberTo[] = [];

  public filter: any = '';
  selectedMember: DiscordGuildMemberTo | undefined;

  constructor(private discordIntegrationApiService: DiscordIntegrationApiService) {
  }

  ngOnInit(): void {
    this.discordIntegrationApiService.getDiscordChannels(this.guild.id).subscribe((channels: any) => {
      this.channels = channels;
    });

    this.discordIntegrationApiService.getDiscordMembers(this.guild.id).subscribe((members: any) => {
      this.members = members;
    })

    this.discordIntegrationApiService.getDiscordAdministrators(this.guild.id).subscribe((administrators: any) => {
      this.administrators = administrators;
    })
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

  grantAccess(guildId: string, memberId: string) {
    this.discordIntegrationApiService.grantAccess(guildId, memberId).subscribe(member => {
      this.administrators.push(member);
      this.members = this.members.filter(member => member.id !== memberId);
    });
  }

  removeAccess(guildId: string, memberId: string) {
    this.discordIntegrationApiService.removeAccess(guildId, memberId).subscribe(member => {
      this.administrators = this.administrators.filter(member => member.id !== memberId);
      this.members.push(member);
    });

  }

  updateSelectedMember(member: any) {
    this.selectedMember = member;
  }
}
