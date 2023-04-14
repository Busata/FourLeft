import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewRestrictionsFormComponent } from './view-restrictions-form.component';

describe('ViewRestrictionsFormComponent', () => {
  let component: ViewRestrictionsFormComponent;
  let fixture: ComponentFixture<ViewRestrictionsFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewRestrictionsFormComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ViewRestrictionsFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
