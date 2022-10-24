import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RacenetNamesComponent } from './racenet-names.component';

describe('RacenetNamesComponent', () => {
  let component: RacenetNamesComponent;
  let fixture: ComponentFixture<RacenetNamesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RacenetNamesComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RacenetNamesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
