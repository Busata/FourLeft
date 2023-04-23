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
  constructor() {
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

  isResultsViewDisabled() {
    return this.clubViewForm?.resultsViewType?.value === '';
  }

  isPointsViewDisabled() {
    return this.clubViewForm?.pointsViewType?.value === '';
  }
}
