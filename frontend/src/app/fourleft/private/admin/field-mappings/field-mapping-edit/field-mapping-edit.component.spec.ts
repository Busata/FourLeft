import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FieldMappingEditComponent } from './field-mapping-edit.component';

describe('FieldMappingEditComponent', () => {
  let component: FieldMappingEditComponent;
  let fixture: ComponentFixture<FieldMappingEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FieldMappingEditComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FieldMappingEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
