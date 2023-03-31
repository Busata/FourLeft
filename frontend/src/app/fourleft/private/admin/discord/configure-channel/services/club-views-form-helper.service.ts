import { Injectable } from '@angular/core';
import {FormArray, FormControl, FormGroup, Validators} from "@angular/forms";

@Injectable({
  providedIn: 'root'
})
export class ClubViewsFormHelper {

  public static readonly POINTS_VIEW = 'pointsView';
  public static readonly RESULTS_VIEW = 'resultsView';
  public static readonly STANDING_POINTS = 'standingPoints';
  public static readonly POWER_STAGE_POINTS = 'powerStagePoints';

  public static readonly TYPE_SINGLE_CLUB_VIEW = 'singleClub';
  public static readonly TYPE_MERGED_CLUB_VIEW = 'mergeClub';
  public static readonly TYPE_PARTITION_CLUB_VIEW = 'partitionClub';

  constructor() { }


  setPointsView(form: FormGroup, value: any) {
    if(value == 'defaultPoints') {
      form.setControl(ClubViewsFormHelper.POINTS_VIEW, this.createDefaultPointsView());

    } else if (value == 'fixedPoints') {
      form.setControl(ClubViewsFormHelper.POINTS_VIEW, this.createFixedPointsView());
    }
  }

  setResultsView(form: FormGroup, value: any) {
    switch(value) {
      case ClubViewsFormHelper.TYPE_SINGLE_CLUB_VIEW:
        this.setSingleClubView(form);
        break;
      case ClubViewsFormHelper.TYPE_MERGED_CLUB_VIEW:
        this.setMergeClubView(form);
        break;
      case ClubViewsFormHelper.TYPE_PARTITION_CLUB_VIEW:
        this.setPartitionClubView(form);
        break;
      default:
        form.setControl(ClubViewsFormHelper.RESULTS_VIEW, undefined);
    }
  }

  setSingleClubView(form: FormGroup) {
    form.setControl(ClubViewsFormHelper.RESULTS_VIEW, this.createSingleClubViewForm());
  }

  setMergeClubView(form: FormGroup) {
    form.setControl(ClubViewsFormHelper.RESULTS_VIEW, this.createMergeClubView());
  }

  setPartitionClubView(form: FormGroup) {
    form.setControl(ClubViewsFormHelper.RESULTS_VIEW, this.createPartitionClubView());
  }

  public createSingleClubViewForm() {
    return new FormGroup({
      type: new FormControl(ClubViewsFormHelper.TYPE_SINGLE_CLUB_VIEW, {}),
      name: new FormControl("", {}),
      clubId: new FormControl("", [Validators.required]),
      usePowerstage: new FormControl(false, {}),
      defaultPowerStageIndex: new FormControl("", {}),
      playerFilter: new FormGroup({
        playerFilterType: new FormControl("NONE", {}),
        racenetNames: new FormArray([])
      })
    })
  }
  public createMergeClubView() {
    return new FormGroup({
      type: new FormControl(ClubViewsFormHelper.TYPE_MERGED_CLUB_VIEW, {}),
      name: new FormControl("", {}),
      clubViews: new FormArray([])
    })
  }
  public createPartitionClubView() {
    return new FormGroup({
      type: new FormControl(ClubViewsFormHelper.TYPE_PARTITION_CLUB_VIEW, {}),
      name: new FormControl("",  {}),
      partitionElements: new FormArray([]),
    })
  }
  public createPartitionElement() {
    return new FormGroup({
      name: new FormControl("", {}),
      order: new FormControl("", {}),
      players: new FormArray([])
    })
  }

  public createDefaultPointsView() {
    return new FormGroup({
      type: new FormControl("defaultPoints", {}),
    })
  }

  public  createFixedPointsView() {
    return new FormGroup({
      type: new FormControl("fixedPoints", {}),
      defaultStandingPoint: new FormControl("1", {}),
      defaultPowerstagePoint: new FormControl("0", {}),
      defaultDNFPoint: new FormControl("0",{}),
      [ClubViewsFormHelper.STANDING_POINTS]: new FormArray([]),
      [ClubViewsFormHelper.POWER_STAGE_POINTS]: new FormArray([])
    })
  }

  public createPointPairForm(rank: number) {
    return new FormGroup({
      rank: new FormControl(`${rank}`,  {}),
      point: new FormControl("", {})
    })
  }
}
