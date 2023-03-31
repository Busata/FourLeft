import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {ConfigureChannelService} from "../services/configure-channel.service";
import {BehaviorSubject} from "rxjs";
import {CreateDiscordChannelConfigurationTo} from "../../../../../../common/generated/server-models";

@Component({
  selector: 'app-configure-channel-container',
  templateUrl: './configure-channel-container.component.html',
  styleUrls: ['./configure-channel-container.component.scss']
})
export class ConfigureChannelContainerComponent implements OnInit {

  public existingForm = new BehaviorSubject<any>(undefined);

  constructor(private activatedRoute: ActivatedRoute, private configureChannelService: ConfigureChannelService) {
  }

  saveConfiguration($event: any) {
    this.activatedRoute.params.subscribe((params: any) => {

      this.configureChannelService.saveConfiguration(params.guildId, params.channelId, $event).subscribe(() => {
        this.updateConfig(params);
      });
    })
  }

  ngOnInit(): void {
    this.activatedRoute.params.subscribe((params: any) => {

      this.updateConfig(params);
    })
  }

  private updateConfig(params: any) {
    this.configureChannelService.getConfiguration(params.guildId, params.channelId).subscribe(configuration => {
      this.existingForm.next(configuration);
    });
  }
}
