import {Component, Input} from '@angular/core';
import {FormArray, FormGroup} from "@angular/forms";
import {ClubViewsFormHelper} from "../services/club-views-form-helper.service";

@Component({
  selector: 'app-fixed-points-form',
  templateUrl: './fixed-points-form.component.html',
  styleUrls: ['./fixed-points-form.component.scss']
})
export class FixedPointsFormComponent {

  @Input("formGroup")
  fixedPointsFormGroup!: FormGroup;

  constructor(private clubViewFormHelper: ClubViewsFormHelper) {
  }

  addStandingPoints() {
    const length = this.standingPoints.length;
    this.standingPoints.push(this.clubViewFormHelper.createPointPairForm(length + 1));

  }

  public get standingPoints() {
    return this.fixedPointsFormGroup.get(ClubViewsFormHelper.STANDING_POINTS) as FormArray;
  }

  addPowerstagePoints() {
    const length = this.powerStagePoints.length;
    this.powerStagePoints.push(this.clubViewFormHelper.createPointPairForm(length + 1));
  }

  public get powerStagePoints() {
    return this.fixedPointsFormGroup.get(ClubViewsFormHelper.POWER_STAGE_POINTS) as FormArray
  }

  removeStandingsPair(i: number) {
    this.standingPoints.removeAt(i);
  }

  removePowerStagePair(i: number) {
    this.powerStagePoints.removeAt(i);
  }
}
