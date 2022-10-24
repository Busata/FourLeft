import {Component, Input, OnInit} from '@angular/core';
import {ActiveEventInfo} from '../domain/active-event-info';
import {Tier} from '../domain/tier';
import {ClubTiersService} from '../club-tiers.service';
import {take} from 'rxjs';

@Component({
  selector: 'app-tier-vehicle-configuration',
  templateUrl: './tier-vehicle-configuration.component.html',
  styleUrls: ['./tier-vehicle-configuration.component.scss']
})
export class TierVehicleConfigurationComponent implements OnInit {

  @Input()
  eventInfo!: ActiveEventInfo

  @Input()
  tier!: Tier

  vehicles: {id: string; displayName:string}[] = [];
  selectedVehicle: any;

  constructor(public clubTiersService: ClubTiersService) { }

  ngOnInit(): void {
    this.clubTiersService.getVehicles(this.tier.id, this.eventInfo.challengeId, this.eventInfo.eventId).subscribe((vehicles:any) => {
      this.vehicles = vehicles;
    })
  }


  addVehicle() {
    this.vehicles.push(this.selectedVehicle);
    this.selectedVehicle = null;

    this.clubTiersService.addVehicles(this.tier.id, this.eventInfo.challengeId, this.eventInfo.eventId, this.vehicles.map(vehicle => vehicle.id)).pipe(take(1)).subscribe();
  }

  deleteVehicle() {
    this.vehicles = [];
    this.clubTiersService.addVehicles(this.tier.id, this.eventInfo.challengeId, this.eventInfo.eventId, []).pipe(take(1)).subscribe();

  }
}
