import {Component, Input} from '@angular/core';
import {
  CreateDiscordChannelConfigurationTo,
  ViewPointsTo,
  ViewResultTo
} from "../../../../../../../common/generated/server-models";
import {ViewResultsStoreService} from '../../services/view-results-store.service';

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

  points: ViewPointsTo | undefined = undefined;

  constructor(private viewResultsStoreService: ViewResultsStoreService) {
  }

  updatePreview(value: CreateDiscordChannelConfigurationTo) {
    this.viewResultsStoreService.getResults(value.clubView.id).subscribe(results => {
      this.results = results;
    })

    this.viewResultsStoreService.getPoints(value.clubView.id).subscribe(points => {
      this.points = points;
    });
  }
}
