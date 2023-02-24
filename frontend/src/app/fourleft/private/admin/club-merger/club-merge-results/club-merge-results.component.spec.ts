import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClubMergeResultsComponent } from './club-merge-results.component';

describe('ClubMergeResultsComponent', () => {
  let component: ClubMergeResultsComponent;
  let fixture: ComponentFixture<ClubMergeResultsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ClubMergeResultsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ClubMergeResultsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
