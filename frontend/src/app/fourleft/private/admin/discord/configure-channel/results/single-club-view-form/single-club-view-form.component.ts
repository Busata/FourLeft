import {Component, Input} from '@angular/core';
import {FormArray, FormControl, FormGroup} from "@angular/forms";
import {SingleClubViewForm} from "./single-club-view.form";

@Component({
  selector: 'app-single-club-view-form',
  templateUrl: './single-club-view-form.component.html',
  styleUrls: ['./single-club-view-form.component.scss']
})
export class SingleClubViewFormComponent {

  @Input("formGroup")
  singleClubFormGroup!: SingleClubViewForm


  showPlayers() {
    return ["INCLUDE","EXCLUDE"].indexOf(this.playerFilterType) !== -1;
  }

  private get playerFilter() {
    return this.singleClubFormGroup.get('playerFilter') as FormGroup;
  }

  private get playerFilterType() {
    return this.playerFilter?.get('playerFilterType')?.value;
  }

  public get players() {
    return this.playerFilter.get('racenetNames') as FormArray;
  }

  addPlayer(name: string) {
    this.players.push(new FormControl(name, {}))
  }

  removePlayer(i: number) {
    this.players.removeAt(i);
  }

  updatePlayers() {
    if(this.playerFilterType == 'NONE') {
      this.players.clear();
    }
  }

  usesPowerstage() {
    return !!this.singleClubFormGroup.get('usePowerstage')?.value;
  }
}
