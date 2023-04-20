import {Component, Input} from '@angular/core';
import {PointPairForm} from "./point-pair.form";

@Component({
  selector: 'app-point-pair-form',
  templateUrl: './point-pair-form.component.html',
  styleUrls: ['./point-pair-form.component.scss']
})
export class PointPairFormComponent {

  @Input("formGroup")
  form!: PointPairForm;

}
