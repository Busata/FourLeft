import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ManagementContainerComponent } from './management-container.component';

describe('ManagementContainerComponent', () => {
  let component: ManagementContainerComponent;
  let fixture: ComponentFixture<ManagementContainerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ManagementContainerComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ManagementContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
