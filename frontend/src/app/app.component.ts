import {Component, OnInit} from '@angular/core';
import {FieldMappingsService} from './fourleft/shared/field-mappings.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  title = 'fourleft-frontend';


  constructor(private fieldMappingService: FieldMappingsService) {
  }

  ngOnInit(): void {
    this.fieldMappingService.loadFieldMappings();
  }


}
