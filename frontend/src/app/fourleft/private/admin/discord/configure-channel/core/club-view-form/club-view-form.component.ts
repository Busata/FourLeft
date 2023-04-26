import {Component, Input} from '@angular/core';
import {ClubViewForm} from "./club-view.form";

@Component({
  selector: 'app-club-view-form',
  templateUrl: './club-view-form.component.html',
  styleUrls: ['./club-view-form.component.scss']
})
export class ClubViewFormComponent {

  @Input("formGroup")
  clubViewForm!: ClubViewForm;

  @Input()
  activeConfiguration: string = 'results';

  @Input()
  set resultsViewType(value: string) {
    this.clubViewForm?.setResultsView({type: value})
  }
  @Input()
  set pointsViewType(value: any) {
    this.clubViewForm?.setPointsView({type: value})
  }



  public get configuringResults() {
    return this.activeConfiguration === 'results';
  }

  public get configuringPoints() {
    return this.activeConfiguration === 'points';
  }

  public get isPreviewOpen() {
    return this.activeConfiguration === 'preview';
  }

  constructor() {
  }

  isResultsViewDisabled() {
    return this.clubViewForm?.resultsViewType?.value === '';
  }

  isPointsViewDisabled() {
    return this.clubViewForm?.pointsViewType?.value === '';
  }
}
