import {Component, Input} from '@angular/core';
import {Platform} from '@server-models';

@Component({
  selector: 'app-platform-type',
  templateUrl: './platform-type.component.html',
  styleUrls: ['./platform-type.component.scss']
})
export class PlatformTypeComponent {

  @Input()
  public value: Platform = 'UNKNOWN';

}
