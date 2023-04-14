import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ResultRestrictionsTo, VehicleTo} from '@server-models';
import {QueryService} from '../../services/query-service';
import {FieldMappingQueryService} from '../../../../field-mappings/field-mapping-query.service';
import {ViewRestrictionsForm} from './view-restrictions.form';

@Component({
  selector: 'app-view-restrictions-form',
  templateUrl: './view-restrictions-form.component.html',
  styleUrls: ['./view-restrictions-form.component.scss']
})
export class ViewRestrictionsFormComponent implements OnInit{

  public form!: ViewRestrictionsForm;

  public vehicles: VehicleTo[] = [];

  @Input()
  set viewRestrictions(value: ResultRestrictionsTo){
    this.form = new ViewRestrictionsForm(value);

    this.queryService.getVehicles(value.vehicleClass).subscribe((vehicles) => {
      this.vehicles = vehicles;
    });
  }

  @Output()
  restrictionsUpdated = new EventEmitter();

  ngOnInit() {
    this.form.valueChanges.subscribe(value => {
      this.restrictionsUpdated.emit(value);
    })
  }

  constructor(private queryService: QueryService, public fieldMapper: FieldMappingQueryService) {
  }

  isRestricted(vehicle: VehicleTo) {
    return this.form.restrictedVehicles.controls.map(control => control.value.id).includes(vehicle.id);
  }
  restrictVehicle(vehicle: VehicleTo) {
    this.form.pushVehicle(vehicle);
  }

  allowVehicle(vehicle: VehicleTo) {
    this.form.removeVehicle(vehicle);
  }
}
