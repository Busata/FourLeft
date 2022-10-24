import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {ClubTiersService} from '../club-tiers.service';
import {combineLatest, flatMap, forkJoin, mergeMap, Observable, of, tap} from 'rxjs';
import {ActiveEventInfo} from '../domain/active-event-info';
import {FieldMappingQueryService} from '../../field-mappings/field-mapping-query.service';
import {Tier} from '../domain/tier';
import {moveItemInArray, transferArrayItem} from '@angular/cdk/drag-drop';

@Component({
  selector: 'app-club-tiers',
  templateUrl: './club-tiers-container.component.html',
  styleUrls: ['./club-tiers-container.component.scss']
})
export class ClubTiersContainerComponent implements OnInit {

  public activeEvent!: ActiveEventInfo;
  tierName: any;
  public tiers: Tier[] = [];

  constructor(private route: ActivatedRoute,
              private clubTiers: ClubTiersService,
              public fieldMapper: FieldMappingQueryService
  ) {
  }

  ngOnInit(): void {
    this.route.params.pipe(mergeMap((params) => {
      let clubId = params['id'];
      return combineLatest([this.clubTiers.getCurrentEvent(clubId).pipe(tap(event => {
          this.activeEvent = event;
        })), this.clubTiers.getTiers(clubId).pipe(tap(tiers => {
          this.tiers = tiers;
        }))]
      )
    })).subscribe(() => {
    })
  }


  addNewTier() {
    this.route.params.pipe(mergeMap((params) => {
      return this.clubTiers.createTier(params['id'], {name: this.tierName});
    })).subscribe((tier) => {
      this.tierName = '';
      this.tiers.push(tier);
    })
  }

  getPersons(tier: Tier) {
    return of([
      {
        id:1,
        name: "JamesF890"
      },
      {
        id:2,
        name: "BoringDamo"
      }
    ])
  }

  doSomething(event: any) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex,
      );
    }}
}
