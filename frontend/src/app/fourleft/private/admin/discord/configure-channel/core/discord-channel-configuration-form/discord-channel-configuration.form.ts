import {FormControl, FormGroup} from "@angular/forms";
import {ClubViewForm} from "../club-view-form/club-view.form";
import {CreateDiscordChannelConfigurationTo} from "../../../../../../../common/generated/server-models";

export class DiscordChannelConfigurationForm extends FormGroup {
  public readonly enableAutoposts = this.get('enableAutoposts') as FormControl;
  public readonly clubView = this.get('clubView') as ClubViewForm;

  constructor(value?: CreateDiscordChannelConfigurationTo) {
    super({
      enableAutoposts: new FormControl(value?.enableAutoposts, {}),
      clubView: new ClubViewForm(value?.clubView)
    })
  }
}
