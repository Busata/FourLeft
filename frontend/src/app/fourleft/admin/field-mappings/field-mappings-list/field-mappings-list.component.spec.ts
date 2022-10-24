import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FieldMappingsListComponent } from './field-mappings-list.component';

describe('FieldMappingsListComponent', () => {
  let component: FieldMappingsListComponent;
  let fixture: ComponentFixture<FieldMappingsListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FieldMappingsListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FieldMappingsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
