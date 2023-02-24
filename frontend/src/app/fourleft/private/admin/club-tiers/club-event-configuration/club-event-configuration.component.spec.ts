import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClubEventConfigurationComponent } from './club-event-configuration.component';

describe('ClubEventConfigurationComponent', () => {
  let component: ClubEventConfigurationComponent;
  let fixture: ComponentFixture<ClubEventConfigurationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ClubEventConfigurationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ClubEventConfigurationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
