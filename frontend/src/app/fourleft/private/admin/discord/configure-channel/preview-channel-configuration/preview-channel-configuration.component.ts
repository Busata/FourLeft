import {Component, Input} from '@angular/core';
import {CreateDiscordChannelConfigurationTo, ViewResultTo} from "../../../../../../common/generated/server-models";
import {PreviewChannelConfigurationService} from "../services/preview-channel-configuration.service";

@Component({
  selector: 'app-preview-channel-configuration',
  templateUrl: './preview-channel-configuration.component.html',
  styleUrls: ['./preview-channel-configuration.component.scss']
})
export class PreviewChannelConfigurationComponent {

  @Input()
  set configuration(value: CreateDiscordChannelConfigurationTo) {
    if(!value) { return;}
    this.updatePreview(value)
  }

  results: ViewResultTo | undefined = undefined;

  constructor(private previewChannelConfigurationService: PreviewChannelConfigurationService) {
  }

  updatePreview(value: CreateDiscordChannelConfigurationTo) {
    this.previewChannelConfigurationService.getResults(value.clubView.id).subscribe(results => {
      this.results = results;
    })
  }
}
