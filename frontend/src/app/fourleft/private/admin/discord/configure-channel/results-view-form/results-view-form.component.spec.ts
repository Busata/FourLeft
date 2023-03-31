import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResultsViewFormComponent } from './results-view-form.component';

describe('CreateResultsViewComponent', () => {
  let component: ResultsViewFormComponent;
  let fixture: ComponentFixture<ResultsViewFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ResultsViewFormComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ResultsViewFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
