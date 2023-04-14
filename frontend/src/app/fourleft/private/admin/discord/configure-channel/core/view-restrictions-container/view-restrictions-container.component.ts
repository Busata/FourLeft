import {Component, Input, OnInit} from '@angular/core';
import {ResultRestrictionsTo} from '@server-models';
import {ViewResultsStoreService} from '../../services/view-results-store.service';
import {FieldMappingQueryService} from '../../../../field-mappings/field-mapping-query.service';

@Component({
  selector: 'app-view-restrictions-container',
  templateUrl: './view-restrictions-container.component.html',
  styleUrls: ['./view-restrictions-container.component.scss']
})
export class ViewRestrictionsContainerComponent implements OnInit {

  public restrictions: ResultRestrictionsTo[]= [];
  private _resultViewId!: string;
  @Input()
  public set resultViewId(value: string) {
    if(!value) { return;}

    this._resultViewId = value;

    this.viewResultsStoreService.getResultRestrictions(value).subscribe((restrictions) => {
      this.restrictions = restrictions;
    });

  }

  constructor(private viewResultsStoreService: ViewResultsStoreService,
              public fieldMapper: FieldMappingQueryService) {

  }

  ngOnInit(): void {

  }
  updateRestriction(restriction: ResultRestrictionsTo, $event: any) {
    this.viewResultsStoreService.createResultRestrictions(this._resultViewId, {
      ...restriction,
      ...$event
    });
  }
}
