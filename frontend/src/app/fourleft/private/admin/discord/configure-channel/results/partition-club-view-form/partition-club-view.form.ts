import {FormArray, FormControl, FormGroup} from "@angular/forms";
import {SingleClubViewForm} from "../single-club-view-form/single-club-view.form";
import {MergedClubViewForm} from "../merge-club-view-form/merged-club-view.form";
import {PartitionElementForm} from "../partition-element-form/partition-element.form";
import {
  MergedViewTo,
  PartitionViewTo,
  ResultsViewTo,
  SingleClubViewTo
} from "../../../../../../../common/generated/server-models";

export class PartitionClubViewForm extends FormGroup {

  public readonly type = this.get('type') as FormControl;
  public readonly partitionElements = this.get('partitionElements') as FormArray;

  public get resultsView() {
    return this.get('resultsView') as FormGroup;
  }
  public get singleClubView() {
    return this.resultsView as SingleClubViewForm;
  }
  public get mergeClubView() {
    return this.resultsView as MergedClubViewForm;
  }

  get resultsViewType() {
    return this.resultsView?.get('type') as FormControl;
  }
  constructor(value?: PartitionViewTo) {
    super({
      type: new FormControl('partitionClub', {}),
      partitionElements: new FormArray(value?.partitionElements?.map(e => new PartitionElementForm(e)) || []),
    });
    this.setResultsView(value?.resultsView as ResultsViewTo);
  }

  public setResultsView(value: any) {
    switch (value?.type) {
      case 'singleClub':
        this.setControl('resultsView', new SingleClubViewForm(value as SingleClubViewTo));
        break;
      case 'mergeClub':
        this.setControl('resultsView', new MergedClubViewForm(value as MergedViewTo));
        break;
      default:
        this.setControl('resultsView', new FormGroup({type: new FormControl('', {})}));
    }
  }

  addPartitionElement() {
    this.partitionElements.push(new PartitionElementForm());
  }

  removePartitionElement(i: number) {
    this.partitionElements.removeAt(i);
  }
}
