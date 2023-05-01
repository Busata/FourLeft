import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResultsContainerComponent } from './results-container.component';

describe('ResultsContainerComponent', () => {
  let component: ResultsContainerComponent;
  let fixture: ComponentFixture<ResultsContainerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ResultsContainerComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ResultsContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
