import {Component, EventEmitter, Input, Output} from '@angular/core';
import {ProfileForm} from "./profile.form";
import {ProfileTo} from "@server-models";

@Component({
    selector: 'app-easports-wrcprofile-form',
    templateUrl: './easports-wrcprofile-form.component.html',
    styleUrls: ['./easports-wrcprofile-form.component.scss']
})
export class EASportsWRCProfileFormComponent {
    input!: ProfileTo;
    form!: ProfileForm;
    @Output()
    formSaved = new EventEmitter();

    @Input()
    set profile(input: ProfileTo) {
        this.input = input;
        this.form = new ProfileForm(input);
    }

    constructor() {
        this.form = new ProfileForm();
    }

    saveProfile() {
        this.formSaved.emit(this.form.getRawValue());
    }

    cancel() {
        this.form = new ProfileForm(this.input);
    }
}
