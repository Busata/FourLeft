import {Component, OnInit} from '@angular/core';
import {
  ViewResultsStoreService
} from '../../../private/admin/discord/configure-channel/services/view-results-store.service';
import {ActivatedRoute} from '@angular/router';
import {mergeMap} from 'rxjs';
import {DriverEntryTo, ViewResultTo} from '@server-models';

@Component({
  selector: 'app-result-details',
  templateUrl: './result-details.component.html',
  styleUrls: ['./result-details.component.scss']
})
export class ResultDetailsComponent implements OnInit {
  public results!: ViewResultTo;

  public resultsMode: 'current' | 'previous' = 'current';

  constructor(
    private route: ActivatedRoute,
    private viewResultsStoreService: ViewResultsStoreService) {

  }

  public toggleMode() {
    if(this.resultsMode === 'current') {
      this.getPreviousResults();
      this.resultsMode = 'previous';
    } else {
      this.getCurrentResults();
      this.resultsMode = 'current';
    }
  }

  ngOnInit(): void {
    this.getCurrentResults();
  }

  getCurrentResults() {
    this.route.params.pipe(mergeMap(params => {
      return this.viewResultsStoreService.getResults(params['id'])
    })).subscribe(results => {
      this.results = results;
    })
  }

  getPreviousResults() {
    this.route.params.pipe(mergeMap(params => {
      return this.viewResultsStoreService.getPreviousResults(params['id'])
    })).subscribe(results => {
      this.results = results;
    })
  }

  getResultsSortedByPowerRank(singleList: DriverEntryTo[]) {
    return singleList.sort((a, b) => {
      return a.relative.powerStageRank - b.relative.powerStageRank;
    })
  }

  getResultsSortedByActivityRank(singleList: DriverEntryTo[]) {
    return singleList.sort((a, b) => {
      return a.relative.activityRank - b.relative.activityRank;
    })
  }
}
