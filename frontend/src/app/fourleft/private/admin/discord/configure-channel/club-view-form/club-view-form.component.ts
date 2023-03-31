import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormControl, FormGroup, UntypedFormGroup} from "@angular/forms";
import {ClubViewsFormHelper} from "../services/club-views-form-helper.service";

@Component({
  selector: 'app-club-view-form',
  templateUrl: './club-view-form.component.html',
  styleUrls: ['./club-view-form.component.scss']
})
export class ClubViewFormComponent {

  @Input("formGroup")
  clubViewForm!: FormGroup;

  private activeConfiguration : 'club' | 'results' | 'points' = 'club';

  public get title() {
    let title = ['Configure Club View'];
    if(this.activeConfiguration == 'results') {
      title.push('Results View');
    } else if(this.activeConfiguration == 'points') {
      title.push('Points View');
    }

    return title.join(' - ');
  }
  public get configuringClubView() {
    return this.activeConfiguration === 'club';
  }

  public get configuringResults() {
    return this.activeConfiguration === 'results';
  }

  public get configuringPoints() {
    return this.activeConfiguration === 'points';
  }
  constructor(public clubViewsFormHelperService: ClubViewsFormHelper) {
  }

  get resultViewForm() {
    return this.clubViewForm.get(ClubViewsFormHelper.RESULTS_VIEW) as FormGroup;
  }

  get fixedPointsForm() {
    return this.clubViewForm.get(ClubViewsFormHelper.POINTS_VIEW) as FormGroup;
  }

  get resultsViewType() {
    let type = this.resultViewForm.get('type') as FormControl;
    return type.value;
  }

  hasResultsView() {
    return !!this.clubViewForm.get(ClubViewsFormHelper.RESULTS_VIEW);
  }

  hasPointsView() {
    return !!this.clubViewForm.get(ClubViewsFormHelper.POINTS_VIEW);
  }

  get pointsViewForm() {
    return this.clubViewForm.get(ClubViewsFormHelper.POINTS_VIEW) as FormGroup;
  }

  get pointsViewType() {
    return (this.pointsViewForm?.get('type') as FormControl)?.value;
  }



  configureResults() {
    this.activeConfiguration = 'results';
  }

  configureClubView() {
    this.activeConfiguration = 'club';
  }

  configurePoints() {
    this.activeConfiguration = 'points';
  }

  setPointsView(value: any) {
    this.clubViewsFormHelperService.setPointsView(this.clubViewForm, value);
  }

  setResultsView(value: any) {
    this.clubViewsFormHelperService.setResultsView(this.clubViewForm, value);
  }
}
