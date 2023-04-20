import {FormArray, FormControl, FormGroup} from '@angular/forms';
import {ResultRestrictionsTo, VehicleTo} from '@server-models';

export class ViewRestrictionsForm extends FormGroup {
  public readonly vehicleClass = this.get('vehicleClass') as FormControl;
  public readonly restrictedVehicles = this.get('restrictedVehicles') as FormArray;

  constructor(value?: ResultRestrictionsTo) {
    super({
      vehicleClass: new FormControl(value?.vehicleClass),
      restrictedVehicles: new FormArray<FormControl<VehicleTo>>(value?.restrictedVehicles.map(vehicle => new FormGroup({
        id: new FormControl(vehicle.id),
        displayName: new FormControl(vehicle.displayName)
      })) as any, {})
    });
  }

  pushVehicle(vehicle: VehicleTo) {
    this.restrictedVehicles.push(new FormGroup({
      id: new FormControl(vehicle.id),
      displayName: new FormControl(vehicle.displayName)
    }, {}));
  }

  removeVehicle(vehicle: VehicleTo) {
    const idx = this.restrictedVehicles.controls.map(grp => grp.value.id).indexOf(vehicle.id);
    this.restrictedVehicles.removeAt(idx);
  }
}
