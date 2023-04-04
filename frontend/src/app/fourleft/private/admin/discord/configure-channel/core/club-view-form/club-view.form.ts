import {FormControl, FormGroup, FormRecord} from "@angular/forms";
import {SingleClubViewForm} from "../../results/single-club-view-form/single-club-view.form";
import {MergedClubViewForm} from "../../results/merge-club-view-form/merged-club-view.form";
import {PartitionClubViewForm} from "../../results/partition-club-view-form/partition-club-view.form";
import {FixedPointsForm} from "../../points/fixed-points-form/fixed-points.form";
import {DefaultPointsForm} from "../../points/default-points-form/default-points.form";
import {
  ClubViewTo, FixedPointsCalculatorTo, MergedViewTo, PartitionViewTo, PointsCalculatorTo,
  ResultsViewTo,
  SingleClubViewTo
} from "../../../../../../../common/generated/server-models";

export class ClubViewForm extends FormGroup {
  public readonly badgeType = this.get('badgeType') as FormControl;

  public get resultsView() {
    if(this.contains('resultsView')) {
      return this.get('resultsView') as FormGroup;
    } else {
      return null;
    }
  }
  public get singleClubView() {
    return this.resultsView as SingleClubViewForm;
  }
  public get mergeClubView() {
    return this.resultsView as MergedClubViewForm;
  }
  public get partitionClubView() {
    return this.resultsView as PartitionClubViewForm;
  }

  public get pointsView() { return this.get('pointsView') as FormGroup}
  public get fixedPointsView() {return this.pointsView as FixedPointsForm }

  constructor(value?: ClubViewTo) {
    super({
      badgeType: new FormControl(value?.badgeType, {nonNullable: true}),
    });

    this.setResultsView(value?.resultsView as ResultsViewTo);
    this.setPointsView(value?.pointsView as PointsCalculatorTo );
  }
  get resultsViewType() {
   return this.resultsView?.get('type') as FormControl
  }
  get pointsViewType() {
    return this.pointsView?.get('type') as FormControl;
  }

  public setResultsView(value: any) {
    if(!value) {
      return;
    }
    switch (value.type) {
      case 'singleClub':
        this.setControl('resultsView', new SingleClubViewForm(value as SingleClubViewTo));
        break;
      case 'mergeClub':
        this.setControl('resultsView', new MergedClubViewForm(value as MergedViewTo));
        break;
      case 'partitionClub':
        this.setControl('resultsView', new PartitionClubViewForm(value as PartitionViewTo));
        break;
      default:
        this.setControl('resultsView', undefined);
    }
  }

  setPointsView(value: PointsCalculatorTo) {
    if(!value) {
      return;
    }
    switch (value.type) {
      case 'defaultPoints':
        this.setControl('pointsView', new DefaultPointsForm());
        break;
      case 'fixedPoints':
        this.setControl('pointsView', new FixedPointsForm(value as FixedPointsCalculatorTo));
        break;
      default:
        this.setControl('pointsView', undefined);
    }
  }
}
