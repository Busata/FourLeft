import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TierVehicleConfigurationComponent } from './tier-vehicle-configuration.component';

describe('TierVehicleConfigurationComponent', () => {
  let component: TierVehicleConfigurationComponent;
  let fixture: ComponentFixture<TierVehicleConfigurationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TierVehicleConfigurationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TierVehicleConfigurationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
