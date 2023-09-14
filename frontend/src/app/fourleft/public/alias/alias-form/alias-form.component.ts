import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AliasForm} from "./alias.form";
import {FormControl} from "@angular/forms";
import {AliasUpdateDataTo} from "@server-models";

@Component({
  selector: 'app-alias-form',
  templateUrl: './alias-form.component.html',
  styleUrls: ['./alias-form.component.scss']
})
export class AliasFormComponent {

  input!: AliasUpdateDataTo;
  form!: AliasForm;


  @Input()
  set aliasData(input: AliasUpdateDataTo) {
    this.input = input;
    this.form = new AliasForm(input);
  }

  @Output()
  formSaved = new EventEmitter();

  constructor() {
    this.form = new AliasForm();
  }

  removeAlias(idx: number) {
    this.form.removeAliasAt(idx);

  }

  addAlias($event: string) {
    this.form.addAlias($event);
  }

  cast(formControl: any) {
    return formControl as FormControl;
  }

  saveAliases() {
    this.formSaved.emit(this.form.getRawValue());
  }

  cancel() {
    this.form = new AliasForm(this.input);
  }
}
