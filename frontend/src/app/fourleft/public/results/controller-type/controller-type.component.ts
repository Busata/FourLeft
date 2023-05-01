import {Component, Input} from '@angular/core';
import {ControllerType} from '@server-models';

@Component({
  selector: 'app-controller-type',
  templateUrl: './controller-type.component.html',
  styleUrls: ['./controller-type.component.scss']
})
export class ControllerTypeComponent {

  @Input()
  public value: ControllerType = 'UNKNOWN';

}
