import {FormArray, FormControl, FormGroup, Validators} from "@angular/forms";
import {SingleClubViewTo} from "../../../../../../../common/generated/server-models";

export class SingleClubViewForm extends FormGroup {

  public readonly type = this.get('type') as FormControl;
  public readonly name = this.get('name') as FormControl;
  public readonly clubId = this.get('clubId') as FormControl;
  public readonly usePowerstage = this.get('usePowerstage') as FormControl;
  public readonly powerStageIndex = this.get('powerStageIndex') as FormControl;

  public readonly playerFilter = this.get('playerFilter') as FormGroup;

  constructor(value?: SingleClubViewTo) {
    super({
      type: new FormControl("singleClub", {}),
      name: new FormControl(value?.name, {}),
      clubId: new FormControl(value?.clubId, [Validators.required]),
      usePowerstage: new FormControl(value?.usePowerstage, {}),
      powerStageIndex: new FormControl(value?.powerStageIndex, {}),
      playerFilter: new FormGroup({
        playerFilterType: new FormControl(value?.playerFilter?.playerFilterType, {}),
        racenetNames: new FormArray(value?.playerFilter?.racenetNames?.map(name => new FormControl(name, {})) || [])
      })
    });

  }
}
