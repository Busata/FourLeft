import {Component, Input} from '@angular/core';
import {
  DiscordChannelConfigurationTo, VehicleEntryTo,
  ViewPointsTo,
  ViewResultTo
} from "@server-models";
import {ViewResultsStoreService} from '../../services/view-results-store.service';

@Component({
  selector: 'app-preview-channel-configuration',
  templateUrl: './preview-channel-configuration.component.html',
  styleUrls: ['./preview-channel-configuration.component.scss']
})
export class PreviewChannelConfigurationComponent {

  @Input()
  set configuration(value: DiscordChannelConfigurationTo) {
    if(!value) { return;}
    this.updatePreview(value)
  }

  results: ViewResultTo | undefined = undefined;

  points: ViewPointsTo | undefined = undefined;

  constructor(private viewResultsStoreService: ViewResultsStoreService) {
  }

  updatePreview(value: DiscordChannelConfigurationTo) {
    this.viewResultsStoreService.getResults(value.clubView.id).subscribe(results => {
      this.results = results;
    })

    this.viewResultsStoreService.getPoints(value.clubView.id).subscribe(points => {
      this.points = points;
    });
  }

  isValidVehicle(vehicles: VehicleEntryTo[]) {
    return vehicles.every(v=> v.vehicleAllowed);
  }
}
