import {Component, Input} from '@angular/core';
import {AbstractControl} from "@angular/forms";
import {FixedPointsForm} from "./fixed-points.form";
import {PointPairForm} from "../point-pair-form/point-pair.form";

@Component({
  selector: 'app-fixed-points-form',
  templateUrl: './fixed-points-form.component.html',
  styleUrls: ['./fixed-points-form.component.scss']
})
export class FixedPointsFormComponent {

  @Input("formGroup")
  fixedPointsFormGroup!: FixedPointsForm;

  cast(form: AbstractControl) {
    return form as PointPairForm;
  }
}
