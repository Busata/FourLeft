import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AliasRedirectComponent } from './alias-redirect.component';

describe('AliasRedirectComponent', () => {
  let component: AliasRedirectComponent;
  let fixture: ComponentFixture<AliasRedirectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AliasRedirectComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AliasRedirectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
