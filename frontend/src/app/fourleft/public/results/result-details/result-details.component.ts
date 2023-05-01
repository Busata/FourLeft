import {Component, OnInit} from '@angular/core';
import {
  ViewResultsStoreService
} from '../../../private/admin/discord/configure-channel/services/view-results-store.service';
import {ActivatedRoute} from '@angular/router';
import {mergeMap} from 'rxjs';
import {ViewResultTo} from '@server-models';

@Component({
  selector: 'app-result-details',
  templateUrl: './result-details.component.html',
  styleUrls: ['./result-details.component.scss']
})
export class ResultDetailsComponent implements OnInit {
  public results!: ViewResultTo;


  constructor(
    private route: ActivatedRoute,
    private viewResultsStoreService: ViewResultsStoreService) {

  }

  ngOnInit(): void {
    this.route.params.pipe(mergeMap(params => {
      return this.viewResultsStoreService.getResults(params['id'])
    })).subscribe(results => {
      this.results = results;
    })
  }

}
